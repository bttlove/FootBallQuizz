package com.example.footballquizz

data class QuizzModel(

  val  name: String = "",
  val yearOfBirth: Int = 0,
  val club: String = "",
  val imageUrl: String = "",
  var id: String? = null
){

/*
  fun getEasyQuestion(): String {
         return "Tên cầu thủ trong ảnh là gì?"
     }
     fun getMediumQuestion(): String {
         return "Cầu thủ $name hiện đang chơi cho câu lạc bộ nào?"
     }
     fun getHardQuestion(): String {
         return "Cầu thủ $name sinh vào năm nào?"
     }*/
     fun checkAnswer(answer: String, level: String): Boolean {
         return when (level.toLowerCase()) {
             "easy" -> answer.equals(name, ignoreCase = true)
             "medium" -> answer.equals(club, ignoreCase = true)
             "hard" -> answer == yearOfBirth.toString()
             else -> false
         }
     }
 }
