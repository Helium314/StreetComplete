package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowBusiness(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o)
{
    override val tagFilters = """
        nodes, ways, relations with
        (
         shop and shop !~ no|vacant
         or craft
         or office
         or information = board
         or amenity = parking and parking = multi-storey
         or tourism = information and information = office
         or """.trimIndent() +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "wheelchair only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino",                                                                     // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",                    // civic
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",           // commercial
                "car_wash", "car_rental", "fuel",                                                                      // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health
                "animal_boarding", "animal_shelter", "animal_breeding",                                                // animals

                "boat_rental",
                "theatre",                             // culture
                "conference_centre", "arts_centre",    // events
                "police", "ranger_station",            // civic
                "ferry_terminal",                      // transport
                "place_of_worship",                    // religious
                "hospital",                            // health care
                "toilets",
                // name only
                "studio",                                                                // culture
                "events_venue", "exhibition_centre", "music_venue",                      // events
                "prison", "fire_station",                                                // civic
                "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
                "monastery",                                                             // religious
                "kindergarten", "school", "college", "university", "research_institute", // education
                "driving_school", "dive_centre", "language_school", "music_school",      // learning
                "brothel", "gambling", "love_hotel", "stripclub"                         // bad stuff
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum",

                // name & wheelchair
                "attraction",
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet", // accommodations

                // wheelchair only
                "viewpoint"

                // and tourism = information, see above
            ),
            "leisure" to arrayOf(
                // common
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon","escape_game",
                "sauna","trampoline_park",

                // name & wheelchair
                "sports_centre", "stadium", "marina",

                "horse_riding", "dance", "nature_reserve","pitch","playground"
            ),
            "landuse" to arrayOf(
                "cemetery", "allotments"
            ),
            "military" to arrayOf(
                "airfield", "barracks", "training_area"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") +
        "\n)"

    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_opening_hours
    override val dotColor = "orange"

    override fun getTitle(tags: Map<String, String>) =
        if (hasProperName(tags))
            R.string.quest_thisIsBusiness_name_title
        else
            R.string.quest_thisIsBusiness_no_name_title

    private fun hasProperName(tags: Map<String, String>?): Boolean =
        tags?.keys?.containsAny(listOf("name", "brand")) ?: false

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"] ?: featureName.value
        val name2 = featureName.value ?: tags.entries
        return if (name != null) arrayOf(name2.toString(),name) else arrayOf()
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
