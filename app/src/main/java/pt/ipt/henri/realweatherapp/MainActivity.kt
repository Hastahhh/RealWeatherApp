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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import java.util.Date
import java.util.Locale
import java.util.UUID


class MainActivity : AppCompatActivity(), LocationListener {

    private val locationPermissionCode = 101
    private val audioPermissionCode = 102
    private var isFavorite: Boolean = false
    private val favoriteCities = mutableSetOf<String>()
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
    private lateinit var btnMic: ImageView
    private lateinit var btnFav: ImageView
    private lateinit var btnStar: ImageView


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        btnMic = findViewById(R.id.btnMic)
        btnFav = findViewById(R.id.btnFav)
        btnStar = findViewById(R.id.btnStar)

        val city = "Lisboa"

        checkFavorite(city)
        callCity(city)

        val btnInfo = this.findViewById<LinearLayout>(R.id.btnInfo)

        btnInfo.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        btnGPS.setOnClickListener {
            getLocation()

        }

        btnSearch.setOnClickListener {
            if (searchCity.text.toString().isEmpty()) {
                Toast.makeText(this, "Por favor introduz uma cidade", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                checkFavorite(searchCity.text.toString().trim())
                callCity(searchCity.text.toString())
                var upperCaseCity = searchCity.text.toString().lowercase()
                address.text = upperCaseCity.substring(0,1).uppercase().plus(upperCaseCity.substring(1)).trim()
                hideKeyboard()
            }
        }
        searchCity.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                checkFavorite(searchCity.text.toString().trim())
                callCity(searchCity.text.toString())
                var upperCaseCity = searchCity.text.toString().lowercase()
                address.text = upperCaseCity.substring(0,1).uppercase().plus(upperCaseCity.substring(1)).trim()
                hideKeyboard()
                true
            } else {
                Toast.makeText(this, "Por favor introduz uma cidade", Toast.LENGTH_SHORT).show()
                false
            }
        }

        btnMic.setOnClickListener {
            checkAudioPermission()
        }

        btnFav.setOnClickListener {
            val intent = Intent(this, FavouritesActivity::class.java)
            startActivity(intent)
        }

        btnStar.setOnClickListener {
            toggleFavorites()
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

                }
            }
        })
    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchCity.windowToken, 0)
    }
    private fun toggleFavorites() {

        val currentCity = address.text.toString().lowercase().trim()
        isFavorite = favoriteCities.contains(currentCity)

        if (isFavorite) {
            btnStar.setImageResource(R.drawable.star)
            favoriteCities.remove(currentCity)
            Toast.makeText(this, "$currentCity foi removida dos favoritos", Toast.LENGTH_SHORT).show()
        } else {
            btnStar.setImageResource(R.drawable.starfull)
            favoriteCities.add(currentCity)
            Toast.makeText(this, "$currentCity foi adicionada aos favoritos", Toast.LENGTH_SHORT).show()
        }

        saveFavorites()
    }

    private fun saveFavorites() {
        val sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("favoriteCities", favoriteCities)
        editor.apply()
    }

    private fun loadFavorites() {
        val sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val savedFavorites = sharedPreferences.getStringSet("favoriteCities", setOf()) ?: setOf()
        favoriteCities.clear()
        favoriteCities.addAll(savedFavorites)
    }

    private fun checkFavorite(city: String) {
        loadFavorites()
        if (favoriteCities.contains(city.lowercase())) {
            btnStar.setImageResource(R.drawable.starfull)
            isFavorite = true
        } else {
            btnStar.setImageResource(R.drawable.star)
            isFavorite = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    //Função do professor
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            /*  Inicia o GPS, que vai autilizar a posição de 5 em 5 segundos,
            se a nova localização estiver pelo menos a 5 metros da última
            localização  */
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        // Apresenta o nome da cidade
        address.text = getCityName(location.latitude, location.longitude)

        if (address.text.isEmpty()) {
            Toast.makeText(this@MainActivity, "A cidade não foi encontrada", Toast.LENGTH_SHORT).show()
        }else{
            checkFavorite(address.text.toString().trim())
            callCity(address.text.toString())
        }
    }

    private fun getCityName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].locality ?: "A cidade não foi encontrada"
        } else {
            "A cidade não foi encontrada"
        }
    }

    //função do professor adaptada
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
                getLocation()
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == audioPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio Permission Granted", Toast.LENGTH_SHORT).show()
                speechToText()
            } else {
                Toast.makeText(this, "Audio Permission Denied", Toast.LENGTH_SHORT).show()
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
    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), audioPermissionCode)
        } else {
            speechToText()
        }
    }
    private fun speechToText() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val speechRecognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the city name")
        }

        // Set the RecognitionListener
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@MainActivity, "A escutar...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {
                // Can provide feedback that the speech input has started
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Can provide feedback on the volume level of the input speech
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Handle any buffer received
            }

            override fun onEndOfSpeech() {
                // Can provide feedback that the speech input has ended
            }

            override fun onError(error: Int) {
                val errorMessage = getErrorText(error)
                Toast.makeText(this@MainActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                Log.e("SpeechRecognizer", "Error recognizing speech: $errorMessage")
            }

            override fun onResults(results: Bundle?) {
                val recognizedText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                if (recognizedText != null) {
                    searchCity.setText(recognizedText)
                    address.setText(recognizedText)
                    checkFavorite(recognizedText.trim())
                    callCity(recognizedText)
                } else {
                    Toast.makeText(this@MainActivity, "No speech recognized, try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Handle partial recognition results if needed
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle any events during the recognition process
            }
        })

        // Start listening for speech input
        speechRecognizer.startListening(speechRecognitionIntent)
    }

    // Function to get a readable error message for the given error code
    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

}