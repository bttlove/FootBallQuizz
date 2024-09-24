package com.example.footballquizz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.footballquizz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var quizzModelList : MutableList<QuizzModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quizzModelList = mutableListOf()
        getDataFromFirebase()
    }
    private  fun setupRecyclerView(){

    }
    private fun getDataFromFirebase(){
        quizzModelList.add(QuizzModel("1","Thinh 1","My Kym 1","10"))
        quizzModelList.add(QuizzModel("1","Thinh 2","My Kym 2","20"))
        quizzModelList.add(QuizzModel("1","Thinh 3","My Kym 3","30"))
    }
}