package io.github.teccheck.fastlyrics.ui.search

import androidx.recyclerview.selection.SelectionTracker

class SelectionPredicate : SelectionTracker.SelectionPredicate<Long>() {
    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean = false

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean = false

    override fun canSelectMultiple(): Boolean = false
}
