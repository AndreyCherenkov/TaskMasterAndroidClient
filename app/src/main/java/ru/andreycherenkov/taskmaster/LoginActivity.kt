package ru.andreycherenkov.taskmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.andreycherenkov.taskmaster.api.dto.UserLoginDto
import ru.andreycherenkov.taskmaster.viewModel.UserViewModel

class LoginActivity : AppCompatActivity() {

    private val editTextUsername: EditText by lazy { findViewById(R.id.editTextUsername) }
    private val editTextPassword: EditText by lazy { findViewById(R.id.editTextPassword) }
    private val buttonLogin: Button by lazy { findViewById(R.id.buttonLogin) }

    private val userViewModel by lazy {
        UserViewModel(localRepository = LocalRepository(this)) //todo add constructor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userViewModel.authResult.observe(this) { result ->
            if (result.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        buttonLogin.setOnClickListener {
            val userLoginDto = UserLoginDto(
                username = editTextUsername.text.toString().trim(),
                password = editTextPassword.text.toString()
            )
            userViewModel.login(userLoginDto)
        }
    }
}