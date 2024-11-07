package com.cs407.grouplab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val usernameEditText: EditText = view.findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)
        val confirmPasswordEditText: EditText = view.findViewById(R.id.confirmPasswordEditText)
        val registerButton: Button = view.findViewById(R.id.registerButton)

        // Set up SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check if the username already exists
            if (sharedPreferences.contains(username)) {
                Snackbar.make(view, "Username already exists!", Snackbar.LENGTH_SHORT).show()

            } else if (password != confirmPassword) {
                // Check if passwords match
                Snackbar.make(view, "Passwords do not match!", Snackbar.LENGTH_SHORT).show()
            } else {
                // Hash the password
                val hashedPassword = hashPassword(password)

                // Save the new username and hashed password in SharedPreferences
                sharedPreferences.edit()
                    .putString(username, hashedPassword)
                    .apply()

                Snackbar.make(view, "Registration successful!", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button in the toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "" // Hide the toolbar title text
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back to the previous fragment
        }

        return view
    }

    // Function to hash the password using SHA-256
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
