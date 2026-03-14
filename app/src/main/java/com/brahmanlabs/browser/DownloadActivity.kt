package com.brahmanlabs.browser

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnClearAll: ImageButton
    private lateinit var emptyText: TextView
    private lateinit var adapter: DownloadAdapter
    private lateinit var db: BrahmanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        db = BrahmanDatabase.getInstance(this)

        recyclerView = findViewById(R.id.downloadRecycler)
        btnBack = findViewById(R.id.btnDownloadBack)
        btnClearAll = findViewById(R.id.btnClearAllDownloads)
        emptyText = findViewById(R.id.emptyDownloadText)

        adapter = DownloadAdapter(
            items = emptyList(),
            onItemClick = { item ->
                Toast.makeText(
                    this,
                    "Saved: ${item.fileName}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteClick = { item ->
                lifecycleScope.launch {
                    db.downloadDao().deleteById(item.id)
                    loadDownloads()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Downloads")
                .setMessage("Clear all download records?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        db.downloadDao().deleteAll()
                        loadDownloads()
                        Toast.makeText(
                            this@DownloadActivity,
                            "Downloads cleared",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadDownloads()
    }

    private fun loadDownloads() {
        lifecycleScope.launch {
            val items = db.downloadDao().getAll()
            adapter.updateList(items)
            emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
