package mk.ukim.finki.mpip.gameslibrary.AuthActivities

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

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var signupButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        emailText = findViewById(R.id.emailText)
        passwordText = findViewById(R.id.passwordText)
        signupButton = findViewById(R.id.signupButton)

        signupButton.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this@SignUpActivity, "Please fill out both fields.", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        createUserDocument(user.uid, email, password)
                        successfulSignUp(user)
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "Registration failed.", Toast.LENGTH_SHORT).show()
                    successfulSignUp(null)
                }
            }
    }

    private fun createUserDocument(userId: String, email: String, password: String) {
        val userMap = hashMapOf(
            "email" to email,
            "password" to password,
            "role" to "user"
        )

        database.collection("users").document(userId)
            .set(userMap)
            .addOnFailureListener { e ->
                Toast.makeText(this@SignUpActivity, "Failed to create user document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun successfulSignUp(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this@SignUpActivity, GamesSearchActivity::class.java))
            Toast.makeText(this@SignUpActivity, "Sign Up Successful.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
