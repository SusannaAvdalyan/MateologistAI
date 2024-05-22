package com.example.myapplicationtest;
public class Song {
    private String name;
    private String artists;
    private String imageUrl;

    public Song(String name, String artists, String imageUrl) {
        this.name = name;
        this.artists = artists;
        this.imageUrl = imageUrl;
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
}
