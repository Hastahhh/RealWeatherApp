package pt.ipt.henri.realweatherapp

import CityAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavouritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CityAdapter
    private lateinit var backButton: ImageView
    private lateinit var favoritesTitle: TextView
    private val favoriteCities = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        loadFavorites()

        // Inicializa a Recycle view
        recyclerView = findViewById(R.id.listFav)
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton = findViewById(R.id.backButton)
        favoritesTitle = findViewById(R.id.favoritesTitle)

        adapter = CityAdapter(this, favoriteCities,
            onCityClicked = { city ->
                returnCityToMainActivity(city)
            },
            onCityRemoved = { city ->
                removeCityFromFavorites(city)
            }
        )

        recyclerView.adapter = adapter

        //Redireciona para a atividade main
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //Carrega as cidades favoritas do arquivo de preferences
    private fun loadFavorites() {
        val sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val savedFavorites = sharedPreferences.getStringSet("favoriteCities", setOf()) ?: setOf()
        favoriteCities.clear()
        favoriteCities.addAll(savedFavorites)
    }

    //Remove a cidade selecionada dos favoritos e atualiza o adapter
    private fun removeCityFromFavorites(city: String) {

        favoriteCities.remove(city)
        //Atualiza o adapter
        adapter.notifyDataSetChanged()

        val sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favoriteCitiesSet = sharedPreferences.getStringSet("favoriteCities", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        favoriteCitiesSet.remove(city)
        sharedPreferences.edit().putStringSet("favoriteCities", favoriteCitiesSet).apply()
    }

    //Devolve a cidade selecionada para a atividade main
    private fun returnCityToMainActivity(city: String) {
        val resultIntent = Intent().apply {
            putExtra("selectedCity", city)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}