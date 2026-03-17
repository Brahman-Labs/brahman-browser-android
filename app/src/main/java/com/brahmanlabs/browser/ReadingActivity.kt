package com.brahmanlabs.browser

import android.content.Intent
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

class ReadingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var emptyText: TextView
    private lateinit var adapter: ReadingAdapter
    private lateinit var db: BrahmanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading)

        db = BrahmanDatabase.getInstance(this)
        recyclerView = findViewById(R.id.readingRecycler)
        btnBack = findViewById(R.id.btnReadingBack)
        btnClear = findViewById(R.id.btnClearReading)
        emptyText = findViewById(R.id.emptyReadingText)

        adapter = ReadingAdapter(
            items = emptyList(),
            onItemClick = { item ->
                val intent = Intent()
                intent.putExtra("url", item.url)
                setResult(RESULT_OK, intent)
                finish()
            },
            onDeleteClick = { item ->
                lifecycleScope.launch {
                    db.readingDao().deleteById(item.id)
                    loadItems()
                    Toast.makeText(this@ReadingActivity,
                        "Removed from reading list", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }
        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Reading List")
                .setMessage("Remove all saved articles?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        db.readingDao().deleteAll()
                        loadItems()
                        Toast.makeText(this@ReadingActivity,
                            "Reading list cleared", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadItems()
    }

    private fun loadItems() {
        lifecycleScope.launch {
            val items = db.readingDao().getAll()
            adapter.updateList(items)
            emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
