package com.brahmanlabs.browser

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var btnClearAll: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var emptyText: TextView
    private lateinit var adapter: HistoryAdapter
    private lateinit var db: BrahmanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        db = BrahmanDatabase.getInstance(this)

        recyclerView = findViewById(R.id.historyRecycler)
        searchBar = findViewById(R.id.historySearch)
        btnClearAll = findViewById(R.id.btnClearAll)
        btnBack = findViewById(R.id.btnHistoryBack)
        emptyText = findViewById(R.id.emptyHistoryText)

        adapter = HistoryAdapter(
            items = emptyList(),
            onItemClick = { item ->
                val intent = Intent()
                intent.putExtra("url", item.url)
                setResult(RESULT_OK, intent)
                finish()
            },
            onDeleteClick = { item ->
                lifecycleScope.launch {
                    db.historyDao().deleteById(item.id)
                    loadHistory()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnClearAll.setOnClickListener {
            lifecycleScope.launch {
                db.historyDao().clearAll()
                loadHistory()
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    val query = "%${s.toString()}%"
                    val results = db.historyDao().search(query)
                    adapter.updateList(results)
                    emptyText.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val items = db.historyDao().getAll()
            adapter.updateList(items)
            emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
