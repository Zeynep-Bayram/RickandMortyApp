\# Rick and Morty App



\*\*Jetpack Compose\*\* ve \*\*Kotlin\*\* ile geliştirilmiş, Rick and Morty API üzerinden karakter verilerini listeleyen, arayan ve interaktif 3D flip kart animasyonuyla karakter detaylarını gösteren bir Android uygulamasıdır.



Uygulama; REST API üzerinden asenkron veri çekme, MVVM mimarisi, state-driven UI tasarımı ve Jetpack Compose animasyonları gibi modern Android geliştirme pratiklerini bir araya getirir.



<p align="center">

&#x20; <a href="#özellikler">Özellikler</a> •

&#x20; <a href="#ekran-görüntüleri">Ekran Görüntüleri</a> •

&#x20; <a href="#kullanılan-teknolojiler">Teknolojiler</a> •

&#x20; <a href="#mimari">Mimari</a> •

&#x20; <a href="#projeyi-çalıştırma">Çalıştırma</a>

</p>



\---



\## Özellikler



\- \*\*Karakter Listeleme\*\* — Rick and Morty API üzerinden karakter verilerinin çekilip listelenmesi.

\- \*\*Karakter Arama\*\* — Girilen isme göre API üzerinden karakter araması yapılabilmesi.

\- \*\*3D Flip Kart Animasyonu\*\* — Karakter kartına dokunulduğunda kartın 180° dönerek arka yüzündeki detayları göstermesi.

\- \*\*Detaylı Karakter Bilgisi\*\* — Durum (status), tür (species), cinsiyet, köken ve mevcut konum bilgilerinin görüntülenmesi.

\- \*\*State Yönetimi\*\* — Loading, Success ve Error durumlarının ayrı ayrı ve tutarlı biçimde yönetilmesi.

\- \*\*Hata Yönetimi ve Tekrar Deneme\*\* — Ağ veya API hatasında kullanıcıya hata mesajı gösterilmesi ve "Tekrar Dene" seçeneği sunulması.

\- \*\*Uzaktan Görsel Yükleme\*\* — Karakter görsellerinin Coil ile API üzerinden yüklenmesi.



\---



\## Ekran Görüntüleri



Uygulamanın farklı kullanım senaryolarına ait ekran görüntüleri aşağıda yer almaktadır.



\### Ana Ekran



Karakterlerin API üzerinden çekilerek listelendiği ana ekran.



!\[Ana Ekran](screenshots/homepage.png)



\### Karakter Arama



Kullanıcının karakter adına göre arama yapabildiği ekran.



!\[Karakter Arama](screenshots/search.png)



\### Arama Sonuçları



Girilen arama sorgusuna göre API'den dönen karakterlerin listelendiği ekran.



!\[Arama Sonuçları](screenshots/searchresult.png)



\### Kart Detayı



Karakter kartına dokunulduğunda gerçekleşen 3D flip animasyonu sonrasında karakterin detay bilgilerinin gösterildiği ekran.



!\[Kart Detayı](screenshots/details.png)



\---



\## Kullanılan Teknolojiler



| Katman | Teknoloji | Açıklama |

|---|---|---|

| Dil | \*\*Kotlin\*\* | Android için resmi ve modern programlama dili |

| UI | \*\*Jetpack Compose\*\* | Deklaratif ve modern Android UI araç seti |

| UI Bileşenleri | \*\*Material 3\*\* | Tasarım sistemi ve hazır UI bileşenleri |

| Asenkronluk | \*\*Kotlin Coroutines\*\* | Asenkron ve non-blocking API çağrıları |

| Ağ Katmanı | \*\*Retrofit\*\* | Tip güvenli (type-safe) HTTP istemcisi |

| Serileştirme | \*\*Gson\*\* | JSON verilerinin Kotlin modellerine dönüştürülmesi |

| Görsel Yükleme | \*\*Coil\*\* | Compose ile uyumlu uzaktan görsel yükleme kütüphanesi |

| State Yönetimi | \*\*ViewModel\*\* | UI state ve verilerin yönetilmesi |

