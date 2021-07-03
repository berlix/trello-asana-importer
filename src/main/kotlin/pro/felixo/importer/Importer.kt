package pro.felixo.importer

import com.asana.Client
import java.lang.IllegalStateException

class Importer(private val client: Client) {

    private val projects by lazy { client.projects.findAll().execute() }

    fun import(board: BoardState) {
        if (projects.any { it.name == board.name })
            throw IllegalStateException("Project '{$board.name}' already exists!")
    }
}
