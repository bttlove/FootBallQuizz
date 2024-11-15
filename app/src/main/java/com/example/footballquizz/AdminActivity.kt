package com.example.footballquizz
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class AdminActivity : AppCompatActivity() {

  private lateinit var searchPlayerEditText: EditText
  private lateinit var playerAdapter: PlayerAdapter
  private var playerList: MutableList<QuizzModel> = mutableListOf()
  private var currentPage = 0
  private val playersPerPage = 8
  private lateinit var nextPageButton: Button
  private lateinit var prevPageButton: Button
  private lateinit var pageNumberTextView: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_admin)

    // Initialize UI components
    searchPlayerEditText = findViewById(R.id.searchPlayerEditText)
    nextPageButton = findViewById(R.id.nextPageButton)
    prevPageButton = findViewById(R.id.prevPageButton)
    pageNumberTextView = findViewById(R.id.pageNumberTextView)

    // Setup search functionality
    searchPlayerEditText.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: Editable?) {
        filterPlayers(s.toString())
      }
    })

    // Button to add player
    findViewById<Button>(R.id.addPlayerButton).setOnClickListener {
      showAddPlayerDialog()
    }

    setupRecyclerView()
    fetchPlayers()

    // Bottom Navigation
    val bottomNavigation: BottomNavigationView = findViewById(R.id.admin_bottom_navigation)
    bottomNavigation.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.nav_player_management -> {
          val intent = Intent(this, PlayerManagementAdmin::class.java)
          startActivity(intent)
          true
        }

        R.id.nav_ranking_admin -> {
          startActivity(Intent(this, AdminRankingActivity::class.java))
          true
        }
        R.id.nav_settings_admin -> {
          startActivity(Intent(this, AdminSettingActivity::class.java))
          true
        }

        else -> false
      }
    }

    // Pagination buttons
    nextPageButton.setOnClickListener {
      if (currentPage < (playerList.size / playersPerPage)) {
        currentPage++
        updatePlayerList()
      }
    }

    prevPageButton.setOnClickListener {
      if (currentPage > 0) {
        currentPage--
        updatePlayerList()
      }
    }

    // Update page number display
    updatePageNumber()
  }

  // Setup RecyclerView
  private fun setupRecyclerView() {
    val recyclerView: RecyclerView = findViewById(R.id.playersRecyclerView)
    recyclerView.layoutManager = LinearLayoutManager(this)
    playerAdapter = PlayerAdapter(mutableListOf(), this::showEditDeleteDialog)
    recyclerView.adapter = playerAdapter
  }

  // Fetch players from Firebase
  private fun fetchPlayers() {
    val database = FirebaseDatabase.getInstance().getReference("players")
    database.addValueEventListener(object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        playerList.clear()
        for (data in snapshot.children) {
          val player = data.getValue(QuizzModel::class.java)
          player?.let {
            // Assign ID from the database to the model
            it.id = data.key
            playerList.add(it)
          }
        }
        updatePlayerList() // Update the displayed player list after fetching
      }

      override fun onCancelled(error: DatabaseError) {
        Toast.makeText(this@AdminActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
      }
    })
  }

  // Update player list based on current page
  private fun updatePlayerList() {
    val startIndex = currentPage * playersPerPage
    val endIndex = minOf(startIndex + playersPerPage, playerList.size)

    // Show paginated list
    val paginatedList = playerList.subList(startIndex, endIndex)
    playerAdapter.updateData(paginatedList)

    // Enable or disable pagination buttons
    nextPageButton.isEnabled = endIndex < playerList.size
    prevPageButton.isEnabled = currentPage > 0

    // Update page number display
    updatePageNumber()
  }

  // Update page number display
  private fun updatePageNumber() {
    pageNumberTextView.text = "Page ${currentPage + 1}"
  }

  // Filter players by name or club
  private fun filterPlayers(query: String) {
    val filteredList = playerList.filter {
      it.name.contains(query, ignoreCase = true) || it.club.contains(query, ignoreCase = true)
    }
    playerAdapter.updateData(filteredList)
  }

  // Show dialog to add a new player
  private fun showAddPlayerDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_add_player, null)

    val playerNameEditText: EditText = dialogView.findViewById(R.id.etPlayerName)
    val playerClubEditText: EditText = dialogView.findViewById(R.id.etPlayerClub)
    val playerYearEditText: EditText = dialogView.findViewById(R.id.etPlayerYear)
    val playerImageUrlEditText: EditText = dialogView.findViewById(R.id.etPlayerImageUrl)

    AlertDialog.Builder(this)
      .setTitle("Thêm cầu thủ mới")
      .setView(dialogView)
      .setPositiveButton("Thêm") { _, _ ->
        val name = playerNameEditText.text.toString()
        val club = playerClubEditText.text.toString()
        val year = playerYearEditText.text.toString()
        val imageUrl = playerImageUrlEditText.text.toString()

        if (name.isNotEmpty() && club.isNotEmpty() && year.isNotEmpty() && imageUrl.isNotEmpty()) {
          addPlayerToFirebase(name, club, year, imageUrl)
        } else {
          Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  // Add player to Firebase
  private fun addPlayerToFirebase(name: String, club: String, yearOfBirth: String, imageUrl: String) {
    val database = FirebaseDatabase.getInstance().getReference("players")
    val playerId = database.push().key

    // Chỉ thêm các thuộc tính mà bạn đã khai báo trong QuizzModel
    val player = QuizzModel(
      name = name,
      yearOfBirth = yearOfBirth.toInt(), // Chuyển đổi năm sinh từ String sang Int
      club = club,
      imageUrl = imageUrl
    )

    playerId?.let {
      database.child(it).setValue(player)
        .addOnSuccessListener {
          Toast.makeText(this, "Player added successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
          Toast.makeText(this, "Failed to add player", Toast.LENGTH_SHORT).show()
        }
    }
  }

  // Show dialog to edit or delete a player
  private fun showEditDeleteDialog(player: QuizzModel) {
    val options = arrayOf("Sửa", "Xóa")
    AlertDialog.Builder(this)
      .setTitle("Tùy chọn")
      .setItems(options) { _, which ->
        when (which) {
          0 -> showEditPlayerDialog(player) // Edit player
          1 -> deletePlayer(player) // Delete player
        }
      }
      .show()
  }

  // Show dialog to edit player
  private fun showEditPlayerDialog(player: QuizzModel) {
    val dialogView = layoutInflater.inflate(R.layout.dialog_edit_player, null)

    val playerNameEditText: EditText = dialogView.findViewById(R.id.etPlayerName)
    val playerClubEditText: EditText = dialogView.findViewById(R.id.etPlayerClub)
    val playerYearEditText: EditText = dialogView.findViewById(R.id.etPlayerYear)
    val playerImageUrlEditText: EditText = dialogView.findViewById(R.id.etPlayerImageUrl)

    // Pre-fill the fields with the current player's data
    playerNameEditText.setText(player.name)
    playerClubEditText.setText(player.club)
    playerYearEditText.setText(player.yearOfBirth.toString())
    playerImageUrlEditText.setText(player.imageUrl)

    AlertDialog.Builder(this)
      .setTitle("Chỉnh sửa cầu thủ")
      .setView(dialogView)
      .setPositiveButton("Lưu") { _, _ ->
        val name = playerNameEditText.text.toString()
        val club = playerClubEditText.text.toString()
        val year = playerYearEditText.text.toString()
        val imageUrl = playerImageUrlEditText.text.toString()

        if (name.isNotEmpty() && club.isNotEmpty() && year.isNotEmpty() && imageUrl.isNotEmpty()) {
          updatePlayerInFirebase(player.id!!, name, club, year, imageUrl)
        } else {
          Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .show()
  }

  // Update player in Firebase
  private fun updatePlayerInFirebase(id: String, name: String, club: String, year: String, imageUrl: String) {
    val database = FirebaseDatabase.getInstance().getReference("players").child(id)


    val updatedPlayer = QuizzModel(
      name = name,
      yearOfBirth = year.toInt(),
      club = club,
      imageUrl = imageUrl
    )

    database.setValue(updatedPlayer)
      .addOnSuccessListener {
        Toast.makeText(this, "Cập nhật cầu thủ thành công!", Toast.LENGTH_SHORT).show()
        val index = playerList.indexOfFirst { it.id == id }
        if (index != -1) {
          playerList[index] = updatedPlayer
          playerAdapter.notifyItemChanged(index)
        }
      }
      .addOnFailureListener {
        Toast.makeText(this, "Cập nhật cầu thủ thất bại!", Toast.LENGTH_SHORT).show()
      }
  }

  // Delete player from Firebase
  private fun deletePlayer(player: QuizzModel) {
    val database = FirebaseDatabase.getInstance().getReference("players").child(player.id!!)
    database.removeValue()
      .addOnSuccessListener {
        Toast.makeText(this, "Xóa cầu thủ thành công!", Toast.LENGTH_SHORT).show()
      }
      .addOnFailureListener {
        Toast.makeText(this, "Xóa cầu thủ thất bại!", Toast.LENGTH_SHORT).show()
      }
  }
}
