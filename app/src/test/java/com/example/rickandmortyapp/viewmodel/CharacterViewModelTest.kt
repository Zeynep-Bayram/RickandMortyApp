package com.example.rickandmortyapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapp.api.CharacterApi
import com.example.rickandmortyapp.model.Character
import com.example.rickandmortyapp.model.CharacterLocation
import com.example.rickandmortyapp.model.CharacterResponse
import com.example.rickandmortyapp.model.Info
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val api = FakeCharacterApi()
    private lateinit var viewModel: CharacterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = CharacterViewModel(api, flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `typing sends only the final query after 500 ms without loading on each letter`() = runTest(dispatcher) {
        runCurrent()
        val initialState = viewModel.uiState.value
        for (query in listOf("r", "ri", "ric", "rick")) {
            viewModel.onSearchTextChange(query)
            runCurrent()
            assertEquals(initialState, viewModel.uiState.value)
            advanceTimeBy(100)
        }
        advanceTimeBy(399)
        runCurrent()
        assertEquals(listOf(Request(null, 1)), api.requests)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(Request(null, 1), Request("rick", 1)), api.requests)
        assertEquals("rick", viewModel.searchText.value)
    }

    @Test
    fun `equivalent trimmed queries do not repeat the request`() = runTest(dispatcher) {
        runCurrent()
        viewModel.onSearchTextChange("rick")
        advanceTimeBy(500)
        runCurrent()
        viewModel.onSearchTextChange(" rick ")
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(listOf(Request(null, 1), Request("rick", 1)), api.requests)
    }

    @Test
    fun `reopening after no results resets text and requests the unfiltered first page`() = runTest(dispatcher) {
        api.respond = { name, _ ->
            if (name != null) throw notFound()
            response()
        }
        runCurrent()
        viewModel.onSearchTextChange("no-such-character")
        advanceTimeBy(500)
        runCurrent()
        assertEquals(CharacterUiState.Empty, viewModel.uiState.value)

        viewModel.resetSearch()
        assertEquals("", viewModel.searchText.value)
        runCurrent()
        assertEquals(Request(null, 1), api.requests.last())
        assertTrue(viewModel.uiState.value is CharacterUiState.Success)

        // The same unsuccessful query must still work after using Back to search.
        viewModel.onSearchTextChange("no-such-character")
        advanceTimeBy(500)
        runCurrent()
        assertEquals(2, api.requests.count { it.name == "no-such-character" })
        assertEquals(CharacterUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `reset discards a query still waiting in debounce`() = runTest(dispatcher) {
        runCurrent()
        viewModel.onSearchTextChange("rick")
        advanceTimeBy(250)
        viewModel.resetSearch()
        runCurrent()
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(listOf(Request(null, 1), Request(null, 1)), api.requests)
        assertTrue(viewModel.uiState.value is CharacterUiState.Success)
    }

    @Test
    fun `clearing text loads all characters immediately`() = runTest(dispatcher) {
        runCurrent()
        viewModel.onSearchTextChange("rick")
        advanceTimeBy(500)
        runCurrent()
        viewModel.onSearchTextChange("")
        runCurrent()
        assertEquals(listOf(Request(null, 1), Request("rick", 1), Request(null, 1)), api.requests)
        assertTrue(viewModel.uiState.value is CharacterUiState.Success)
    }

    @Test
    fun `a late cancelled failure cannot overwrite reset results`() = runTest(dispatcher) {
        val finishOldRequest = CompletableDeferred<Unit>()
        api.respond = { name, _ ->
            if (name == "missing") {
                withContext(NonCancellable) { finishOldRequest.await() }
                throw notFound()
            }
            response()
        }
        runCurrent()
        viewModel.onSearchTextChange("missing")
        advanceTimeBy(500)
        runCurrent()
        viewModel.resetSearch()
        runCurrent()
        assertTrue(viewModel.uiState.value is CharacterUiState.Success)
        finishOldRequest.complete(Unit)
        runCurrent()
        assertTrue(viewModel.uiState.value is CharacterUiState.Success)
        assertEquals("", viewModel.searchText.value)
    }

    @Test
    fun `new input cancels the active request before the debounce expires`() = runTest(dispatcher) {
        val finishOldRequest = CompletableDeferred<Unit>()
        var wasCancelled = false
        api.respond = { name, _ ->
            if (name == "rick") {
                try {
                    finishOldRequest.await()
                } finally {
                    wasCancelled = true
                }
            }
            response()
        }
        runCurrent()
        viewModel.onSearchTextChange("rick")
        advanceTimeBy(500)
        runCurrent()
        viewModel.onSearchTextChange("morty")
        runCurrent()
        assertTrue(wasCancelled)
        assertFalse(viewModel.uiState.value is CharacterUiState.Error)
        assertEquals(Request("rick", 1), api.requests.last())
        advanceTimeBy(500)
        runCurrent()
        assertEquals(Request("morty", 1), api.requests.last())
    }

    @Test
    fun `returning to the previous query after cancellation does not leave loading stuck`() = runTest(dispatcher) {
        val waitForResponse = CompletableDeferred<Unit>()
        api.respond = { name, _ ->
            if (name == "rick" && api.requests.count { it.name == name } == 1) {
                waitForResponse.await()
            }
            response()
        }
        runCurrent()
        viewModel.onSearchTextChange("rick")
        advanceTimeBy(500)
        runCurrent()
        viewModel.onSearchTextChange("ricky")
        runCurrent()
        advanceTimeBy(100)
        viewModel.onSearchTextChange("rick")
        advanceTimeBy(500)
        runCurrent()
        assertEquals(2, api.requests.count { it.name == "rick" })
        assertTrue(viewModel.uiState.value is CharacterUiState.Success)
    }

    @Test
    fun `pagination keeps results visible and retries the failed page without skipping it`() = runTest(dispatcher) {
        val nextPageResponse = CompletableDeferred<CharacterResponse>()
        api.respond = { _, page -> if (page == 1) response(hasNext = true) else nextPageResponse.await() }
        runCurrent()
        viewModel.loadNextPage()
        viewModel.loadNextPage()
        runCurrent()
        val loadingState = viewModel.uiState.value as CharacterUiState.Success
        assertEquals(listOf(1), loadingState.characters.map { it.id })
        assertTrue(loadingState.isLoadingNextPage)
        assertEquals(1, api.requests.count { it.page == 2 })

        nextPageResponse.completeExceptionally(IOException("offline"))
        runCurrent()
        assertNotNull((viewModel.uiState.value as CharacterUiState.Success).nextPageError)
        viewModel.loadNextPage()
        runCurrent()
        assertEquals(1, api.requests.count { it.page == 2 })

        api.respond = { _, _ -> response(id = 2) }
        viewModel.retry()
        runCurrent()
        assertEquals(listOf(1, 2, 2), api.requests.map { it.page })
        assertEquals(listOf(1, 2), (viewModel.uiState.value as CharacterUiState.Success).characters.map { it.id })
        viewModel.loadNextPage()
        runCurrent()
        assertEquals(3, api.requests.size)
    }

    @Test
    fun `reset starts at page one even when the previous query was already blank`() = runTest(dispatcher) {
        api.respond = { _, page -> response(id = page, hasNext = true) }
        runCurrent()
        viewModel.loadNextPage()
        runCurrent()
        viewModel.resetSearch()
        runCurrent()
        assertEquals(listOf(Request(null, 1), Request(null, 2), Request(null, 1)), api.requests)
        assertEquals(listOf(1), (viewModel.uiState.value as CharacterUiState.Success).characters.map { it.id })
    }

    @Test
    fun `new search starts at page one and cannot append an old query page`() = runTest(dispatcher) {
        val oldPage = CompletableDeferred<CharacterResponse>()
        api.respond = { name, page ->
            when {
                page == 2 -> withContext(NonCancellable) { oldPage.await() }
                name == "morty" -> response(id = 3)
                else -> response(hasNext = true)
            }
        }
        runCurrent()
        viewModel.loadNextPage()
        runCurrent()
        viewModel.onSearchTextChange("morty")
        runCurrent()
        viewModel.loadNextPage()
        advanceTimeBy(500)
        runCurrent()
        oldPage.complete(response(id = 2))
        runCurrent()
        assertEquals(listOf(Request(null, 1), Request(null, 2), Request("morty", 1)), api.requests)
        assertEquals(listOf(3), (viewModel.uiState.value as CharacterUiState.Success).characters.map { it.id })
    }

    private data class Request(val name: String?, val page: Int)

    private class FakeCharacterApi : CharacterApi {
        val requests = mutableListOf<Request>()
        var respond: suspend (String?, Int) -> CharacterResponse = { _, _ -> response() }

        override suspend fun searchCharacters(name: String?, page: Int): CharacterResponse {
            requests += Request(name, page)
            return respond(name, page)
        }
    }

    companion object {
        private fun notFound() = HttpException(Response.error<Any>(404, "{}".toResponseBody()))

        private fun response(id: Int = 1, hasNext: Boolean = false) = CharacterResponse(
            results = listOf(
                Character(
                    id = id,
                    name = "Character $id",
                    status = "Alive",
                    species = "Human",
                    type = "",
                    gender = "Male",
                    origin = CharacterLocation("Earth", ""),
                    location = CharacterLocation("Earth", ""),
                    image = ""
                )
            ),
            info = Info(count = 2, pages = 2, next = if (hasNext) "next" else null, prev = null)
        )
    }
}
