package be.bxl.moorluck.thisisachat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // View

    lateinit var etEmail: EditText
    lateinit var etPassword : EditText

    lateinit var btnSignIn : Button
    lateinit var btnSignUp : Button

    // Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.et_e_mail_main_activity)
        etPassword = findViewById(R.id.et_password_main_activity)

        btnSignIn = findViewById(R.id.btn_signin_main_activity)
        btnSignUp = findViewById(R.id.btn_signup_main_activity)

        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            Toast.makeText(this, "Signed in !", Toast.LENGTH_LONG).show()
        }
    }
}