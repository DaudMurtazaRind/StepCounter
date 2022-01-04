package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.StepsitemviewBinding
import com.example.myapplication.models.Steps

class StepsCountRVAdapter(var list: MutableList<Steps>) :
    RecyclerView.Adapter<StepsCountRVAdapter.singleViewHolder>() {

    inner class singleViewHolder(val binding: StepsitemviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): singleViewHolder {
        val binding =
            StepsitemviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return singleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: singleViewHolder, position: Int) {
        with(holder) {
            with(list) {
                binding.tvDate.setText(this.get(position).date)
                binding.tvSteps.setText(this.get(position).steps.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}