package com.example.footballquizz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlayerAdapter(
  private var playerList: List<QuizzModel>,
  private val onPlayerClick: (QuizzModel) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_player, parent, false)
    return PlayerViewHolder(view)
  }

  override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
    val player = playerList[position]
    holder.bind(player)

    holder.itemView.setOnClickListener { onPlayerClick(player) }
  }

  override fun getItemCount(): Int = playerList.size

  fun updateData(newPlayerList: List<QuizzModel>) {
    playerList = newPlayerList
    notifyDataSetChanged()
  }

  inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val playerImageView: ImageView = itemView.findViewById(R.id.playerImageView)
    private val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
    private val playerYearTextView: TextView = itemView.findViewById(R.id.playerYearTextView)
    private val playerClubTextView: TextView = itemView.findViewById(R.id.playerClubTextView)

    fun bind(player: QuizzModel) {
      playerNameTextView.text = player.Name
      playerYearTextView.text = player.yearOfBirth.toString()
      playerClubTextView.text = player.club

      // Load image using Glide or any other image loading library
      Glide.with(itemView.context)
        .load(player.imageUrl)
        .into(playerImageView)
    }
  }
}
