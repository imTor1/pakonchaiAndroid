package com.example.pakonchaiandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val gson = Gson()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComputerDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ComputerDetail(emptyList())
        recyclerView.adapter = adapter

        val addComputerButton: FloatingActionButton = findViewById(R.id.addcomputer)
        addComputerButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        fetchData()
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            val url = getString(R.string.root_url) + getString(R.string.fetchdata)
            val request = Request.Builder()
                .url(url)
                .build()
            var response: Response? = null
            try {
                response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("ResponseBody", responseBody)

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        handleFetchResponse(responseBody)
                    }
                } else {
                    Log.e("FetchDataError", "Response not successful: ${response.code}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Error: Server returned ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FetchDataError", "Error fetching data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error fetching data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                response?.close() // Ensure the response is closed to avoid resource leaks
            }
        }
    }

    private fun handleFetchResponse(responseBody: String) {
        try {
            val jsonResponse = gson.fromJson(responseBody, ApiResponse::class.java)
            if (jsonResponse.status == "success") {
                val computers = jsonResponse.data ?: emptyList() // Handle null data
                adapter.updateComputers(computers)
            } else {
                Log.e("ApiResponseError", "API returned an error: ${jsonResponse.message}")
                Toast.makeText(this, "API Error: ${jsonResponse.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        } catch (e: JsonSyntaxException) {
            Log.e("ParseError", "JSON syntax error: ${e.message}", e)
            Toast.makeText(this, "JSON Error: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("ParseError", "Error parsing data: ${e.message}", e)
            Toast.makeText(this, "Parsing Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    data class ApiResponse(
        val status: String,
        val data: List<Computer>?,
        val message: String?
    )
}
