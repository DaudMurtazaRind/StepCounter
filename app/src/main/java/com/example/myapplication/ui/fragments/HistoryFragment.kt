package com.example.myapplication.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.StepsCountRVAdapter
import com.example.myapplication.database.DatabaseClient
import com.example.myapplication.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        viewLifecycleOwner.lifecycleScope.launch(IO) {
            val stepsList =
                context?.let { DatabaseClient.getInstance(it).appDatabase.stepsDao().getAll() }
            withContext(Main) {
                stepsList?.observe(viewLifecycleOwner, {
                    if (it.isNullOrEmpty()) {
                        binding.txtNoRecord.visibility = View.VISIBLE
                        binding.rvHistory.visibility = View.GONE
                    } else {
                        binding.txtNoRecord.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
                        val adapter = StepsCountRVAdapter(it)
                        binding.rvHistory.adapter = adapter
                    }
                })
            }
        }
    }
}