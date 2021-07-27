package be.bxl.moorluck.thisisachat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // View

    lateinit var etEmail: EditText
    lateinit var etPassword : EditText

    lateinit var btnSignIn : Button
    lateinit var btnSignUp : Button

    // Firebase

    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // View

        etEmail = findViewById(R.id.et_e_mail_main_activity)
        etPassword = findViewById(R.id.et_password_main_activity)

        btnSignIn = findViewById(R.id.btn_signin_main_activity)
        btnSignUp = findViewById(R.id.btn_signup_main_activity)

        // Firebase

        auth = Firebase.auth

        // On Click

        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            auth.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "You signed in successfully !", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, RoomActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
        }
    }
}