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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpotifyActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "19dae812f0044c0bac89a49e2b84f8a9";
    private static final String CLIENT_SECRET = "572432672c694610b26e1e023cc39884";
    private static final String REDIRECT_URI = "mateologist://spotify-auth-callback";
    private static final String[] SCOPES = {"user-read-email", "user-library-read", "user-library-modify"};
    private static final String KEY_SONG_LIST = "song_list";
    private static final String TAG = "SpotifyActivity";
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private Button showSuggestionsButton, authenticateButton;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference tokenRef;
    private ProgressBar progressBar;


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
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
        tokenRef = FirebaseDatabase.getInstance().getReference("userTokens").child(currentUser.getUid());

        authenticateButton.setEnabled(true);
        authenticateButton.setOnClickListener(v -> authenticateWithSpotify());

        showSuggestionsButton.setOnClickListener(v -> fetchMusicSuggestions());


    }


    @Override
    protected void onResume() {
        super.onResume();
        handleSpotifyRedirect(getIntent());
        // Check for the token in Firebase
        tokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String accessToken = dataSnapshot.getValue(String.class);
                if (accessToken != null) {
                    // Token found, authenticate with Spotify
                    showSuggestionsButton.setEnabled(true);
                    Toast.makeText(SpotifyActivity.this, "Successfully authenticated with Spotify!", Toast.LENGTH_SHORT).show();
                    //getUserProfile(accessToken); // No need to fetch user profile here
                } else {
                    // Token not found, user needs to authenticate with Spotify
                    showSuggestionsButton.setEnabled(false);
                    Log.e(TAG, "Access token not found in Firebase. Please authenticate with Spotify.");
                    Toast.makeText(SpotifyActivity.this, "Access token not found. Please authenticate with Spotify.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e(TAG, "Failed to retrieve access token from Firebase: " + databaseError.getMessage());
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
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
                        tokenRef.setValue(accessToken);
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
        ensureAccessTokenValid();
        showProgressBar();
        songAdapter.clearSongs();

        SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
        String accessToken = preferences.getString("spotify_access_token", null);
        if (accessToken != null) {
            Log.d(TAG, "Access token retrieved: " + accessToken);
            fetchRandomTracks(accessToken); // Fetch random tracks directly
        } else {
            Log.e(TAG, "Access token is null. Authenticating with Spotify...");
            authenticateWithSpotify();
        }
    }




    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchRandomTracks(String accessToken) {
        OkHttpClient client = new OkHttpClient();
        int defaultOffset = new Random().nextInt(100);
        int limit = 5;
        int offset = new Random().nextInt(1000);

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/tracks?limit=" + limit + "&offset=" + offset)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API request failed", e);
                runOnUiThread(() -> hideProgressBar()); // Hide progress bar on failure
            }

            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> hideProgressBar()); // Hide progress bar regardless of response

                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Response data: " + responseData); // Log response data
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray items = jsonResponse.getJSONArray("items");
                        List<Song> songs = new ArrayList<>();
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
                    Log.e(TAG, "Failed to get user's tracks: " + response.code());
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
        tokenRef.setValue(accessToken)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Access token sent to Firebase successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to send access token to Firebase: " + e.getMessage());
                    }
                });
    }

    private void handleSpotifyAuthenticationFailure() {
        Log.e(TAG, "Spotify authentication failed");
        Toast.makeText(this, "Spotify authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
    }

    // Check if access token is expired
    private boolean isAccessTokenExpired() {
        SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
        long expirationTime = preferences.getLong("spotify_access_token_expires_at", 0);
        return expirationTime < System.currentTimeMillis();
    }

    // Refresh access token
    private void refreshAccessToken(String refreshToken) {
        // Construct the request body
        FormBody.Builder formBodyBuilder = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET);

        RequestBody formBody = formBodyBuilder.build();

        // Create the request
        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(formBody)
                .build();

        // Send the request
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to refresh access token", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String newAccessToken = jsonResponse.getString("access_token");
                        long expiresIn = jsonResponse.getLong("expires_in"); // in seconds
                        long expirationTime = System.currentTimeMillis() + expiresIn * 1000; // convert to milliseconds
                        updateAccessTokenInStorage(newAccessToken, expirationTime);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse JSON response", e);
                    }
                } else {
                    Log.e(TAG, "Failed to refresh access token: " + response.code());
                }
            }
        });
    }

    // Update access token in SharedPreferences and Firebase
    private void updateAccessTokenInStorage(String newAccessToken, long expirationTime) {
        SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("spotify_access_token", newAccessToken);
        editor.putLong("spotify_access_token_expires_at", expirationTime);
        editor.apply();

        // Also update the token in Firebase
        tokenRef.setValue(newAccessToken)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Access token updated in Firebase successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to update access token in Firebase: " + e.getMessage());
                    }
                });
    }

    // Before making API calls, check if access token needs to be refreshed
    private void checkAndRefreshAccessTokenIfNeeded() {
        if (isAccessTokenExpired()) {
            SharedPreferences preferences = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE);
            String refreshToken = preferences.getString("spotify_refresh_token", null);
            if (refreshToken != null) {
                refreshAccessToken(refreshToken);
            } else {
                Log.e(TAG, "Refresh token not found. User needs to re-authenticate.");
                // Handle scenario where refresh token is not available (user needs to re-authenticate)
            }
        }
    }

    // Call this method before making any API calls to Spotify
    private void ensureAccessTokenValid() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAccessTokenExpired()) {
            checkAndRefreshAccessTokenIfNeeded();
        }

        // Proceed with the API call using the refreshed access token
        String accessToken = getSharedPreferences("com.example.myapplicationtest", Context.MODE_PRIVATE)
                .getString("spotify_access_token", null);
        if (accessToken != null) {
            fetchRandomTracks(accessToken); // Fetch random tracks directly
        } else {
            Log.e(TAG, "Access token is null. Please authenticate first.");
            Toast.makeText(this, "Please authenticate with Spotify first.", Toast.LENGTH_SHORT).show();
        }
    }


}