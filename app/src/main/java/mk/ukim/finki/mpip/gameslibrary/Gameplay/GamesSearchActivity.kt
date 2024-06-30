package mk.ukim.finki.mpip.gameslibrary.Gameplay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import mk.ukim.finki.mpip.gameslibrary.Adapters.GamesAdapter
import mk.ukim.finki.mpip.gameslibrary.BuildConfig
import mk.ukim.finki.mpip.gameslibrary.MainActivity
import mk.ukim.finki.mpip.gameslibrary.Models.Game
import mk.ukim.finki.mpip.gameslibrary.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GamesSearchActivity : AppCompatActivity() {

    private lateinit var gameNameText: TextInputEditText
    private lateinit var searchButton: Button
    private lateinit var logoutButton: Button
    private lateinit var goToLibraryButton: Button
    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var gamesAdapter: GamesAdapter
    private lateinit var gamesList: ArrayList<Game>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_games)

        gameNameText = findViewById(R.id.gameName)
        searchButton = findViewById(R.id.searchButton)
        logoutButton = findViewById(R.id.logoutButton)
        goToLibraryButton = findViewById(R.id.goToLibraryButton)
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView)

        firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser?.uid ?: ""

        gamesList = ArrayList()
        gamesAdapter = GamesAdapter(gamesList, userId)

        gamesRecyclerView.layoutManager = LinearLayoutManager(this)
        gamesRecyclerView.adapter = gamesAdapter

        searchButton.setOnClickListener { searchGames() }
        logoutButton.setOnClickListener { logout() }
        goToLibraryButton.setOnClickListener { goToLibrary() }
    }

    private fun goToLibrary() {
        startActivity(Intent(this@GamesSearchActivity, GamesLibraryActivity::class.java))
    }

    private fun searchGames() {
        val gameName = gameNameText.text.toString().trim()
        if (gameName.isNotEmpty()) {
            runOnUiThread {
                gamesList.clear()
                gamesAdapter.notifyDataSetChanged()
            }
            Thread {
                val apiKey = BuildConfig.api_key
                val request = "https://api.rawg.io/api/games?key=$apiKey&search=$gameName"

                try {
                    val url = URL(request)
                    val urlConnection = url.openConnection() as HttpURLConnection

                    try {
                        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                        val stringBuilder = StringBuilder()
                        var line: String?

                        while (bufferedReader.readLine().also { line = it } != null) {
                            stringBuilder.append(line).append("\n")
                        }

                        bufferedReader.close()

                        val response = stringBuilder.toString()
                        parsingResponse(response)
                    } finally {
                        urlConnection.disconnect()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun parsingResponse(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val games = jsonObject.getJSONArray("results")

            if (games.length() == 0) {
                runOnUiThread {
                    Toast.makeText(this@GamesSearchActivity, "No Result!", Toast.LENGTH_SHORT).show()
                }
            }


            for (i in 0 until games.length()) {
                val game = games.getJSONObject(i)

                val gameName = game.getString("name")

                val gameGenre = if (game.has("genres") && game.getJSONArray("genres").length() > 0) {
                    game.getJSONArray("genres").getJSONObject(0).getString("name")
                } else {
                    "Unknown"
                }

                val gamePlatforms = if (game.has("platforms") && game.getJSONArray("platforms").length() > 0) {
                    val platformsArray = game.getJSONArray("platforms")
                    val platformsList = mutableListOf<String>()
                    for (j in 0 until platformsArray.length()) {
                        platformsList.add(platformsArray.getJSONObject(j).getJSONObject("platform").getString("name"))
                    }
                    platformsList.joinToString(", ")
                } else {
                    "Unknown"
                }

                val gameYear = game.getString("released").split("-")[0]
                val gameImageURL = game.getString("background_image")
                gamesList.add(Game(gameName, gameGenre, gameYear, gamePlatforms, gameImageURL))
            }

            runOnUiThread {
                gamesAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this@GamesSearchActivity, "Log Out Successful.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
