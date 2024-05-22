package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "19dae812f0044c0bac89a49e2b84f8a9";
    private static final String REDIRECT_URI = "mateologist://spotify-auth-callback";
    private static final String[] SCOPES = {"user-read-email", "user-read-recently-played"};
    private static final String TAG = "SpotifyActivity";
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private Button showSuggestionsButton, authenticateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spotify);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(songAdapter);
        showSuggestionsButton = findViewById(R.id.showSuggestionsButton);
        authenticateButton = findViewById(R.id.authenticateButton);

        authenticateButton.setEnabled(true);
        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateWithSpotify();
            }
        });

        showSuggestionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchMusicSuggestions();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleSpotifyRedirect(getIntent());
        SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
        String accessToken = preferences.getString("spotify_access_token", null);
        if (accessToken != null) {
            showSuggestionsButton.setEnabled(true);
            Toast.makeText(this, "Successfully authenticated with Spotify!", Toast.LENGTH_SHORT).show();
        } else {
            showSuggestionsButton.setEnabled(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void handleSpotifyRedirect(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            String fragment = uri.getFragment();
            if (fragment != null) {
                String[] params = fragment.split("&");
                for (String param : params) {
                    if (param.startsWith("access_token=")) {
                        String accessToken = param.split("=")[1];
                        handleSpotifyAuthenticationSuccess(accessToken);
                        return;
                    }
                }
            }
            handleSpotifyAuthenticationFailure();
        }
    }

    private void authenticateWithSpotify() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("accounts.spotify.com")
                .appendPath("authorize")
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("scope", TextUtils.join(" ", SCOPES));

        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        startActivity(intent);
    }

    private void fetchMusicSuggestions() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
        String accessToken = preferences.getString("spotify_access_token", null);
        if (accessToken != null) {
            Log.d(TAG, "Fetching music suggestions with access token: " + accessToken);
            getUserProfile(accessToken);
        } else {
            Log.e(TAG, "Access token is null. Please authenticate first.");
            Toast.makeText(this, "Please authenticate with Spotify first.", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getUserProfile(String accessToken) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/player/recently-played?limit=5")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Log.d(TAG, "Making API request to fetch recently played tracks.");
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API request failed", e);
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "API response: " + responseData);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray items = jsonResponse.getJSONArray("items");
                        ArrayList<Song> songs = new ArrayList<>();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject track = items.getJSONObject(i).getJSONObject("track");
                            String trackName = track.getString("name");
                            JSONArray artists = track.getJSONArray("artists");
                            StringBuilder artistNames = new StringBuilder();
                            for (int j = 0; j < artists.length(); j++) {
                                if (j > 0) artistNames.append(", ");
                                artistNames.append(artists.getJSONObject(j).getString("name"));
                            }
                            String imageUrl = track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                            songs.add(new Song(trackName, artistNames.toString(), imageUrl));
                        }
                        runOnUiThread(() -> {
                            Log.d(TAG, "Updating UI with fetched songs.");
                            songAdapter.setSongs(songs);
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse JSON response", e);
                    }
                } else {
                    Log.e(TAG, "Failed to get user's recently played tracks: " + response.code());
                }
            }
        });
    }

    private void handleSpotifyAuthenticationSuccess(String accessToken) {
        SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("spotify_access_token", accessToken);
        editor.apply();
        Log.d(TAG, "Spotify authentication successful. Access token saved.");
        showSuggestionsButton.setEnabled(true);
    }

    private void handleSpotifyAuthenticationFailure() {
        Log.e(TAG, "Spotify authentication failed");
        Toast.makeText(this, "Spotify authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
    }
}
