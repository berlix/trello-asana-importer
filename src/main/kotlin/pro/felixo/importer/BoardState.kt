package pro.felixo.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import java.net.URL
import java.time.Instant

@Serializable
data class BoardState(
    val id: String,
    val name: String,
    val desc: String,
    val closed: Boolean,
    val shortLink: String,
    val starred: Boolean,
    @Serializable(with = UrlSerializer::class) val url: URL,
    @Serializable(with = UrlSerializer::class) val shortUrl: URL,
    val labelNames: Map<String, String>,
    val actions: List<Action>, // reverse-chronologically ordered
    val cards: List<CardState>,
    val lists: List<ListState>,
    val checklists: List<ChecklistState>,
    val labels: List<LabelState>
)

@Serializable
enum class ActionType {
    @SerialName("createBoard") CreateBoard,
    @SerialName("updateBoard") UpdateBoard,
    @SerialName("createList") CreateList,
    @SerialName("updateList") UpdateList,
    @SerialName("createCard") CreateCard,
    @SerialName("updateCard") UpdateCard,
    @SerialName("copyCard") CopyCard,
    @SerialName("commentCard") CommentCard,
    @SerialName("addAttachmentToCard") AddAttachmentToCard,
    @SerialName("deleteAttachmentFromCard") DeleteAttachmentFromCard,
    @SerialName("updateCheckItemStateOnCard") UpdateCheckItemStateOnCard,
    @SerialName("deleteCard") DeleteCard,
    @SerialName("addChecklistToCard") AddChecklistToCard,
    @SerialName("moveCardFromBoard") MoveCardFromBoard,
    @SerialName("moveCardToBoard") MoveCardToBoard,
    @SerialName("addToOrganizationBoard") AddToOrganizationBoard,
    @SerialName("addMemberToBoard") AddMemberToBoard,
    @SerialName("addMemberToCard") AddMemberToCard,
    @SerialName("removeMemberFromCard") RemoveMemberFromCard
}

@Serializable
data class Action(
    val id: String,
    val type: ActionType,
    @Serializable(with = IsoInstantSerializer::class) val date: Instant,
    val data: JsonElement
) {
    val commentCardData: CommentCardData? = (type == ActionType.CommentCard).then { json.decodeFromJsonElement(data) }
}

@Serializable
data class CommentCardData(
    val text: String,
    val card: CardReference,
)

@Serializable
data class CardReference(
    val id: String,
    val name: String,
    val shortLink: String
)

@Serializable
data class ListState(
    val id: String,
    val pos: Double, // increasing left to right
    val name: String,
    val closed: Boolean
)

@Serializable
data class CardState(
    val id: String,
    val pos: Double, // increasing from top to bottom
    val name: String,
    val desc: String,
    @Serializable(with = IsoInstantSerializer::class) val due: Instant? = null,
    val closed: Boolean,
    val idList: String,
    val idChecklists: List<String>,
    val idLabels: List<String>,
    @Serializable(with = UrlSerializer::class) val shortUrl: URL,
    val shortLink: String,
    @Serializable(with = UrlSerializer::class) val url: URL,
    val attachments: List<Attachment>
)

@Serializable
data class LabelState(
    val id: String,
    val name: String,
    val color: String
)

@Serializable
data class ChecklistState(
    val id: String,
    val pos: Double,
    val name: String,
    val idCard: String,
    val checkItems: List<CheckItemState>
)

@Serializable
data class CheckItemState(
    val id: String,
    val pos: Double, // increasing from top to bottom
    val name: String,
    val state: CompletionState,
)

@Serializable
enum class CompletionState {
    @SerialName("complete") Complete,
    @SerialName("incomplete") Incomplete
}

@Serializable
data class Attachment(
    val id: String,
    val bytes: Long,
    @Serializable(with = IsoInstantSerializer::class) val date: Instant,
    val mimeType: String? = null,
    val name: String,
    @Serializable(with = UrlSerializer::class) val url: URL,
    val pos: Double,
    val fileName: String
)
