package com.example.footballquizz

data class QuizzModel(
    val id : String,
    val title : String,
    val subtitle : String,
    val time : String,
){
    constructor() : this("","","","", )
}
