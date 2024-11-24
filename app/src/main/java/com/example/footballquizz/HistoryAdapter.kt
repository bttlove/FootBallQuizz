package com.example.footballquizz

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

class HistoryAdapter(context: Context, private val historyList: MutableList<ScoreModel>) :
  ArrayAdapter<ScoreModel>(context, 0, historyList), Filterable {

  private var filteredHistoryList: MutableList<ScoreModel> = historyList.toMutableList()

  override fun getCount(): Int = filteredHistoryList.size

  override fun getItem(position: Int): ScoreModel? = filteredHistoryList[position]

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view =
      convertView ?: LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)

    val item = getItem(position)
    val tvPlayerName: TextView = view.findViewById(R.id.tvPlayerName)
    val tvScore: TextView = view.findViewById(R.id.tvScore)
    val tvDifficulty: TextView = view.findViewById(R.id.tvDifficulty)
    val tvTotalTime: TextView = view.findViewById(R.id.tvTotalTime)
    val tvDate: TextView = view.findViewById(R.id.tvDate)

    tvDate.text = item?.date
    tvPlayerName.text = item?.name
    tvScore.text = item?.score
    tvDifficulty.text = item?.difficulty
    tvTotalTime.text = item?.time

    // Set color for difficulty based on its value
    when (item?.difficulty) {
      "easy" -> tvDifficulty.setTextColor(Color.GREEN)
      "medium" -> tvDifficulty.setTextColor(Color.parseColor("#DEAA06"))
      "hard" -> tvDifficulty.setTextColor(Color.RED)
      "random" -> tvDifficulty.setTextColor(Color.MAGENTA)
      "VeryHard" -> tvDifficulty.setTextColor(Color.parseColor("#6B4226"))
      else -> tvDifficulty.setTextColor(Color.BLACK)
    }

    // Alternate background colors
    val backgroundColor = if (position % 2 == 0) Color.WHITE else Color.LTGRAY
    view.setBackgroundColor(backgroundColor)

    return view
  }

  override fun getFilter(): Filter {
    return object : Filter() {
      override fun performFiltering(constraint: CharSequence?): FilterResults {
        val query = constraint?.toString()?.lowercase() ?: ""

        val filteredList = if (query.isEmpty()) {
          historyList
        } else {
          historyList.filter {
            it.name.lowercase().contains(query) ||
                    it.score.lowercase().contains(query) ||
                    it.difficulty.lowercase().contains(query) ||
                    it.time.lowercase().contains(query) ||
                    it.date.lowercase().contains(query)
          }.toMutableList()
        }

        return FilterResults().apply { values = filteredList }
      }

      override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        filteredHistoryList = results?.values as MutableList<ScoreModel>
        notifyDataSetChanged()
      }
    }
  }

  // Method to sort the list based on criteria and order
  fun sortList(criteria: String, isAscending: Boolean) {
    when (criteria) {
      "name" -> filteredHistoryList.sortBy { it.name }
      "score" -> filteredHistoryList.sortByDescending { it.score.toIntOrNull() ?: 0 }
      "date" -> filteredHistoryList.sortByDescending { it.date }
      "time" -> filteredHistoryList.sortBy { it.time }
      "difficulty" -> filteredHistoryList.sortWith { o1, o2 ->
        val difficultyOrder = mapOf(
          "easy" to 1,
          "medium" to 2,
          "hard" to 3,
          "random" to 4
        )

        val difficulty1 = difficultyOrder[o1.difficulty] ?: 5
        val difficulty2 = difficultyOrder[o2.difficulty] ?: 5

        difficulty1.compareTo(difficulty2)
      }
    }

    // Reversed list if sorting in descending order
    if (!isAscending) {
      filteredHistoryList.reverse()
    }
  }
}
