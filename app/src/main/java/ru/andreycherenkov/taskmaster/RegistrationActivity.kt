package ru.andreycherenkov.taskmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.andreycherenkov.taskmaster.api.dto.UserCreateDto
import ru.andreycherenkov.taskmaster.viewModel.UserViewModel


class RegistrationActivity : AppCompatActivity() {

    private val usernameEditText: EditText by lazy { findViewById(R.id.username) }
    private val passwordEditText: EditText by lazy { findViewById(R.id.password) }
    private val confirmPasswordEditText: EditText by lazy { findViewById(R.id.confirm_password) }
    private val emailEditText: EditText by lazy { findViewById(R.id.email) }
    private val registerButton: Button by lazy { findViewById(R.id.register_button) }

    private val userViewModel by lazy {
        UserViewModel(localRepository = LocalRepository(this)) //todo add Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        userViewModel.authResult.observe(this) { result ->
            if (result.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val userCreateDto = UserCreateDto(
                username = usernameEditText.text.toString(),
                email = emailEditText.text.toString(),
                password = passwordEditText.text.toString(),
                confirmedPassword = confirmPasswordEditText.text.toString()
            )
            userViewModel.createUser(userCreateDto)
        }
    }

}
