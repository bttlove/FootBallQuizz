package com.example.footballquizz

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

  // Create a new view holder for each player item
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_player, parent, false)
    return PlayerViewHolder(view)
  }

  // Bind the player data to the view holder
  override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
    val player = playerList[position]
    holder.bind(player)

    // Set the click listener for the item view
    holder.itemView.setOnClickListener { onPlayerClick(player) }

    // Load the player's image using Glide
    Glide.with(holder.itemView.context)
      .load(player.imageUrl)  // The image URL from Firebase Storage
      .into(holder.playerImageView)
  }

  // Return the size of the player list
  override fun getItemCount(): Int = playerList.size

  // Update the player list when data changes
  fun updateData(newPlayerList: List<QuizzModel>) {
    playerList = newPlayerList
    notifyDataSetChanged()  // Notify the adapter that the data has changed
  }

  // ViewHolder class to hold references to views in the item layout
  inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val playerImageView: ImageView = itemView.findViewById(R.id.playerImageView)
    private val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
    private val playerYearTextView: TextView = itemView.findViewById(R.id.playerYearTextView)
    private val playerClubTextView: TextView = itemView.findViewById(R.id.playerClubTextView)

    // Bind player data to the views in the item layout
    fun bind(player: QuizzModel) {
      playerNameTextView.text = player.name
      playerYearTextView.text = player.yearOfBirth.toString()
      playerClubTextView.text = player.club

      // Load the player's image using Glide
      Glide.with(itemView.context)
        .load(player.imageUrl)
        .into(playerImageView)
    }

  }
}
