package com.cs407.grouplab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameEditText: EditText = view.findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)
        val signInButton: Button = view.findViewById(R.id.signin_button)
        val registerButton: Button = view.findViewById(R.id.register_button)

        //declare the animation
        val ttb = AnimationUtils.loadAnimation(requireContext(), R.anim.ttb);
        val stb = AnimationUtils.loadAnimation(requireContext(), R.anim.stb);
        val btt = AnimationUtils.loadAnimation(requireContext(), R.anim.btt);
        val btt2 = AnimationUtils.loadAnimation(requireContext(), R.anim.btt2);
        val btt3 = AnimationUtils.loadAnimation(requireContext(), R.anim.btt3);
        val btt4 = AnimationUtils.loadAnimation(requireContext(), R.anim.btt4);

        val headertitle = view?.findViewById<TextView>(R.id.headertitle)
        val logo = view?.findViewById<ImageView>(R.id.logo)

        //set the animation
        headertitle?.startAnimation(ttb)
        logo?.startAnimation(stb)
        usernameEditText.startAnimation(btt)
        passwordEditText.startAnimation(btt2)
        signInButton.startAnimation(btt3)
        registerButton.startAnimation(btt4)

        // Set up SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Set up the sign-in button and its click listener
        signInButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            // Check if the username exists in SharedPreferences
            if (sharedPreferences.contains(username)) {
                val storedHashedPassword = sharedPreferences.getString(username, null)

                // Hash the entered password to compare
                val hashedPassword = hashPassword(password)
                if (storedHashedPassword == hashedPassword) {
                    // Correct username and password
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

                    with(sharedPreferences.edit()) {
                        putString("logged_in_username", username)
                        apply()
                    }

                    val fragment = AppHomePageFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment) // Replace with your container ID
                        .addToBackStack(null)
                        .commit()
                } else {
                    // Incorrect password
                    Toast.makeText(requireContext(), "Incorrect password!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Username does not exist
                Toast.makeText(requireContext(), "Username does not exist!", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the register button and its click listener
        registerButton.setOnClickListener {
            // Navigate to RegisterFragment with animation
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    // Function to hash the password using SHA-256
    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}
