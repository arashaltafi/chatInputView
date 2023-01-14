package com.arash.altafi.chatinputview.ext

import android.view.View
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun RecyclerView.syncScrollWithFab(fab: FloatingActionButton) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0 && fab.visibility === View.VISIBLE) {
                fab.hide()
            } else if (dy < 0 && fab.visibility !== View.VISIBLE) {
                fab.show()
            }
        }
    })
}

fun PagingDataAdapter<*, *>.pagingStates(listener: PagingStateListener) {

    addLoadStateListener { loadState ->

        if (loadState.refresh is LoadState.Error) {
            listener.onError((loadState.refresh as LoadState.Error).error)
            return@addLoadStateListener
        }

        val isEmpty =
            loadState.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    itemCount < 1
        "loadState: $loadState".logE("pagingStates")
        when {
            isEmpty -> listener.onEmpty()
            loadState.refresh is LoadState.Loading -> listener.onRefresh(false)
            loadState.append is LoadState.Loading -> listener.onAppend()
            else -> listener.onShowContent()
        }

    }

}

interface PagingStateListener {
    fun onEmpty() {

    }

    fun onAppend() {

    }

    /**
     * @param isFresh : when fresh data submitted ( page = 1)
     */
    fun onRefresh(isFresh: Boolean) {

    }

    fun onShowContent() {

    }

    fun onError(error: Throwable) {

    }
}