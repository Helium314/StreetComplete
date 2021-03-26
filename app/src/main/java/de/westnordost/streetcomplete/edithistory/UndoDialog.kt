package de.westnordost.streetcomplete.edithistory

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.*
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.*
import de.westnordost.streetcomplete.quests.getHtmlQuestTitle
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.setText
import kotlinx.coroutines.*
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.util.*
import java.util.concurrent.FutureTask
import javax.inject.Inject

class UndoDialog(
    context: Context,
    private val edit: Edit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    @Inject internal lateinit var mapDataSource: MapDataWithEditsSource
    @Inject internal lateinit var featureDictionaryFutureTask: FutureTask<FeatureDictionary>
    @Inject internal lateinit var editHistoryController: EditHistoryController

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        Injector.applicationComponent.inject(this)

        val resources = context.resources

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_undo, null, false)

        view.findViewById<ImageView>(R.id.icon).setImageResource(edit.icon)
        val overlayResId = edit.overlayIcon
        if (overlayResId != 0) view.findViewById<ImageView>(R.id.overlayIcon).setImageResource(overlayResId)
        view.findViewById<TextView>(R.id.createdTimeText).text = edit.getAgoString()
        view.findViewById<FrameLayout>(R.id.descriptionContainer).addView(edit.descriptionView)

        setTitle(R.string.undo_confirm_title2)
        setView(view)
        setButton(BUTTON_POSITIVE, resources.getText(R.string.undo_confirm_positive), null) { _, _ ->
            scope.launch(Dispatchers.IO) { editHistoryController.undo(edit) }
        }
        setButton(BUTTON_NEGATIVE, resources.getText(R.string.undo_confirm_negative), null, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scope.launch {
            findViewById<TextView>(R.id.titleText)!!.text = edit.getTitle()
        }
    }

    override fun dismiss() {
        super.dismiss()
        scope.cancel()
    }

    suspend fun Edit.getTitle(): CharSequence = when(this) {
        is ElementEdit -> {
            // TODO when the edit contains the original element the edit was made with, this wouldn't be necessary
            val element = withContext(Dispatchers.IO) { mapDataSource.get(elementType, elementId) }
            try {
                context.resources.getHtmlQuestTitle(questType, element, featureDictionaryFutureTask)
            } catch (e: MissingFormatArgumentException) {
                /* The exception happens when the number of format strings in the quest title
                 * differs from what can be "filled" by getHtmlQuestTitle. When does this happen?
                 * It happens the element is null or otherwise is not at all what is expected by
                 * that quest type.
                 * So, this is the fallback for that case */
                context.resources.getString(questType.title, *Array(10){"…"})
            }
        }
        is NoteEdit -> {
            context.resources.getText(when(action) {
                CREATE -> R.string.created_note_action_title
                COMMENT -> R.string.commented_note_action_title
            })
        }
        else -> throw IllegalArgumentException()
    }

    private fun Edit.getAgoString(): String {
        val minutesAgo = (System.currentTimeMillis() - createdTimestamp) / 1000 / 60
        return if (minutesAgo >= 60) {
            context.getString(R.string.edit_x_hours_ago, (minutesAgo / 60).toString())
        } else {
            context.getString(R.string.edit_x_minutes_ago, (minutesAgo).toString())
        }
    }

    private val Edit.descriptionView: View get() = when(this) {
        is ElementEdit -> {
            when(action) {
                is UpdateElementTagsAction -> createListOfTagUpdates(action.changes.changes)
                is DeletePoiNodeAction -> createTextView(ResText(R.string.deleted_poi_action_title))
                is SplitWayAction -> createTextView(ResText(R.string.split_way_action_title))
                else -> throw IllegalArgumentException()
            }
        }
        is NoteEdit -> {
            createTextView(text?.let { CharSequenceText(it) })
        }
        else -> throw IllegalArgumentException()
    }

    val Edit.overlayIcon: Int get() = when(this) {
        is ElementEdit -> {
            when(action) {
                is DeletePoiNodeAction -> R.drawable.ic_delete
                is SplitWayAction -> R.drawable.ic_scissors
                else -> 0
            }
        }
        else -> 0
    }

    private fun createTextView(text: Text?): TextView {
        val txt = TextView(context)
        txt.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        txt.setText(text)
        return txt
    }

    private fun createListOfTagUpdates(changes: List<StringMapEntryChange>): HtmlTextView {
        val txt = HtmlTextView(context)
        txt.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        txt.setHtml(changes.joinToString(separator = "", prefix = "<ul>", postfix = "</ul>") { change ->
           "<li>" +
           context.resources.getString(
               change.titleResId,
               "<tt>"+Html.escapeHtml(change.tagString)+"</tt>"
           ) +
           "</li>"
        })
        return txt
    }
}

private val StringMapEntryChange.tagString: String get() = when(this) {
    is StringMapEntryAdd -> "$key = $value"
    is StringMapEntryModify -> "$key = $value"
    is StringMapEntryDelete -> "$key = $valueBefore"
    else -> ""
}

private val StringMapEntryChange.titleResId: Int get() = when(this) {
    is StringMapEntryAdd -> R.string.added_tag_action_title
    is StringMapEntryModify -> R.string.changed_tag_action_title
    is StringMapEntryDelete -> R.string.removed_tag_action_title
    else -> 0
}