package com.example.myapplicationtest;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private Context context;

    public SongAdapter(List<Song> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }
    public void clearSongs() {
        songList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        private TextView txtSongName;
        private TextView txtArtistName;
        private ImageView imgSong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSongName = itemView.findViewById(R.id.txt_song_name);
            txtArtistName = itemView.findViewById(R.id.txt_artist_name);
            imgSong = itemView.findViewById(R.id.img_song);
        }

        public void bind(Song song) {
            txtSongName.setText(song.getName());
            txtArtistName.setText(song.getArtists());
            Glide.with(context).load(song.getImageUrl()).into(imgSong);
        }
    }
}
