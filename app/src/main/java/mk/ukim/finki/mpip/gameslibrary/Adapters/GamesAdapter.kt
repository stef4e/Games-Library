package mk.ukim.finki.mpip.gameslibrary.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import mk.ukim.finki.mpip.gameslibrary.Models.Game
import mk.ukim.finki.mpip.gameslibrary.R

class GamesAdapter(private var gamesList: ArrayList<Game>, private val userId: String) : RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    private val database = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_item, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gamesList[position]
        holder.gameName.text = game.name
        holder.gameGenre.text = game.genre
        holder.gameYear.text = game.year
        holder.gamePlatforms.text = game.platforms
        Glide.with(holder.itemView.context).load(game.imageUrl).into(holder.gameImage)

        if(game.isInLibrary)
        {
            holder.addGameToLibraryButton.visibility = View.GONE
        }

        holder.addGameToLibraryButton.setOnClickListener {

            val userGamesReference = database.collection("users").document(userId).collection("games")
            userGamesReference.whereEqualTo("name", game.name)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        userGamesReference.add(game)
                            .addOnSuccessListener {
                                Toast.makeText(holder.itemView.context, "Game added successfully", Toast.LENGTH_SHORT).show()
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(holder.itemView.context, "Failed to add game: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(holder.itemView.context, "${game.name} already exists in your library!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context, "Failed to check game in library: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return gamesList.size
    }

    fun updateList(newList: ArrayList<Game>) {
        gamesList = newList
        notifyDataSetChanged()
    }

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameName: TextView = itemView.findViewById(R.id.gameName)
        val gameGenre: TextView = itemView.findViewById(R.id.gameGenre)
        val gamePlatforms: TextView = itemView.findViewById(R.id.gamePlatforms)
        val gameYear: TextView = itemView.findViewById(R.id.gameYear)
        val gameImage: ImageView = itemView.findViewById(R.id.gameImage)
        val addGameToLibraryButton: Button = itemView.findViewById(R.id.addGameButton)
    }
}
