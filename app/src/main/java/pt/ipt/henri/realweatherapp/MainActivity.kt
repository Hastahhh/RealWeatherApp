package pt.ipt.henri.realweatherapp

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Address
import retrofit2.Call
import retrofit2.Callback
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    val wList = mutableListOf<Weather>()
    val auxList = mutableListOf<Weather>()
    private val rCode = 1
    private val lCode = 2
    //private lateinit var city: String
    private lateinit var address: TextView
    private lateinit var status: TextView
    private lateinit var temp: TextView
    private lateinit var tempMin: TextView
    private lateinit var tempMax: TextView
    private lateinit var sunrise: TextView
    private lateinit var sunset: TextView
    private lateinit var wind: TextView
    private lateinit var pressure: TextView
    private lateinit var humidity: TextView
    private lateinit var todayHour: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        callCity()

        status = findViewById(R.id.status)
        temp = findViewById(R.id.temp)
        tempMin = findViewById(R.id.temp_min)
        tempMax = findViewById(R.id.temp_max)
        sunrise = findViewById(R.id.sunrise)
        sunset = findViewById(R.id.sunset)
        wind = findViewById(R.id.wind)
        pressure = findViewById(R.id.pressure)
        humidity = findViewById(R.id.humidity)
        todayHour = findViewById(R.id.updated_at)
        address = findViewById(R.id.address)

        //list.adapter = ListAdapter(auxList)
        val btnInfo = this.findViewById<LinearLayout>(R.id.btnInfo)

        btnInfo.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun callCity (){
        var call =  API.create().getData("Lisboa")

        call.enqueue(object : Callback<Response> {
            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.e("onFailure error", call.request().url().toString())
                Log.e("onFailure error", t.message!!)
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                response.body()?.let {

                    //homeprogress1.visibility = ViewPager.GONE

//Gera um id aleatorio e coloca numa string os valores da API
                    it.weather.forEach { art ->
                        art.id = UUID.randomUUID().toString()
                        status.text = art.description
                    }
                    tempMin.text = it.main.temp_min.replaceAfter(".", "").replace(".", "") + "°C"
                    tempMax.text = it.main.temp_max.replaceAfter(".", "").replace(".", "") + "°C"
                    temp.text = it.main.temp.replaceAfter(".", "").replace(".", "") + "°C"
                    wind.text = it.wind.speed
                    pressure.text = it.main.pressure
                    humidity.text = it.main.humidity
                    sunrise.text = convertUnixToTime(it.sys.sunrise.toLong())
                    sunset.text = convertUnixToTime(it.sys.sunset.toLong())
                    todayHour.text = getCurrentDateTime()




                    wList.clear()
                    wList.addAll(it.weather)

                    auxList.clear()
                    auxList.addAll(it.weather)


                }
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun convertUnixToTime(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }
}