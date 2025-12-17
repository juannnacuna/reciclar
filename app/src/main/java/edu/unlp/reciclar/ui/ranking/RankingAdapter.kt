package edu.unlp.reciclar.ui.ranking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.unlp.reciclar.R
import edu.unlp.reciclar.domain.model.RankingEntry

class RankingAdapter(private var rankingList: List<RankingEntry>) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val rankingEntry = rankingList[position]
        holder.bind(rankingEntry)
    }

    override fun getItemCount(): Int = rankingList.size

    fun updateData(newRankingList: List<RankingEntry>) {
        rankingList = newRankingList
        notifyDataSetChanged()
    }

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.tvUsername)
        private val pointsTextView: TextView = itemView.findViewById(R.id.tvPoints)

        fun bind(rankingEntry: RankingEntry) {
            usernameTextView.text = rankingEntry.username
            pointsTextView.text = rankingEntry.total_puntos.toString()
        }
    }
}
