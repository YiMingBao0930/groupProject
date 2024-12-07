package com.cs407.grouplab

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.grouplab.data.AppDatabase
import com.cs407.grouplab.databinding.FragmentScanPageBinding
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanPageFragment : Fragment() {
    private var _binding: FragmentScanPageBinding? = null
    private val binding get() = _binding!!
    private val apiService = OpenFoodApiService.create()
    private lateinit var db: AppDatabase

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startScanner()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        handleScanResult(result)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanPageBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.scan_toolbar)
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
    }

    private fun setupClickListeners() {
        // FAB click listener for scanning
        binding.fab.setOnClickListener {
            checkCameraPermission()
        }

        // Manual search button click listener
        binding.searchButton.setOnClickListener {
            val barcode = binding.barcodeInput.text.toString()
            if (barcode.isNotEmpty()) {
                processBarcode(barcode)
            } else {
                Snackbar.make(binding.root, "Please enter a barcode", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startScanner()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                Snackbar.make(
                    binding.root,
                    "Camera permission is needed to scan barcodes",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Grant") {
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }.show()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun startScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Center the barcode in the camera")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }

    private fun handleScanResult(result: ScanIntentResult) {
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show()
        } else {
            processBarcode(result.contents)
        }
    }

    private fun processBarcode(barcode: String) {
        binding.textResult.text = "Searching for barcode: $barcode..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getProduct(barcode)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.product != null) {
                        val foodItem = response.body()?.product?.toFoodItem()
                        foodItem?.let {
                            // Insert into database
                            launch(Dispatchers.IO) {
                                db.foodItemDao().insert(it)
                            }
                            // Update UI
                            binding.textResult.text = """
                                Found: ${it.name}
                                Calories: ${it.calories} kcal
                                Protein: ${it.protein}g
                                Carbs: ${it.carbs}g
                                Fat: ${it.fat}g
                            """.trimIndent()
                            Snackbar.make(binding.root, "Food item added to database", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.textResult.text = "Product not found"
                        Snackbar.make(binding.root, "Product not found in database", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textResult.text = "Error: ${e.message}"
                    Snackbar.make(binding.root, "Error retrieving product data", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}