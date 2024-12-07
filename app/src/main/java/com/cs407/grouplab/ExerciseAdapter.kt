import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.R
import com.cs407.grouplab.Exercise

class ExerciseAdapter(private val onClick: (Exercise) -> Unit) :
    ListAdapter<Exercise, ExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = getItem(position)
        holder.bind(exercise)
    }

    class ExerciseViewHolder(
        itemView: View,
        private val onClick: (Exercise) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exercise_name)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.exercise_calories)

        private var currentExercise: Exercise? = null

        init {
            itemView.setOnClickListener {
                currentExercise?.let(onClick)
            }
        }

        fun bind(exercise: Exercise) {
            currentExercise = exercise
            nameTextView.text = exercise.name
            caloriesTextView.text = "Workout MET Intensity: %.2f".format(exercise.metValue)
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}
