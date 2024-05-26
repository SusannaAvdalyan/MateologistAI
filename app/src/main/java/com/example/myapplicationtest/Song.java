package com.example.myapplicationtest;

public class Song {
    private String name;
    private String artists;
    private String imageUrl;
    private String spotifyUri;

    public Song(String name, String artists, String imageUrl, String spotifyUri) {
        this.name = name;
        this.artists = artists;
        this.imageUrl = imageUrl;
        this.spotifyUri = spotifyUri;
    }

    public String getName() {
        return name;
    }

    public String getArtists() {
        return artists;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpotifyUri() {
        return spotifyUri;
    }
}
