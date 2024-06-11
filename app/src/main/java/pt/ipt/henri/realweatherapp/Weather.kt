package pt.ipt.henri.realweatherapp;

 data class Weather(
        var id: String = "",
        val main: String,
        val description: String,
        val icon: String
        )
