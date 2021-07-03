package pro.felixo.importer

import com.asana.Client
import kotlinx.serialization.decodeFromString
import java.io.File

fun main(vararg args: String) {
    val importer = Importer(Client.accessToken(args[0]).apply {
        headers.put("Asana-Enable", "new_user_task_lists")
    })

    val directory = File(args[1]).also {
        require(it.isDirectory) { "$it is not a directory! "}
    }
    val files = requireNotNull(directory.listFiles()) { "Could not get directory listing" }
        .filter { it.isFile && it.extension == "json" }
        .map { json.decodeFromString<BoardState>(it.readText()) }

    files.forEach(importer::import)
}
