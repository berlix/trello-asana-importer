package pro.felixo.importer

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

inline fun <T> Boolean.then(block: () -> T): T? = if (this) block() else null
