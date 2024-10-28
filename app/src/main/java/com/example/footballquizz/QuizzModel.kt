package com.example.footballquizz

data class QuizzModel(

  val  name: String = "",
  val yearOfBirth: Int = 0,
  val club: String = "",
  val imageUrl: String = "",
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
