package com.example.footballquizz
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
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
                R.id.nav_ranking -> {
                    startActivity(Intent(this, AdminRankingActivity::class.java))
                    true
                }
                else -> false
            }
        }

        nextPageButton.setOnClickListener { loadNextPage() }
        prevPageButton.setOnClickListener { loadPreviousPage() }

        loadPlayerData()
    }

    private fun loadPlayerData() {
        val query = db.collection("auths")
            .orderBy("date-time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(pageSize.toLong())

        if (lastVisible != null) {
            query.startAfter(lastVisible)  // Chuyển sang trang tiếp theo
        }

        query.get()
            .addOnSuccessListener { result ->
                // Kiểm tra nếu không có dữ liệu, tắt nút Next
                if (result.isEmpty) {
                    nextPageButton.isEnabled = false
                    return@addOnSuccessListener
                }

                playerListLayout.removeAllViews()  // Xóa nội dung trang trước

                // Lặp qua các dữ liệu và thêm vào bảng
                for ((index, document) in result.withIndex()) {
                    val email = document.getString("email") ?: "No Email"
                    val dateTime = document.getString("date-time") ?: "Unknown"
                    val imageUrl = document.getString("image_url") ?: ""
                    val formattedDateTime = formatDateTime(dateTime)

                    val playerRow = TableRow(this)
                    val playerImageView = ImageView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, 150, 1f)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        adjustViewBounds = true

                        if (imageUrl.isNotEmpty()) {
                            val requestOptions = RequestOptions().circleCrop().override(150, 150)
                            Glide.with(this@PlayerManagementAdmin).load(imageUrl).apply(requestOptions).into(this)
                        }
                    }

                    val emailTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = email
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }
                    val dateTimeTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = formattedDateTime
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    }
                    val blockButton = Button(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = "Chặn"
                        setOnClickListener { showBlockDialog(email) }
                    }

                    playerRow.addView(playerImageView)
                    playerRow.addView(emailTextView)
                    playerRow.addView(dateTimeTextView)
                    playerRow.addView(blockButton)
                    playerListLayout.addView(playerRow)

                    if (index == result.size() - 1) {
                        lastVisible = document  // Ghi lại tài liệu cuối cùng của trang này
                    }
                }

                // Cập nhật trạng thái của các nút phân trang
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
                        // Vô hiệu hóa nút "Next" khi không còn dữ liệu
                        nextPageButton.isEnabled = false
                        Toast.makeText(this, "Không có dữ liệu tiếp theo.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    playerListLayout.removeAllViews()
                    for (document in result.documents) {
                        addPlayerRow(document)
                    }

                    // Cập nhật chỉ mục cho trang hiện tại
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
        if (currentPage <= 1) return  // Ngăn chặn chuyển về trang trước khi ở trang đầu tiên

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

                    // Cập nhật chỉ mục cho trang hiện tại
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
        nextPageButton.isEnabled = lastVisible != null  // Chỉ kích hoạt nếu có dữ liệu tiếp theo
    }


    private fun addPlayerRow(document: DocumentSnapshot) {
        val email = document.getString("email") ?: "No Email"
        val dateTime = document.getString("date-time") ?: "Unknown"
        val imageUrl = document.getString("image_url") ?: ""
        val formattedDateTime = formatDateTime(dateTime)

        val playerRow = TableRow(this).apply {
            setPadding(10, 10, 10, 10)  // Thêm padding cho toàn bộ hàng
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 8, 0, 8)  // Thêm margin giữa các hàng
            }
        }

        val playerImageView = ImageView(this).apply {
            layoutParams = TableRow.LayoutParams(0, 150, 1f)
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            setPadding(8, 8, 8, 8)  // Thêm padding cho ảnh

            if (imageUrl.isNotEmpty()) {
                val requestOptions = RequestOptions().circleCrop().override(150, 150)
                Glide.with(this@PlayerManagementAdmin).load(imageUrl).apply(requestOptions).into(this)
            }
        }

        val emailTextView = TextView(this).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            text = email
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(8, 8, 8, 8)  // Thêm padding cho TextView email
        }

        val dateTimeTextView = TextView(this).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            text = formattedDateTime
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(8, 8, 8, 8)  // Thêm padding cho TextView date-time
        }

        val blockButton = Button(this).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            text = "Chặn"
            setPadding(8, 8, 8, 8)  // Thêm padding cho nút chặn
            setOnClickListener { showBlockDialog(email) }
        }

        playerRow.addView(playerImageView)
        playerRow.addView(emailTextView)
        playerRow.addView(dateTimeTextView)
        playerRow.addView(blockButton)

        playerListLayout.addView(playerRow)
    }


    private fun showBlockDialog(email: String) {
        val options = arrayOf("Chặn 7 ngày", "Chặn 14 ngày")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn thời gian chặn cho $email")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> blockPlayer(email, 7)
                1 -> blockPlayer(email, 14)
            }
        }
        builder.show()
    }

    private fun blockPlayer(email: String, days: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val unblockDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        db.collection("login").document(email)
            .update("block_until", unblockDate)
            .addOnSuccessListener {
                Toast.makeText(this, "Người chơi $email đã bị chặn trong $days ngày.", Toast.LENGTH_SHORT).show()
                updateBlockButton(email)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi chặn người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBlockButton(email: String) {
        for (i in 0 until playerListLayout.childCount) {
            val row = playerListLayout.getChildAt(i) as TableRow
            val emailTextView = row.getChildAt(0) as TextView
            val blockButton = row.getChildAt(3) as Button

            if (emailTextView.text == email) {
                blockButton.text = "Đã chặn"
                blockButton.isEnabled = false
                Toast.makeText(this, "$email đã bị chặn", Toast.LENGTH_SHORT).show()
                break
            }
        }
    }

    private fun formatDateTime(dateTime: String): String {
        return try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = originalFormat.parse(dateTime)
            val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            targetFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}