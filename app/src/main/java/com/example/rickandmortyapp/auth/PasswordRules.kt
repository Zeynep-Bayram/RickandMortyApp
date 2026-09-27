package com.example.rickandmortyapp.auth

data class PasswordRule(
    val label: String,
    val satisfied: Boolean
)

object PasswordRules {
    const val MIN_LENGTH = 8

    private val uppercase = Regex("[A-Z]")
    private val lowercase = Regex("[a-z]")
    private val digit = Regex("[0-9]")
    private val special = Regex("[^A-Za-z0-9]")

    fun evaluate(password: String): List<PasswordRule> = listOf(
        PasswordRule("At least $MIN_LENGTH characters", password.length >= MIN_LENGTH),
        PasswordRule("At least one uppercase letter (A-Z)", uppercase.containsMatchIn(password)),
        PasswordRule("At least one lowercase letter (a-z)", lowercase.containsMatchIn(password)),
        PasswordRule("At least one number (0-9)", digit.containsMatchIn(password)),
        PasswordRule("At least one special character (!@# etc.)", special.containsMatchIn(password)),
        PasswordRule("No spaces", password.none { it.isWhitespace() })
    )

    fun firstError(password: String): String? =
        evaluate(password).firstOrNull { !it.satisfied }?.label?.let { "Password rule: $it" }

    fun isStrong(password: String): Boolean = evaluate(password).all { it.satisfied }
}
