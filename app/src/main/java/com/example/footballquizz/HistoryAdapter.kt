package com.example.footballquizz

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HistoryAdapter(context: Context, historyList: List<ScoreModel>) : ArrayAdapter<ScoreModel>(context, 0, historyList) {

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)

    val item = getItem(position)
    val tvPlayerName: TextView = view.findViewById(R.id.tvPlayerName)
    val tvScore: TextView = view.findViewById(R.id.tvScore)
    val tvDifficulty: TextView = view.findViewById(R.id.tvDifficulty)
    val tvTimeTaken: TextView = view.findViewById(R.id.tvTimeTaken)
    val tvTotalTime: TextView = view.findViewById(R.id.tvTotalTime)

    // Set values to TextViews
    tvPlayerName.text = item?.name
    tvScore.text = item?.score
    tvDifficulty.text = item?.difficulty
    tvTimeTaken.text = item?.timeTaken
    tvTotalTime.text = item?.time

    // Alternate background colors
    val backgroundColor = if (position % 2 == 0) Color.WHITE else Color.LTGRAY
    view.setBackgroundColor(backgroundColor)

    return view

  }
}

