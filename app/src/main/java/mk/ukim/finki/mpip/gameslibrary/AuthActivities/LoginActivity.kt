package mk.ukim.finki.mpip.gameslibrary.AuthActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import mk.ukim.finki.mpip.gameslibrary.Gameplay.GamesSearchActivity
import mk.ukim.finki.mpip.gameslibrary.R

class LoginActivity : AppCompatActivity() {

    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        emailText = findViewById(R.id.emailText)
        passwordText = findViewById(R.id.passwordText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)

        loginButton.setOnClickListener { loginUser() }

        signupButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loginUser() {
        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            Toast.makeText(this@LoginActivity, "Please fill out both fields.", Toast.LENGTH_SHORT).show()
            return
        } else if (TextUtils.isEmpty(email)) {
            emailText.error = "Email is required"
            return
        } else if (TextUtils.isEmpty(password)) {
            passwordText.error = "Password is required"
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        logInUser(user)
                    } else {
                        Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        emailText.setText("")
                        passwordText.setText("")
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    emailText.setText("")
                    passwordText.setText("")
                }
            }
    }

    private fun logInUser(user: FirebaseUser) {
        val userId = user.uid
        database.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    startGamesActivity()
                } else {
                    Toast.makeText(this@LoginActivity, "User document does not exist.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@LoginActivity, "Failed to retrieve user's document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startGamesActivity() {
        startActivity(Intent(this@LoginActivity, GamesSearchActivity::class.java))
        Toast.makeText(this@LoginActivity, "Log In Successful.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
