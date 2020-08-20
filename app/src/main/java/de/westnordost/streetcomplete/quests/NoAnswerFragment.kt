package de.westnordost.streetcomplete.quests

class NoAnswerFragment : NoAAnswerFragment<Boolean>() {

    override fun onClick(answer: Boolean) { applyAnswer(answer) }
}
