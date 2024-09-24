package com.example.footballquizz

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.footballquizz.databinding.QuizItemRecyclerRowBinding

class QuizzListAdapter(private val quizzModelList :List<QuizzModel>):
    RecyclerView.Adapter<QuizzListAdapter.MyViewHolder>() {
    class MyViewHolder(private val binding: QuizItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bin(model: QuizzModel){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}