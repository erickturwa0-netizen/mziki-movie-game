package com.mziki.moviegame;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView errorText;
    private TextView headerTitle;
    private TextView headerSubtitle;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final Map<Integer, List<MediaItem>> cache = new HashMap<>();

    private static final String[] ENDPOINTS = {
            "https://itunes.apple.com/search?term=afrobeats&media=music&limit=20",
            "https://api.tvmaze.com/shows",
            "https://www.freetogame.com/api/games"
    };

    private static final String[] TITLES = {"Mziki", "Movie", "Game"};

    private static final int[] ACCENTS = {
            Color.parseColor("#E91E63"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#4CAF50")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recycler);
        progress = findViewById(R.id.progress);
        errorText = findViewById(R.id.errorText);
        headerTitle = findViewById(R.id.headerTitle);
        headerSubtitle = findViewById(R.id.headerSubtitle);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new MediaAdapter(new ArrayList<>(), ACCENTS[0]));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int index = 0;
            int id = item.getItemId();
            if (id == R.id.nav_movie) index = 1;
            else if (id == R.id.nav_game) index = 2;
            loadTab(index);
            return true;
        });

        loadTab(0);
    }

    private void loadTab(int index) {
        headerTitle.setText(TITLES[index]);
        headerSubtitle.setText("Data kutoka API");
        headerTitle.setTextColor(ACCENTS[index]);

        if (cache.containsKey(index)) {
            showList(cache.get(index), ACCENTS[index]);
            return;
        }

        progress.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);

        Request request = new Request.Builder().url(ENDPOINTS[index]).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Imeshindwa kupakia:\n" + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response r = response) {
                    if (!r.isSuccessful()) {
                        runOnUiThread(() -> {
                            progress.setVisibility(View.GONE);
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setText("Error: HTTP " + r.code());
                        });
                        return;
                    }
                    String body = r.body() != null ? r.body().string() : "";
                    List<MediaItem> items = parse(index, body);
                    cache.put(index, items);
                    runOnUiThread(() -> showList(items, ACCENTS[index]));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Parse error: " + e.getMessage());
                    });
                }
            }
        });
    }

    private void showList(List<MediaItem> items, int accent) {
        progress.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);
        recycler.setAdapter(new MediaAdapter(items, accent));
    }

    private List<MediaItem> parse(int index, String body) {
        List<MediaItem> list = new ArrayList<>();
        try {
            if (index == 0) {
                JSONArray results = new JSONObject(body).optJSONArray("results");
                if (results == null) results = new JSONArray();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject r = results.getJSONObject(i);
                    String title = r.optString("trackName", r.optString("collectionName", "Unknown"));
                    String artist = r.optString("artistName", "");
                    String img = r.optString("artworkUrl100", null);
                    if (img != null) img = img.replace("100x100", "200x200");
                    list.add(new MediaItem(title, artist, img));
                }
            } else if (index == 1) {
                JSONArray results = new JSONArray(body);
                int limit = Math.min(25, results.length());
                for (int i = 0; i < limit; i++) {
                    JSONObject r = results.getJSONObject(i);
                    String name = r.optString("name", "Unknown");
                    JSONArray genres = r.optJSONArray("genres");
                    StringBuilder sb = new StringBuilder();
                    if (genres != null) {
                        for (int g = 0; g < genres.length(); g++) {
                            if (g > 0) sb.append(", ");
                            sb.append(genres.getString(g));
                        }
                    } else {
                        sb.append(r.optString("type", ""));
                    }
                    String image = null;
                    JSONObject imgObj = r.optJSONObject("image");
                    if (imgObj != null) image = imgObj.optString("medium", null);
                    list.add(new MediaItem(name, sb.toString(), image));
                }
            } else {
                JSONArray results = new JSONArray(body);
                int limit = Math.min(30, results.length());
                for (int i = 0; i < limit; i++) {
                    JSONObject r = results.getJSONObject(i);
                    list.add(new MediaItem(
                            r.optString("title", "Unknown"),
                            r.optString("genre", r.optString("platform", "")),
                            r.optString("thumbnail", null)
                    ));
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
