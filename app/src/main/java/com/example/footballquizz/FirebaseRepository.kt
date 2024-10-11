package com.example.footballquizz

import com.google.firebase.database.*

class FirebaseRepository {

  private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

  fun getPlayersData(callback: (List<QuizzModel>) -> Unit) {
    database.child("players").addValueEventListener(object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        val players = mutableListOf<QuizzModel>()
        for (playerSnapshot in snapshot.children) {
          val player = playerSnapshot.getValue(QuizzModel::class.java)
          player?.let { players.add(it) }
        }
        callback(players)  // Return the list of players to the callback
      }

      override fun onCancelled(error: DatabaseError) {
        // Handle error
      }
    })
  }
}
