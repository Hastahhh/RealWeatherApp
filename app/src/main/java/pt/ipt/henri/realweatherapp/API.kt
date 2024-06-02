package pt.ipt.henri.realweatherapp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface API {
//https://api.openweathermap.org/data/2.5/weather?q=Lisbon&units=metric&lang=pt&appid=3064d7e8bfc22727461fc258451b5ad2

        @GET("data/2.5/weather")
        fun getData(
            @Query("q") query: String,  // Parâmetro de consulta "q"
            @Query("unit") unit: String = "metric",
            @Query("appid") apiKey: String = "3064d7e8bfc22727461fc258451b5ad2"
        ): Call<Response>

    companion object{
        fun create(): API {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl("http://api.openweathermap.org/")
                .build()


            return retrofit.create(API::class.java)
        }
    }
}
