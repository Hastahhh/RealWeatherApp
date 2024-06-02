package pt.ipt.henri.realweatherapp;

import android.os.Parcelable
abstract class Weather(
        var id: String = "",
        val main: String,
        val description: String,
        val icon: String
        ): Parcelable
