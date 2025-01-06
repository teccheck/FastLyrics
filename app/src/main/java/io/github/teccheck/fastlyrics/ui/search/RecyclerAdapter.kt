package io.github.teccheck.fastlyrics.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.utils.Utils

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var searchResults: List<SearchResult> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageArt: ImageView = view.findViewById(R.id.image_song_art)
        private val textTitle: TextView = view.findViewById(R.id.text_song_title)
        private val textArtist: TextView = view.findViewById(R.id.text_song_artist)
        private val providerIcon: ImageView = view.findViewById(R.id.provider_icon)

        private var searchResult: SearchResult? = null

        fun bind(searchResult: SearchResult) {
            this.searchResult = searchResult
            textTitle.text = searchResult.title
            textArtist.text = searchResult.artist
            Picasso.get().load(searchResult.artUrl).into(imageArt)
            providerIcon.setImageResource(Utils.getProviderIconRes(searchResult.provider))
        }

        fun getSearchResult() = searchResult
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_song, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val searchResult = searchResults[position]
        viewHolder.bind(searchResult)
    }

    override fun getItemCount() = searchResults.size

    override fun getItemId(position: Int) = position.toLong()

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchResults(searchResults: List<SearchResult>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
    }
}
