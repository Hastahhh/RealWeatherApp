package pt.ipt.henri.realweatherapp;

import java.util.List;

data class Response(
     val weather: List<Weather>,
     val temp: Temp,
     val wind: Wind,
     val sys: Sys
)
