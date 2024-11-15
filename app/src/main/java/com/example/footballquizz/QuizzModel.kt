package com.example.footballquizz

data class QuizzModel(

  var  name: String = "",
  var yearOfBirth: Int = 0,
  var club: String = "",
  var imageUrl: String = "",
  var id: String? = null,
){


     fun checkAnswer(answer: String, level: String): Boolean {
         return when (level.toLowerCase()) {
             "easy" -> answer.equals(name, ignoreCase = true)
             "medium" -> answer.equals(club, ignoreCase = true)
             "hard" -> answer == yearOfBirth.toString()
             else -> false
         }
     }
 }
