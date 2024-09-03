package com.example.pakonchaiandroid

data class Computer(
    val ComputerID: Int,
    val Image: String?,
    val BrandName: String,
    val ModelName: String,
    val SerialNumber: String,
    val Quantity: Int,
    val Price: Double,
    val CPU_Speed_GHz: Double,
    val Memory_GB: Int,
    val HDD_Capacity_GB: Int
)
