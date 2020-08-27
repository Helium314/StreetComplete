package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
//import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowVacant(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {
    override val tagFilters = """
        nodes, ways, relations with
        shop = vacant
        or shop = no
        or disused:shop
        or disused:amenity
        or disused:office
    """
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_wheelchair_shop
    override val dotColor = "grey"

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsVacant_title

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
