package com.mziki.moviegame

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class MediaItem(
    val title: String,
    val subtitle: String,
    val imageUrl: String?
)

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var headerTitle: TextView
    private lateinit var headerSubtitle: TextView
    private lateinit var adapter: MediaAdapter

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val cache = mutableMapOf<Int, List<MediaItem>>()

    private val endpoints = listOf(
        "https://itunes.apple.com/search?term=afrobeats&media=music&limit=20",
        "https://api.tvmaze.com/shows",
        "https://www.freetogame.com/api/games"
    )

    private val titles = listOf("Mziki", "Movie", "Game")
    private val accents = listOf(
        Color.parseColor("#E91E63"),
        Color.parseColor("#00BCD4"),
        Color.parseColor("#4CAF50")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recycler)
        progress = findViewById(R.id.progress)
        errorText = findViewById(R.id.errorText)
        headerTitle = findViewById(R.id.headerTitle)
        headerSubtitle = findViewById(R.id.headerSubtitle)

        adapter = MediaAdapter(emptyList(), accents[0])
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            val index = when (item.itemId) {
                R.id.nav_music -> 0
                R.id.nav_movie -> 1
                R.id.nav_game -> 2
                else -> 0
            }
            loadTab(index)
            true
        }

        loadTab(0)
    }

    private fun loadTab(index: Int) {
        headerTitle.text = titles[index]
        headerSubtitle.text = "Data kutoka API"
        headerTitle.setTextColor(accents[index])

        cache[index]?.let {
            showList(it, accents[index])
            return
        }

        progress.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        recycler.visibility = View.GONE

        val request = Request.Builder().url(endpoints[index]).get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = "Imeshindwa kupakia:\n${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        runOnUiThread {
                            progress.visibility = View.GONE
                            errorText.visibility = View.VISIBLE
                            errorText.text = "Error: HTTP ${it.code}"
                        }
                        return
                    }
                    val body = it.body?.string() ?: ""
                    val items = parse(index, body)
                    cache[index] = items
                    runOnUiThread {
                        showList(items, accents[index])
                    }
                }
            }
        })
    }

    private fun showList(items: List<MediaItem>, accent: Int) {
        progress.visibility = View.GONE
        errorText.visibility = View.GONE
        recycler.visibility = View.VISIBLE
        adapter = MediaAdapter(items, accent)
        recycler.adapter = adapter
    }

    private fun parse(index: Int, body: String): List<MediaItem> {
        val list = mutableListOf<MediaItem>()
        try {
            when (index) {
                0 -> {
                    val results = JSONObject(body).optJSONArray("results") ?: JSONArray()
                    for (i in 0 until results.length()) {
                        val r = results.getJSONObject(i)
                        val title = r.optString("trackName", r.optString("collectionName", "Unknown"))
                        val artist = r.optString("artistName", "")
                        var img = r.optString("artworkUrl100", null)
                        if (img != null) img = img.replace("100x100", "200x200")
                        list.add(MediaItem(title, artist, img))
                    }
                }
                1 -> {
                    val results = JSONArray(body)
                    val limit = minOf(25, results.length())
                    for (i in 0 until limit) {
                        val r = results.getJSONObject(i)
                        val name = r.optString("name", "Unknown")
                        val genres = r.optJSONArray("genres")
                        val subtitle = if (genres != null) {
                            (0 until genres.length()).joinToString(", ") { genres.getString(it) }
                        } else r.optString("type", "")
                        val image = r.optJSONObject("image")?.optString("medium")
                        list.add(MediaItem(name, subtitle, image))
                    }
                }
                else -> {
                    val results = JSONArray(body)
                    val limit = minOf(30, results.length())
                    for (i in 0 until limit) {
                        val r = results.getJSONObject(i)
                        list.add(
                            MediaItem(
                                r.optString("title", "Unknown"),
                                r.optString("genre", r.optString("platform", "")),
                                r.optString("thumbnail", null)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }
        return list
    }
}
