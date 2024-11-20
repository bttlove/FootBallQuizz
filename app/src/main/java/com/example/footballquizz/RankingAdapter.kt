package com.example.footballquizz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(private val playerList: List<Pair<String, Double>>) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.player_name)
        val playerPoints: TextView = itemView.findViewById(R.id.points_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val player = playerList[position]
        holder.playerName.text = player.first
        holder.playerPoints.text = player.second.toString()
    }

    override fun getItemCount(): Int {
        return playerList.size
    }
}
