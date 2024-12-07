import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.cs407.grouplab.FoodItem
import com.cs407.grouplab.R
import com.cs407.grouplab.UserNutritionLog
import com.cs407.grouplab.data.AppDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddToLogDialogFragment(private val foodItem: FoodItem) : DialogFragment() {

    private lateinit var servingInput: EditText
    private lateinit var confirmButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_to_log, container, false)

        val foodNameText: TextView = view.findViewById(R.id.food_name_text)

        servingInput = view.findViewById(R.id.serving_input) // Renamed for consistency
        confirmButton = view.findViewById(R.id.confirm_button)
        cancelButton = view.findViewById(R.id.cancel_button)


        foodNameText.text = foodItem.name

        confirmButton.setOnClickListener { addToLog() }
        cancelButton.setOnClickListener { dismiss() }

        return view
    }

    override fun onStart() {
        super.onStart()

        // Set dialog width and height to a larger size
        val dialog = dialog ?: return
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, // Width matches the parent
            ViewGroup.LayoutParams.WRAP_CONTENT // Height adjusts to content
        )
    }

    private fun addToLog() {
        val servings = servingInput.text.toString().toIntOrNull()
        if (servings == null || servings <= 0) {
            Snackbar.make(requireView(), "Invalid serving size", Snackbar.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null)

        if (username.isNullOrEmpty()) {
            Snackbar.make(requireView(), "No user logged in!", Snackbar.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(requireContext())

        // Get the current date in "YYYY-MM-DD" format
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val logEntry = UserNutritionLog(
            username = username, // Replace with actual user info
            date = currentDate, // Use the dynamically retrieved current date
            foodItemId = foodItem.id,
            quantity = servings // "quantity" is still used here as it's the field name in the database
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userNutritionLogDao().insert(logEntry)
                withContext(Dispatchers.Main) {
                    Snackbar.make(requireView(), "Food added to log!", Snackbar.LENGTH_SHORT).show()
                    dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(requireView(), "Error adding food to log", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
