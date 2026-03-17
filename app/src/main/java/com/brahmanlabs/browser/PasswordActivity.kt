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

class PasswordActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnClearAll: ImageButton
    private lateinit var emptyText: TextView
    private lateinit var adapter: PasswordAdapter
    private lateinit var db: BrahmanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        db = BrahmanDatabase.getInstance(this)
        recyclerView = findViewById(R.id.passwordRecycler)
        btnBack = findViewById(R.id.btnPasswordBack)
        btnClearAll = findViewById(R.id.btnClearAllPasswords)
        emptyText = findViewById(R.id.emptyPasswordText)

        adapter = PasswordAdapter(
            items = emptyList(),
            onItemClick = { item ->
                val decUser = PasswordEncryption.decrypt(item.username)
                val decPass = PasswordEncryption.decrypt(item.password)
                AlertDialog.Builder(this)
                    .setTitle("🔑 ${item.domain}")
                    .setMessage("Username: $decUser\n\nPassword: $decPass")
                    .setPositiveButton("Copy Password") { _, _ ->
                        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("password", decPass))
                        Toast.makeText(this, "Password copied", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("Copy Username") { _, _ ->
                        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("username", decUser))
                        Toast.makeText(this, "Username copied", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Close", null)
                    .show()
            },
            onDeleteClick = { item ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Password")
                    .setMessage("Delete saved password for ${item.domain}?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            db.passwordDao().deleteById(item.id)
                            loadPasswords()
                            Toast.makeText(this@PasswordActivity,
                                "Password deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        btnBack.setOnClickListener { finish() }
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear All Passwords")
                .setMessage("Delete all saved passwords? This cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        db.passwordDao().deleteAll()
                        loadPasswords()
                        Toast.makeText(this@PasswordActivity,
                            "All passwords cleared", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        loadPasswords()
    }

    private fun loadPasswords() {
        lifecycleScope.launch {
            val items = db.passwordDao().getAll()
            adapter.updateList(items)
            emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
