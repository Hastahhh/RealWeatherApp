package pt.ipt.henri.realweatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity(), LocationListener {

    private val wList = mutableListOf<Weather>()
    private val auxList = mutableListOf<Weather>()
    private val locationPermissionCode = 101
    private lateinit var locationManager: LocationManager

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
    private lateinit var btnGPS: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var searchCity: EditText

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        address = findViewById(R.id.address)
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
        btnGPS = findViewById(R.id.btnGPS)
        btnSearch = findViewById(R.id.searchIcon)
        searchCity = findViewById(R.id.searchCity)

        val city = "Lisboa"
        address.text = city
        callCity(city)

        //list.adapter = ListAdapter(auxList)
        val btnInfo = this.findViewById<LinearLayout>(R.id.btnInfo)


        btnInfo.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        btnGPS.setOnClickListener {
            getLocation()

        }

        btnSearch.setOnClickListener {
            callCity(searchCity.text.toString())
            var upperCaseCity = searchCity.text.toString().toLowerCase()
            address.text = upperCaseCity.substring(0,1).toUpperCase().plus(upperCaseCity.substring(1))
            var final = address.text
        }
    }

    private fun callCity (city: String){

        var call = API.create().getData(city)

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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            // Request location updates from the GPS provider
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        // Update the TextView with latitude and longitude
        address.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"

        // Get and display the city name
        address.text = getCityName(location.latitude, location.longitude)
        callCity(address.text.toString())
    }

    private fun getCityName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].locality ?: "City not found"
        } else {
            "City not found"
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                getLocation() // Request location updates again now that permission is granted
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun convertUnixToTime(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }
}