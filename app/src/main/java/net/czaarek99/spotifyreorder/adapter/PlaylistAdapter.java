package net.czaarek99.spotifyreorder.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.activity.PlaylistsActivity;
import net.czaarek99.spotifyreorder.activity.TracksActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by Czarek on 2017-03-22.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private List<PlaylistSimple> severPlaylistList = new ArrayList<>();
    private final PlaylistsActivity activity;
    private List<PlaylistSimple> itemList;
    private String sortMode;
    private boolean hideEmptyPlaylists;

    public PlaylistAdapter(final PlaylistsActivity activity, List<PlaylistSimple> list) {
        setHasStableIds(true);
        this.activity = activity;
        this.itemList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlaylistSimple playlist = itemList.get(position);
        holder.itemView.setTag(itemList.get(position));
        holder.setPlaylist(playlist);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).id.hashCode();
    }

    public void setItemList(List<PlaylistSimple> itemList){
        severPlaylistList.clear();
        severPlaylistList.addAll(itemList);
        updateList();
    }

    public void setSortMethod(String sortMode){
        this.sortMode = sortMode;
    }

    public void setHideEmptyPlaylists(boolean hideEmpty) {
        this.hideEmptyPlaylists = hideEmpty;
    }

    public void updateList(){
        itemList = new ArrayList<>();
        itemList.addAll(severPlaylistList);

        if(sortMode.equals(getString(R.string.alphabetically))){
            Collections.sort(itemList, new Comparator<PlaylistSimple>() {
                @Override
                public int compare(PlaylistSimple o1, PlaylistSimple o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
        } else if(sortMode.equals(getString(R.string.most_tracks))){
            Collections.sort(itemList, new Comparator<PlaylistSimple>() {
                @Override
                public int compare(PlaylistSimple o1, PlaylistSimple o2) {
                    Integer trackAmount1 = o1.tracks.total;
                    Integer trackAmount2 = o2.tracks.total;
                    return -trackAmount1.compareTo(trackAmount2);
                }
            });
        }

        if(hideEmptyPlaylists){
            for (Iterator<PlaylistSimple> iterator = itemList.iterator(); iterator.hasNext(); ) {
                PlaylistSimple playlistSimple = iterator.next();
                if (playlistSimple.tracks.total < 1) {
                    iterator.remove();
                }
            }
        }

        notifyDataSetChanged();
    }

    private String getString(int resId){
        return activity.getResources().getString(resId);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView playlistNameText;
        private final TextView playlistTrackCountText;
        private final ImageView playlistAlbumArtImage;

        ViewHolder(final View itemView){
            super(itemView);
            playlistNameText = (TextView) itemView.findViewById(R.id.playlistNameText);
            playlistTrackCountText = (TextView) itemView.findViewById(R.id.playlistTrackCountText);
            playlistAlbumArtImage = (ImageView) itemView.findViewById(R.id.playlistAlbumArt);
        }

        void setPlaylist(final PlaylistSimple playlist){
            playlistNameText.setText(playlist.name);
            playlistTrackCountText.setText(activity.getResources().getString(R.string.track_amount, playlist.tracks.total));

            if(playlist.images.size() > 0){
                Glide.with(activity)
                        .load(playlist.images.get(0).url)
                        .placeholder(R.drawable.note)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(playlistAlbumArtImage);
            } else {
                playlistAlbumArtImage.setImageResource(R.drawable.note);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), TracksActivity.class);
                    intent.putExtra("playlistId", playlist.id);
                    intent.putExtra("playlistName", playlist.name);
                    itemView.getContext().startActivity(intent);
                }
            });
        }

    }
}
