package com.example.pakonchaiandroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pakonchaiandroid.databinding.ItemComputerBinding

class ComputerDetail(private var computers: List<Computer>) : RecyclerView.Adapter<ComputerDetail.ComviewHolder>() {

    inner class ComviewHolder(private val binding: ItemComputerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(computer: Computer) {
            Glide.with(binding.computerImage.context)
                .load(computer.Image)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .placeholder(R.drawable.game)
                .error(R.drawable.ic_launcher_background)
                .into(binding.computerImage)



//            Glide.with(binding.computerImage.context)
//                .load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQcdTBJkfl2z-h2b2hWTzCrEtw-IbXwW7ceQ&s")
//                .into(binding.computerImage)


            binding.textbrandName.text = computer.BrandName
            binding.textmodelName.text = computer.ModelName
            binding.textserialNumber.text = computer.SerialNumber
            binding.textquantity.text = computer.Quantity.toString()
            binding.textprice.text = computer.Price.toString()
            binding.textcpuSpeedGHz.text = computer.CPU_Speed_GHz.toString()
            binding.textmemoryGB.text = computer.Memory_GB.toString()
            binding.texthddCapacityGB.text = computer.HDD_Capacity_GB.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComviewHolder {
        val binding = ItemComputerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComviewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComviewHolder, position: Int) {
        holder.bind(computers[position])
    }

    override fun getItemCount() = computers.size

    fun updateComputers(newComputers: List<Computer>) {
        this.computers = newComputers
        notifyDataSetChanged()
    }
}

