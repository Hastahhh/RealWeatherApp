package pt.ipt.henri.realweatherapp

import CityAdapter
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavouritesActivity : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CityAdapter
    private val favoriteCities = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        loadFavorites()


        recyclerView = findViewById(R.id.listFav)
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = CityAdapter(this, favoriteCities) { city ->
            favoriteCities.remove(city)
            adapter.notifyDataSetChanged()
        }

        recyclerView.adapter = adapter
    }

    private fun loadFavorites() {
        val sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val savedFavorites = sharedPreferences.getStringSet("favoriteCities", setOf()) ?: setOf()
        favoriteCities.clear()
        favoriteCities.addAll(savedFavorites)
    }

}