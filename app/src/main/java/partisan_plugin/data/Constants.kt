package partisan_plugin.data

object Constants {
    const val DEFAULT_TIMEOUT = 30
    const val DEFAULT_DATABASE_SIZE = 10
    const val DEFAULT_MEMORY = 46
    const val ITERATIONS = 10 //parameters for Argon2 are stricter than recommended by OWASP https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#argon2id
    const val PARALLELISM = 1
    const val CIPHER = "AES"
}