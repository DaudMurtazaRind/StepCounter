package com.example.myapplication.ui.fragments

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.UpdateUiCallBack
import com.example.myapplication.database.DatabaseClient
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.models.Steps
import com.example.myapplication.service.StepService
import com.example.myapplication.utils.SharedPreferencesUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var stepsList: LiveData<MutableList<Steps>>
    private lateinit var binding: FragmentHomeBinding
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        initData()
        setUpBarChart()
        binding.stepChart.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }
    }

    private fun setUpBarChart() {
        viewLifecycleOwner.lifecycleScope.launch(IO) {
            val listSteps =
                DatabaseClient.getInstance(requireContext()).appDatabase.stepsDao().getAll()
            withContext(Main) {
                listSteps.observe(viewLifecycleOwner) {
                    var lastThree = it.takeLast(3)
                    var i = 1
                    lastThree?.forEach {
                        val placeHolder = when (i) {
                            1 -> R.id.txt_date1
                            2 -> R.id.txt_date2
                            else -> {
                                R.id.txt_date3
                            }
                        }
                        val steps = when (i) {
                            1 -> R.id.txt_steps1
                            2 -> R.id.txt_steps2
                            else -> {
                                R.id.txt_steps3
                            }
                        }
                        val view = getView()?.findViewById<TextView>(placeHolder)
                        view?.setText(it.date)
                        val stepsView = getView()?.findViewById<TextView>(steps)
                        stepsView?.setText(it.steps.toString())
                        i++
                    }
                }

            }
        }
    }

    private fun initData() {
        sp = SharedPreferencesUtils(context)
        val planWalk_QTY = sp.getParam("planWalk_QTY", "0") as String
        binding.tvStepsTaken.setText(planWalk_QTY)
        setupService()
    }


    private var isBind = false

    private fun setupService() {
        val intent = Intent(context, StepService::class.java)
        isBind = context?.bindService(intent, conn, AppCompatActivity.BIND_AUTO_CREATE) == true
        context?.startService(intent)
    }

    var conn: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val stepService: StepService? = (service as StepService.StepBinder).getService()
            val planWalk_QTY = sp.getParam("planWalk_QTY", "0") as String
            stepService?.getStepCount()?.let { binding.tvStepsTaken.setText(it.toString()) }

            stepService?.registerCallback(object : UpdateUiCallBack {
                override fun updateUi(stepCount: Int) {
                    val planWalk_QTY = sp.getParam("planWalk_QTY", "0") as String
                    binding.tvStepsTaken.setText(stepCount.toString())
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBind) {
            context?.unbindService(conn)
        }
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                } == PackageManager.PERMISSION_GRANTED -> {

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