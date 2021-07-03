package pro.felixo.importer

import com.asana.Client
import com.asana.models.Project
import com.asana.models.Section
import com.asana.models.Tag
import com.asana.models.Task
import java.lang.IllegalStateException

class Importer(private val client: Client) {

    private val projects by lazy { client.projects.findAll().execute() }
    private val workspace by lazy { client.workspaces.findAll().execute().single() }
    private val team by lazy { client.teams.findByOrganization(workspace.gid).execute().single() }

    fun import(board: BoardState) {
        println("Beginning import of project ${board.name}")
        val project = project(board)
        val tags = tagsByLabelId(board.labels)
        val sections = sectionsByListId(board.lists, project)
        val tasks = tasksByCardId(board.cards, tags, sections)
        createSubtasks(board.checklists, tasks)
        createComments(board.actions, tasks)
        archiveProjectIfNecessary(board, project)
        println("Finished import of project ${board.name}")
    }

    private fun createComments(actions: List<Action>, tasks: Map<String, Task>) {
        actions.forEachReversed { action ->
            val comment = action.commentCardData
            if (comment != null) {
                val taskId = tasks[comment.card.id]!!.gid
                client.stories.createOnTask(taskId).apply {
                    data("text", "Original comment from ${action.date}:\n\n${comment.text}")
                }.execute()
            }
        }
    }

    private fun createSubtasks(
        checklists: List<ChecklistState>,
        tasks: Map<String, Task>
    ) {
        checklists.forEach { checklist ->
            checklist.checkItems.sortedByDescending { it.pos }.forEach { item ->
                client.tasks.createSubtaskForTask(tasks[checklist.idCard]!!.gid).apply {
                    data("name", item.name)
                    data("completed", item.state == CompletionState.Complete)
                }.execute()
            }
        }
    }

    private fun tasksByCardId(
        cards: List<CardState>,
        tags: Map<String, Tag>,
        sections: Map<String, Section>
    ): Map<String, Task> = cards.sortedByDescending { it.pos }.associate { card ->
        println ("Creating task for card: ${card.name}")
        card.id to client.tasks.createInWorkspace(workspace.gid).apply {
            data("name", card.name)
            data("completed", card.closed)
            data("due_at", card.due.toString())
            data("notes", card.desc)
            data("tags", card.idLabels.map { tags[it]!!.gid })
        }.execute().also { task ->
            client.sections.addTask(sections[card.idList]!!.gid).apply {
                data("task", task.gid)
            }.execute()
            card.attachments.forEach { attachment ->
                client.attachments
                    .createOnTask(task.gid, attachment.url.openStream(), attachment.fileName, attachment.mimeType)
                    .execute()
            }
        }
    }

    private fun sectionsByListId(lists: List<ListState>, project: Project) = lists.sortedBy { it.pos }.associate {
        it.id to client.sections.createInProject(project.gid).apply { data("name", it.name) }.execute()
    }

    private fun archiveProjectIfNecessary(board: BoardState, project: Project) {
        if (board.closed)
            client.projects.update(project.gid).apply { data("isArchived", true) }.execute()
    }

    private fun tagsByLabelId(labels: List<LabelState>): Map<String, Tag> {
        val labelNames = labels.map { it.name }.toSet()

        val existingTags = client.tags.findByWorkspace(workspace.gid).execute()
            .filter { it.name in labelNames }

        val existingTagNames = existingTags.map { it.name }.toSet()

        val newTags = labels.filter { it.name !in existingTagNames }
            .map {
                client.tags.createInWorkspace(workspace.gid).apply {
                    data("name", it.name)
                    data("color", trelloToAsanaColor(it.color))
                }.execute()
            }

        return labels.associate { label ->
            label.id to (
                existingTags.firstOrNull { it.name == label.name } ?: newTags.first { it.name == label.name }
            )
        }
    }

    private fun project(board: BoardState): Project {
        if (projects.any { it.name == board.name })
            throw IllegalStateException("Project '${board.name}' already exists!")
        return client.projects.create().apply {
            data("name", board.name)
            data("workspace", workspace.gid)
            data("team", team.gid)
            data("default_view", "board")
            data("notes", board.desc)
        }.execute()
    }

    companion object {
        // Valid Asana colors are:
        // dark-blue, dark-brown, dark-green, dark-orange, dark-pink, dark-purple, dark-red, dark-teal, dark-warm-gray,
        // light-blue, light-green, light-orange, light-pink, light-purple, light-red, light-teal, light-warm-gray,
        // light-yellow, none
        private fun trelloToAsanaColor(trelloColor: String) = when(trelloColor) {
            "purple" -> "dark-purple"
            "blue" -> "dark-blue"
            "red" -> "dark-red"
            "orange" -> "dark-orange"
            "yellow" -> "light-yellow"
            "green" -> "dark-green"
            "sky" -> "light-blue"
            "lime" -> "light-green"
            "pink" -> "light-pink"
            "black" -> "dark-warm-gray"
            else -> "none"
        }
    }
}