| Listeleme | \*\*LazyColumn\*\* | Performanslı listeleme |

| Animasyon | \*\*Compose Animation\*\* | 3D kart flip animasyonu |

| Mimari | \*\*MVVM\*\* | Model-View-ViewModel mimari deseni |



\---



\## Kullanılan API



Uygulamada açık kaynaklı \*\*Rick and Morty API\*\* kullanılmaktadır.



\### Base URL



```text

https://rickandmortyapi.com/api/

```



\### Endpoint



```text

GET /character

```



API üzerinden alınan karakter verileri Retrofit kullanılarak uygulamaya aktarılır ve Gson ile Kotlin modellerine dönüştürülür.



Uygulamada kullanılan temel karakter alanları:



\- `id`

\- `name`

\- `status`

\- `species`

\- `gender`

\- `type`

\- `origin`

\- `location`

\- `image`



\---



\## Mimari



Proje \*\*MVVM (Model-View-ViewModel)\*\* mimari desenini takip eder.



Bu yapı; API işlemleri, veri modelleri, UI state'leri ve kullanıcı arayüzü sorumluluklarının birbirinden ayrılmasını sağlayarak daha düzenli, test edilebilir ve sürdürülebilir bir yapı oluşturur.



\### Veri Akışı



```text

Rick and Morty API

&#x20;       ↓

&#x20;    Retrofit

&#x20;       ↓

&#x20;  CharacterApi

&#x20;       ↓

&#x20;CharacterViewModel

&#x20;       ↓

&#x20;CharacterUiState

&#x20;       ↓

&#x20;CharacterListScreen

&#x20;       ↓

&#x20;  Jetpack Compose

```



\### Katmanlar



\#### Model



API'den gelen verileri temsil eden veri sınıflarını içerir.



\- `Character` — Tekil karakter modelini temsil eder.

\- `CharacterResponse` — API'den dönen karakter listesini temsil eder.



\#### API



Retrofit kullanılarak API iletişimi gerçekleştirilir.



\- `CharacterApi` — API endpoint'lerinin tanımlandığı Retrofit interface'idir.

\- `RetrofitInstance` — Retrofit istemcisinin oluşturulduğu yapıdır.



\#### ViewModel



`CharacterViewModel`, API'den veri çekme işlemini Kotlin Coroutines kullanarak asenkron şekilde yürütür.



API sonucuna göre `CharacterUiState` güncellenir ve UI'ın hangi durumda olması gerektiği belirlenir.



Arama sorgularının yönetimi de ViewModel içerisinde gerçekleştirilir.



\#### View



Jetpack Compose ile oluşturulan `CharacterListScreen`, ViewModel tarafından sağlanan state'i gözlemler ve mevcut duruma göre uygun kullanıcı arayüzünü oluşturur.



\- Loading durumunda `CircularProgressIndicator` gösterilir.

\- Success durumunda karakterler `LazyColumn` içerisinde listelenir.

\- Error durumunda hata mesajı ve tekrar deneme butonu gösterilir.



Kart etkileşimleri ve 3D flip animasyonu da View katmanında yönetilir.



\---



\## UI State Yönetimi



Uygulamadaki farklı UI durumlarını temsil etmek için `CharacterUiState` sealed interface yapısı kullanılmıştır.



```kotlin

sealed interface CharacterUiState {

&#x20;   object Loading : CharacterUiState



&#x20;   data class Success(

&#x20;       val characters: List<Character>

&#x20;   ) : CharacterUiState



&#x20;   data class Error(

&#x20;       val message: String

&#x20;   ) : CharacterUiState

}

```



Bu yapı sayesinde UI, uygulamanın mevcut state'ine göre şekillenir.



```text

&#x20;       Loading

&#x20;          ↓

&#x20;      API Request

&#x20;          ↓

&#x20;    ┌─────┴─────┐

&#x20;    ↓           ↓

&#x20; Success       Error

&#x20;    ↓           ↓

&#x20; Listeleme   Hata Mesajı

&#x20;                 ↓

&#x20;            Tekrar Dene

```



