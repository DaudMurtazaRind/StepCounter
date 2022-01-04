package com.example.myapplication.ui

import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.StepsCountRVAdapter
import com.example.myapplication.database.DatabaseClient
import com.example.myapplication.databinding.ActivityHistoryBinding
import com.example.myapplication.models.Steps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var list: MutableList<Steps>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        CoroutineScope(IO).launch {
            list =
                DatabaseClient.getInstance(this@HistoryActivity).appDatabase.stepsDao().getAll()
            print("list size is ${list.size}")
            withContext(Main) {
                var adapter = list?.let { StepsCountRVAdapter(it) }
                binding.rvStepsHistory.layoutManager =
                    LinearLayoutManager(this@HistoryActivity)
                binding.rvStepsHistory.adapter = adapter
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}