package pro.felixo.importer

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

inline fun <T> Boolean.then(block: () -> T): T? = if (this) block() else null

inline fun <T> List<T>.forEachReversed(action: (T) -> Unit) {
    for (index in (size - 1) downTo 0)
        action(get(index))
}
