package com.cs407.square_up
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.change_password)
        val myUserID =intent.getIntExtra("USER_ID", 1)
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.changePassword)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val notifications= findViewById<CheckBox>(R.id.notifications)
        notifications.setOnClickListener {

        }
        val logout= findViewById<Button>(R.id.logout)
        val currentPassword= findViewById<EditText>(R.id.currentPassword)
        val newPassword= findViewById<EditText>(R.id.newPassword)
        val confirmPassword= findViewById<EditText>(R.id.confirmPassword)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        changePasswordButton.setOnClickListener {
//            val hashedPassword=hash(password.text.toString().trim())
            lifecycleScope.launch(Dispatchers.IO) {
                val currentPasswordInput = currentPassword.text.toString().trim()
                val newPasswordInput = newPassword.text.toString().trim()
                val confirmPasswordInput = confirmPassword.text.toString().trim()
                val user = userDao.getUserById(myUserID)
                val oldHashedPassword=hash(currentPasswordInput)

                if(newPasswordInput.isEmpty() ){
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "no new password entered", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(user?.password!=oldHashedPassword ){
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(newPasswordInput!=confirmPasswordInput){
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                }
                else if( (user != null) ){
                    val newHashedPassword = hash(newPasswordInput)
                    val updateUser = user.let { it1 -> User(userId = it1.userId, userName = user.userName, password = newHashedPassword, email = user.email) }
//                    userDao.insert(updateUser )
//                    userDao.delete(user)
                    userDao.update(updateUser)
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        logout.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}