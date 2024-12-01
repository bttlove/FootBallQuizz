package com.example.footballquizz

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class PlayerManagementAdmin : AppCompatActivity() {

  private lateinit var searchPlayerManagementEditText: EditText
  private lateinit var addPlayerManagementButton: Button
  private lateinit var playerListLayout: LinearLayout
  private val db = FirebaseFirestore.getInstance()
  private lateinit var nextPageButton: Button
  private lateinit var prevPageButton: Button
  private lateinit var pageNumberTextView: TextView
  private var currentPage = 1
  private var lastVisible: DocumentSnapshot? = null
  private var firstVisible: DocumentSnapshot? = null
  private val pageSize = 10

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_player_management_admin)

    searchPlayerManagementEditText = findViewById(R.id.searchPlayerManagementEditText)
    addPlayerManagementButton = findViewById(R.id.addPlayerManagementButton)
    playerListLayout = findViewById(R.id.player_list)
    nextPageButton = findViewById(R.id.nextPageButton)
    prevPageButton = findViewById(R.id.prevPageButton)
    pageNumberTextView = findViewById(R.id.pageNumberTextView)

    val bottomNavigation: BottomNavigationView = findViewById(R.id.admin_bottom_navigation)
    bottomNavigation.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.nav_home_admin -> {
          startActivity(Intent(this, AdminActivity::class.java))
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

    searchPlayerManagementEditText.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
        val searchText = s.toString()
        if (searchText.isNotEmpty()) {
          dynamicSearchPlayerByEmail(searchText)
        } else {
          loadPlayerData()
        }
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })

    addPlayerManagementButton.setOnClickListener {
      val email = searchPlayerManagementEditText.text.toString()
      if (email.isEmpty()) {
        loadPlayerData()
      } else {
        searchPlayerByEmail(email)
      }
    }

    nextPageButton.setOnClickListener { loadNextPage() }
    prevPageButton.setOnClickListener { loadPreviousPage() }
    loadPlayerData()
  }

  private fun searchPlayerByEmail(email: String) {
    db.collection("auths")
      .whereEqualTo("email", email)
      .get()
      .addOnSuccessListener { result ->
        playerListLayout.removeAllViews()

        if (result.isEmpty) {
          Toast.makeText(this, "Không tìm thấy người chơi với email $email", Toast.LENGTH_SHORT).show()
          return@addOnSuccessListener
        }

        for (document in result) {
          addPlayerRow(document)
        }

        prevPageButton.isEnabled = false
        nextPageButton.isEnabled = false
        pageNumberTextView.text = "Kết quả tìm kiếm"
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Lỗi khi tìm kiếm người chơi: $e", Toast.LENGTH_SHORT).show()
      }
  }

  private fun dynamicSearchPlayerByEmail(email: String) {
    db.collection("auths")
      .orderBy("email")
      .startAt(email)
      .endAt(email + "\uf8ff")
      .limit(pageSize.toLong())
      .get()
      .addOnSuccessListener { result ->
        playerListLayout.removeAllViews()

        if (result.isEmpty) {
          Toast.makeText(this, "Không tìm thấy người chơi với email chứa '$email'", Toast.LENGTH_SHORT).show()
          return@addOnSuccessListener
        }

        for (document in result) {
          addPlayerRow(document)
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Lỗi khi tìm kiếm động: $e", Toast.LENGTH_SHORT).show()
      }
  }

  private fun loadPlayerData() {
    val query = db.collection("auths")
      .orderBy("date-time", Query.Direction.DESCENDING)
      .limit(pageSize.toLong())
    if (lastVisible != null) {
      query.startAfter(lastVisible)
    }

    query.get()
      .addOnSuccessListener { result ->
        if (result.isEmpty) {
          nextPageButton.isEnabled = false
          return@addOnSuccessListener
        }

        playerListLayout.removeAllViews()

        for ((index, document) in result.withIndex()) {
          addPlayerRow(document)
          if (index == result.size() - 1) {
            lastVisible = document
          }
        }

        prevPageButton.isEnabled = currentPage > 1
        nextPageButton.isEnabled = result.size() == pageSize
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Không thể tải dữ liệu người chơi: $e", Toast.LENGTH_SHORT).show()
      }
  }

  private fun loadNextPage() {
    lastVisible?.let {
      db.collection("auths")
        .orderBy("date-time", Query.Direction.DESCENDING)
        .startAfter(it)
        .limit(pageSize.toLong())
        .get()
        .addOnSuccessListener { result ->
          if (result.isEmpty) {
            nextPageButton.isEnabled = false
            Toast.makeText(this, "Không có dữ liệu tiếp theo.", Toast.LENGTH_SHORT).show()
            return@addOnSuccessListener
          }

          playerListLayout.removeAllViews()
          for (document in result.documents) {
            addPlayerRow(document)
          }

          firstVisible = result.documents.firstOrNull()
          lastVisible = result.documents.lastOrNull()
          currentPage++
          updatePageButtons()
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, "Không thể tải trang tiếp theo: $e", Toast.LENGTH_SHORT).show()
        }
    }
  }

  private fun loadPreviousPage() {
    if (currentPage <= 1) return

    firstVisible?.let {
      db.collection("auths")
        .orderBy("date-time", Query.Direction.DESCENDING)
        .endBefore(it)
        .limitToLast(pageSize.toLong())
        .get()
        .addOnSuccessListener { result ->
          if (result.isEmpty) {
            prevPageButton.isEnabled = false
            Toast.makeText(this, "Không có dữ liệu trang trước.", Toast.LENGTH_SHORT).show()
            return@addOnSuccessListener
          }

          playerListLayout.removeAllViews()
          for (document in result.documents) {
            addPlayerRow(document)
          }

          firstVisible = result.documents.firstOrNull()
          lastVisible = result.documents.lastOrNull()
          currentPage--
          updatePageButtons()
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, "Không thể tải trang trước: $e", Toast.LENGTH_SHORT).show()
        }
    }
  }

  private fun updatePageButtons() {
    pageNumberTextView.text = "Page $currentPage"
    prevPageButton.isEnabled = currentPage > 1
    nextPageButton.isEnabled = lastVisible != null
  }

  private fun addPlayerRow(document: DocumentSnapshot) {
    val email = document.getString("email") ?: "No Email"
    val dateTime = document.getString("date-time") ?: "Unknown"
    val imageUrl = document.getString("image_url") ?: ""
    val formattedDateTime = formatDateTime(dateTime)
    val role = document.getString("role") ?: "unknown"

    // Tạo nút "Thay đổi trạng thái"
    val actionButton = Button(this).apply {
      layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
      text = if (role == "Banned") "Bỏ Cấm" else "Cấm"
      setPadding(4, 4, 4, 4)
      setOnClickListener {
        if (role == "Banned") {
          unbanPlayer(email)
        } else {
          showBanDialog(email) // Gọi hộp thoại cấm
        }
      }
    }

    // Tạo hàng thông tin người chơi
    val playerRow = TableRow(this).apply {
      setPadding(10, 5, 10, 5)
      layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
        setMargins(0, 4, 0, 4)
      }
    }

    // Hiển thị ảnh người chơi
    val playerImageView = ImageView(this).apply {
      layoutParams = TableRow.LayoutParams(0, 150, 1f)
      scaleType = ImageView.ScaleType.CENTER_CROP
      adjustViewBounds = true

      if (imageUrl.isNotEmpty()) {
        val requestOptions = RequestOptions().circleCrop().override(150, 150)
        Glide.with(this@PlayerManagementAdmin).load(imageUrl).apply(requestOptions).into(this)
      }

      setOnClickListener {
        val intent = Intent(this@PlayerManagementAdmin, Profile_AdminActivity::class.java)
        intent.putExtra("USER_EMAIL", email)
        startActivity(intent)
      }
    }

    // Hiển thị email và ngày tham gia
    val playerEmailTextView = TextView(this).apply {
      layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
      text = email
      textSize = 16f
      setPadding(4, 4, 4, 4)
    }

    val playerDateTextView = TextView(this).apply {
      layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
      text = formattedDateTime
      textSize = 16f
      setPadding(4, 4, 4, 4)
    }

    // Thêm các thành phần vào hàng
    playerRow.addView(playerImageView)
    playerRow.addView(playerEmailTextView)
    playerRow.addView(playerDateTextView)
    playerRow.addView(actionButton)

    // Thêm hàng vào danh sách
    playerListLayout.addView(playerRow)
  }

  private fun showBanDialog(email: String) {
    AlertDialog.Builder(this)
      .setMessage("Bạn có chắc chắn muốn cấm người chơi này không?")
      .setPositiveButton("Cấm") { _, _ -> showReasonDialog(email) } // Chuyển sang chọn lý do
      .setNegativeButton("Hủy", null)
      .create()
      .show()
  }

  private fun showReasonDialog(email: String) {
    val reasons = arrayOf("1. Spam", "2. Hình ảnh phản cảm", "3. Tên phản cảm", "Khác")
    AlertDialog.Builder(this)
      .setTitle("Chọn lý do cấm người chơi")
      .setSingleChoiceItems(reasons, -1) { dialog, which ->
        val reason = reasons[which]
        if (reason == "Khác") {
          showCustomReasonDialog(email)
        } else {
          dialog.dismiss()
          showDurationInputDialog(email, reason) // Gọi hộp thoại nhập số ngày
        }
      }
      .setNegativeButton("Hủy", null)
      .create()
      .show()
  }

  private fun showCustomReasonDialog(email: String) {
    val input = EditText(this).apply {
      hint = "Nhập lý do khác"
    }

    AlertDialog.Builder(this)
      .setTitle("Nhập lý do cấm")
      .setView(input)
      .setPositiveButton("Tiếp tục") { _, _ ->
        val customReason = input.text.toString()
        if (customReason.isNotEmpty()) {
          showDurationInputDialog(email, customReason) // Gọi hộp thoại nhập số ngày
        } else {
          Toast.makeText(this, "Lý do không được để trống", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .create()
      .show()
  }
  private fun showDurationInputDialog(email: String, reason: String) {
    val input = EditText(this).apply {
      inputType = android.text.InputType.TYPE_CLASS_NUMBER
      hint = "Nhập số ngày cấm"
    }

    AlertDialog.Builder(this)
      .setTitle("Nhập số ngày cấm")
      .setView(input)
      .setPositiveButton("Cấm") { _, _ ->
        val daysInput = input.text.toString()
        val days = daysInput.toIntOrNull()
        if (days != null && days > 0) {
          banPlayerWithDays(email, reason, days)
        } else {
          Toast.makeText(this, "Vui lòng nhập số ngày hợp lệ", Toast.LENGTH_SHORT).show()
        }
      }
      .setNegativeButton("Hủy", null)
      .create()
      .show()
  }





  private fun banPlayerWithDays(email: String, reason: String, days: Int) {
    db.collection("auths").whereEqualTo("email", email)
      .get()
      .addOnSuccessListener { result ->
        if (result.isEmpty) {
          Toast.makeText(this, "Không tìm thấy người dùng trong 'auths'", Toast.LENGTH_SHORT).show()
          return@addOnSuccessListener
        }

        for (document in result) {
          val role = document.getString("role") ?: "unknown"
          if (role == "user") {
            val documentId = document.id

            // Thời gian hiện tại
            val currentDate = Date()

            // Tính toán thời gian kết thúc
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, days)
            val banEndAt = calendar.time

            // Chuẩn bị dữ liệu cấm
            val banData = mapOf(
              "role" to "Banned",
              "banReason" to reason,
              "bannedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(currentDate),
              "banEndAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(banEndAt)
            )

            // Cập nhật vào collection "auths"
            db.collection("auths").document(documentId)
              .update(banData)
              .addOnSuccessListener {
                // Thêm dữ liệu cấm vào "user" với ID là email
                db.collection("user").document(email)
                  .set(banData) // Sử dụng `set` thay vì `add`
                  .addOnSuccessListener {
                    Toast.makeText(
                      this,
                      "Người chơi đã bị cấm với lý do: $reason. Lệnh cấm sẽ kết thúc vào: ${
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(banEndAt)
                      }",
                      Toast.LENGTH_LONG
                    ).show()
                    refreshPlayerList()
                  }
                  .addOnFailureListener { e ->
                    Toast.makeText(this, "Không thể thêm dữ liệu vào 'user': $e", Toast.LENGTH_SHORT).show()
                  }
              }
              .addOnFailureListener { e ->
                Toast.makeText(this, "Không thể cập nhật dữ liệu trong 'auths': $e", Toast.LENGTH_SHORT).show()
              }

          } else {
            Toast.makeText(this, "Không thể cấm tài khoản không phải người chơi", Toast.LENGTH_SHORT).show()
          }
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Lỗi khi tìm người chơi trong 'auths': $e", Toast.LENGTH_SHORT).show()
      }
  }
  private fun unbanPlayer(email: String) {
    db.collection("auths")
      .whereEqualTo("email", email)
      .get()
      .addOnSuccessListener { result ->
        if (result.isEmpty) {
          Toast.makeText(this, "Không tìm thấy người dùng trong 'auths'", Toast.LENGTH_SHORT).show()
          return@addOnSuccessListener
        }

        for (document in result) {
          val documentId = document.id
          val updates = mapOf(
            "role" to "user",
            "banReason" to null,
            "bannedAt" to null,
            "banEndAt" to null
          )

          // Cập nhật role và xóa các trường cấm trong 'auths'
          db.collection("auths").document(documentId)
            .update(updates)
            .addOnSuccessListener {
              // Xóa người dùng khỏi 'user'
              db.collection("user").document(email)
                .delete()
                .addOnSuccessListener {
                  Toast.makeText(this, "Đã bỏ cấm người chơi $email", Toast.LENGTH_SHORT).show()
                  refreshPlayerList()
                }
                .addOnFailureListener { e ->
                  Toast.makeText(this, "Lỗi khi xóa khỏi 'user': $e", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
              Toast.makeText(this, "Lỗi khi cập nhật trong 'auths': $e", Toast.LENGTH_SHORT).show()
            }
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Lỗi khi tìm người dùng trong 'auths': $e", Toast.LENGTH_SHORT).show()
      }
  }





  private fun formatDateTime(dateTime: String): String {
    return try {
      val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
      val date = format.parse(dateTime)
      val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
      outputFormat.format(date)
    } catch (e: Exception) {
      "Unknown Date"
    }
  }

  private fun refreshPlayerList() {
    loadPlayerData()
  }
}
