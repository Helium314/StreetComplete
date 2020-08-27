package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowMachine(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {
    override val tagFilters = """
        nodes, ways with
        amenity = vending_machine
        or amenity = atm
        or amenity = telephone
        or amenity = charging_station
        or atm = yes and (amenity or shop)
    """
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_cash
    override val dotColor = "blue"

    override fun getTitle(tags: Map<String, String>) =
        if (tags["amenity"].equals("atm") || !tags["atm"].isNullOrEmpty())
            R.string.quest_thisIsAtm_title
        else if (tags["amenity"].equals("telephone"))
            R.string.quest_thisIsTelephone_title
        else if (tags["amenity"].equals("charging_station"))
            R.string.quest_thisIsCharging_title
        else
            R.string.quest_thisIsVendingMachine_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        return arrayOf(tags["vending"] ?: tags.entries.toString())
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
