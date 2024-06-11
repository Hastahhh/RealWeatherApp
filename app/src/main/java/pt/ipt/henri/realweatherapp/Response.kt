package pt.ipt.henri.realweatherapp;

data class Response(
     val weather: List<Weather>,
     val main: Main,
     val wind: Wind,
     val sys: Sys
)
