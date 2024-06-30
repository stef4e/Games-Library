package mk.ukim.finki.mpip.gameslibrary.Gameplay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mk.ukim.finki.mpip.gameslibrary.Adapters.GamesAdapter
import mk.ukim.finki.mpip.gameslibrary.MainActivity
import mk.ukim.finki.mpip.gameslibrary.Models.Game
import mk.ukim.finki.mpip.gameslibrary.R

class GamesLibraryActivity : AppCompatActivity() {

    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var logoutButton: Button
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var gamesList: ArrayList<Game>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_library)

        gamesRecyclerView = findViewById(R.id.gamesRecyclerView)
        backButton = findViewById(R.id.backButton)
        logoutButton = findViewById(R.id.logoutButton)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userId = firebaseAuth.currentUser?.uid ?: ""

        gamesList = ArrayList()
        gamesAdapter = GamesAdapter(gamesList, userId)

        gamesRecyclerView.layoutManager = LinearLayoutManager(this)
        gamesRecyclerView.adapter = gamesAdapter

        loadUserGames()

        backButton.setOnClickListener { back() }
        logoutButton.setOnClickListener {  logout() }
    }

    private fun back() {
        startActivity(Intent(this@GamesLibraryActivity, GamesSearchActivity::class.java))
    }

    private fun logout() {
        firebaseAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this@GamesLibraryActivity, "Log Out Successful.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun loadUserGames() {
        firestore.collection("users").document(userId).collection("games")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val game = document.toObject(Game::class.java)
                    game.isInLibrary=true
                    gamesList.add(game)
                }
                if (documents.isEmpty)
                {
                    Toast.makeText(this@GamesLibraryActivity, "Your Library is Empty!", Toast.LENGTH_LONG).show()
                }
                gamesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}
