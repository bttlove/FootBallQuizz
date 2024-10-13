package com.example.footballquizz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ArrayAdapter

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

        tvName.text = "Tên: ${currentScore.name}"
        tvScore.text = "Điểm: ${currentScore.score}"
        tvDifficulty.text = "Chế độ: ${currentScore.difficulty}"
        tvTimeTaken.text = "Thời gian hoàn thành: ${currentScore.timeTaken} giây"
        tvTime.text = "Thời gian: ${currentScore.time}"

        return listItemView
    }
}
