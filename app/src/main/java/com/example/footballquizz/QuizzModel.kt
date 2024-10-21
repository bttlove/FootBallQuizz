package com.example.footballquizz

data class QuizzModel(

  val Name: String = "",
  val yearOfBirth: Int = 0,
  val club: String = "",
  val imageUrl: String = "",
  var id: String? = null
){


  fun getEasyQuestion(): String {
         return "Tên cầu thủ trong ảnh là gì?"
     }
     fun getMediumQuestion(): String {
         return "Cầu thủ $Name hiện đang chơi cho câu lạc bộ nào?"
     }
     fun getHardQuestion(): String {
         return "Cầu thủ $Name sinh vào năm nào?"
     }
     fun checkAnswer(answer: String, level: String): Boolean {
         return when (level.toLowerCase()) {
             "easy" -> answer.equals(Name, ignoreCase = true)
             "medium" -> answer.equals(club, ignoreCase = true)
             "hard" -> answer == yearOfBirth.toString()
             else -> false
         }
     }
 }
