package com.example.footballquizz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HistoryAdapter(context: Context, private val historyList: List<ScoreModel>) :
    ArrayAdapter<ScoreModel>(context, 0, historyList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
        }

        val currentScore = historyList[position]

        val tvName = listItemView!!.findViewById<TextView>(R.id.tvName)
        val tvScore = listItemView.findViewById<TextView>(R.id.tvScore)
        val tvDifficulty = listItemView.findViewById<TextView>(R.id.tvDifficulty)
        val tvTimeTaken = listItemView.findViewById<TextView>(R.id.tvTimeTaken)
        val tvTime = listItemView.findViewById<TextView>(R.id.tvTime)

        // Thiết lập màu chữ ngẫu nhiên cho mỗi phần tử
        val randomColor = getRandomColor()
        tvName.setTextColor(randomColor)
        tvScore.setTextColor(randomColor)
        tvDifficulty.setTextColor(randomColor)
        tvTimeTaken.setTextColor(randomColor)
        tvTime.setTextColor(randomColor)

        tvName.text = "Tên: ${currentScore.name}"
        tvScore.text = "Điểm: ${currentScore.score}"
        tvDifficulty.text = "Chế độ: ${currentScore.difficulty}"
        tvTimeTaken.text = "Thời gian hoàn thành: ${currentScore.timeTaken} giây"
        tvTime.text = "Thời gian: ${currentScore.time}"

        return listItemView
    }

    private fun getRandomColor(): Int {
        val colors = listOf(
            android.graphics.Color.RED,
            android.graphics.Color.WHITE,
            android.graphics.Color.GREEN,
            android.graphics.Color.YELLOW,
            android.graphics.Color.CYAN,
            android.graphics.Color.MAGENTA
        )
        return colors.random()
    }
}