Bu yaklaşım, UI'ın doğrudan mevcut uygulama state'ine bağlı olarak oluşturulmasını sağlar.



\---



\## Karakter Arama



Arama işlemi istemci tarafında mevcut listenin filtrelenmesi yerine doğrudan API üzerinden gerçekleştirilir.



Kullanıcının girdiği arama metni Retrofit içerisindeki `@Query("name")` parametresiyle API isteğine eklenir.



Örneğin:



```text

Rick

```



arama sorgusu API'ye karakter adı parametresi olarak gönderilir.



API'den dönen sonuç tekrar `CharacterUiState` üzerinden UI'a aktarılır ve ekranda listelenir.



\---



\## 3D Kart Animasyonu



Karakter kartına dokunulduğunda kartın ön yüzünden arka yüzüne geçiş yapılır.



Kartın ön yüzünde:



\- Karakter görseli

\- Karakter adı



yer alırken, arka yüzünde:



\- Status

\- Species

\- Gender

\- Type

\- Origin

\- Location



bilgileri gösterilir.



Animasyon Jetpack Compose içerisindeki `animateFloatAsState` ve `graphicsLayer` kullanılarak oluşturulmuştur.



`rotationY` değeri kartın yatay eksende dönüşünü sağlarken, `cameraDistance` 3D perspektif görünümünü oluşturmak için kullanılır.



```text

0° ──────────────────→ 180°



Ön Yüz                    Arka Yüz

Görsel / İsim              Detaylar

```



\---



\## Öne Çıkan Teknik Konular



Bu projede özellikle aşağıdaki konseptler üzerinde çalışılmıştır:



\- REST API entegrasyonu ve veri alışverişi

\- Retrofit ile API isteklerinin gerçekleştirilmesi

\- Kotlin Coroutines ile asenkron ağ işlemleri

\- `viewModelScope` kullanımı

\- MVVM mimarisi ve katmanlar arası veri akışı

\- Sealed Interface ile UI state yönetimi

\- State-driven UI yaklaşımı

\- Jetpack Compose ve recomposition mekanizması

\- `remember` / `rememberSaveable` ile state saklama

\- `LaunchedEffect` ile side-effect yönetimi

\- `LazyColumn` ile performanslı listeleme

\- Coil ile uzaktan görsel yükleme

\- Compose Animation

\- `animateFloatAsState` kullanımı

\- `graphicsLayer` ile 3D dönüşümler

\- `rotationY` ve `cameraDistance` kullanımı

\- API ve ağ hatalarının yönetimi



\---



\## Proje Yapısı



```text

com.example.rickandmortyapp

│

├── api

│   ├── CharacterApi.kt

│   └── RetrofitInstance.kt

│

├── model

│   ├── Character.kt

│   └── CharacterResponse.kt

│

├── ui

│   ├── CharacterListScreen.kt

│   └── theme

│       ├── Color.kt

│       ├── Theme.kt

│       └── Type.kt

│

├── viewmodel

│   └── CharacterViewModel.kt

│

└── MainActivity.kt

```



\---



\## Projeyi Çalıştırma



\### Gereksinimler



\- Android Studio (güncel sürüm önerilir)

\- JDK 11 veya üzeri

\- Android SDK

\- İnternet bağlantısı (API istekleri için)



\### Kurulum



1\. Repository'yi klonlayın:



```bash

git clone https://github.com/Zeynep-Bayram/RickAndMortyApp.git

```



2\. Projeyi Android Studio ile açın.



3\. Gradle senkronizasyonunun tamamlanmasını bekleyin.



4\. Uygulamayı bir emülatör veya fiziksel Android cihaz üzerinde çalıştırın.



> Uygulamanın karakter verilerini API üzerinden alabilmesi için cihazın internete bağlı olması gerekir.



\---



\## Repository



Projenin kaynak koduna GitHub repository üzerinden ulaşabilirsiniz:



https://github.com/Zeynep-Bayram/RickAndMortyApp



\---



<p align="center">

&#x20; Geliştirme sürecinde modern Android geliştirme yaklaşımları kullanılarak hazırlanmıştır.

</p>

