package pt.ipt.henri.realweatherapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import java.util.UUID

class MainActivity : AppCompatActivity() {

    val wList = mutableListOf<Weather>()
    val auxList = mutableListOf<Weather>()
    private val rCode = 1
    private val lCode = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        //list.adapter = ListAdapter(auxList)
        val btnInfo = this.findViewById<LinearLayout>(R.id.btnInfo)

        btnInfo.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun callCity (){
        var call =  API.create().getData("Lisbon")

        call.enqueue(object : Callback<Response> {
            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.e("onFailure error", call.request().url().toString())
                Log.e("onFailure error", t.message!!)
            }

            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                response.body()?.let {

                    //homeprogress1.visibility = ViewPager.GONE

//Gera um id aleatorio e coloca numa string
                    it.weather.forEach { art ->
                        art.id = UUID.randomUUID().toString()
                    }

                    wList.clear()
                    wList.addAll(it.weather)

                    auxList.clear()
                    auxList.addAll(it.weather)


                }
            }
        })
    }
}