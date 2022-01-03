package com.example.myapplication.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.UpdateUiCallBack
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.service.StepService
import com.example.myapplication.utils.SharedPreferencesUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

            } else {

            }
        }
    private lateinit var sp: SharedPreferencesUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()
        initData()

    }

    private fun initData() {
        sp = SharedPreferencesUtils(this)
        val planWalk_QTY = sp.getParam("planWalk_QTY", "7000") as String
        binding.tvStepsTaken.setText(planWalk_QTY)
        setupService()
    }


    private var isBind = false

    private fun setupService() {
        val intent = Intent(this, StepService::class.java)
        isBind = bindService(intent, conn, BIND_AUTO_CREATE)
        startService(intent)
    }

    var conn: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val stepService: StepService? = (service as StepService.StepBinder).getService()
            val planWalk_QTY = sp.getParam("planWalk_QTY", "7000") as String
            stepService?.getStepCount()?.let { binding.tvStepsTaken.setText(it.toString()) }

            stepService?.registerCallback(object : UpdateUiCallBack {
                override fun updateUi(stepCount: Int) {
                    val planWalk_QTY = sp.getParam("planWalk_QTY", "7000") as String
                    binding.tvStepsTaken.setText(stepCount.toString())
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBind) {
            unbindService(conn)
        }
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {

                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {

                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                }
            }
        } else {

        }
    }

}