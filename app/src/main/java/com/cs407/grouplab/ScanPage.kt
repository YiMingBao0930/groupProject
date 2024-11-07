package com.cs407.grouplab

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cs407.grouplab.databinding.ActivityMainBinding
import com.cs407.grouplab.databinding.ActivityScanPageBinding

class ScanPage : AppCompatActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if (isGranted){
                showCamera()
            }
            else{
               //Explain why you need permission
            }
        }

    private lateinit var binding: ActivityScanPageBinding

    private fun showCamera(){

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initBinding()
        initViews()


    }

    private fun initViews(){
        binding.fab.setOnClickListener{
            checkPermissionCamera(this)
        }
    }

    private fun checkPermissionCamera(contex: Context){
        if(ContextCompat.checkSelfPermission(contex, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            showCamera()
        }
        else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(contex, "CAMERA permission required", Toast.LENGTH_SHORT)
        }
        else{
            requestPermissionLauncher.launch((android.Manifest.permission.CAMERA))
        }
    }

    private fun initBinding(){
        binding = ActivityScanPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}