import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.henri.realweatherapp.R
import java.util.Locale

class CityAdapter(
    private val context: Context,
    private var cityList: MutableList<String>,
    private val onCityRemoved: (String) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_favourites, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val cityName = cityList[position]
        holder.cityName.text = cityName

        holder.btnStar.setOnClickListener {
            removeCityFromFavorites(cityName)
            onCityRemoved(cityName)
        }
    }

    override fun getItemCount(): Int = cityList.size

    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cityName: TextView = itemView.findViewById(R.id.cityName)
        val btnStar: ImageView = itemView.findViewById(R.id.btnStar)
    }

    private fun removeCityFromFavorites(city: String) {

        cityList.remove(city)
        notifyDataSetChanged()

        val sharedPreferences: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val favoriteCities = sharedPreferences.getStringSet("favoriteCities", mutableSetOf()) ?: mutableSetOf()
        favoriteCities.remove(city.lowercase(Locale.getDefault()))
        sharedPreferences.edit().putStringSet("favoriteCities", favoriteCities).apply()
    }
}
