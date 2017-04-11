package net.czaarek99.spotifyreorder.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.activity.PlaylistsActivity;
import net.czaarek99.spotifyreorder.activity.TracksActivity;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by Czarek on 2017-03-22.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private static final int IMAGE_DOWNLOAD_THREADS = 5;

    private final BlockingQueue<PlaylistSimple> queuedPlaylistsToRefresh = new LinkedBlockingQueue<>();
    private final Map<String, Pair<Image, Bitmap>> playlistImages = new HashMap<>();
    private final AtomicBoolean runImageThreads = new AtomicBoolean(true);
    private final PlaylistsActivity activity;
    private List<PlaylistSimple> itemList;

    public PlaylistAdapter(final PlaylistsActivity activity, List<PlaylistSimple> list) {
        setHasStableIds(true);
        this.activity = activity;
        this.itemList = list;

        for(int threadId = 0; threadId < IMAGE_DOWNLOAD_THREADS; threadId++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(runImageThreads.get()){
                            PlaylistSimple playlistSimple = queuedPlaylistsToRefresh.poll(5, TimeUnit.SECONDS);

                            if(playlistSimple != null){
                                Image image = playlistSimple.images.get(0);
                                InputStream in = new URL(image.url).openStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(in);

                                playlistImages.put(playlistSimple.id, new Pair<>(image, bitmap));
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
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

    public void setItemList(List<PlaylistSimple> itemList){
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).id.hashCode();
    }

    public void killImageThreads(){
        runImageThreads.set(false);
    }

    public void updateCachedImageFor(final PlaylistSimple playlist){
        if(playlist.images.isEmpty()){
            playlistImages.remove(playlist.id);

            notifyDataSetChanged();
        } else {
            boolean doRefresh = true;
            if(playlistImages.containsKey(playlist.id)){
                String prevImageUrl = playlistImages.get(playlist.id).first.url;
                String newImageUrl = playlist.images.get(0).url;

                if(prevImageUrl.equals(newImageUrl)){
                    doRefresh = false;
                }

            }

            if(doRefresh){
                queuedPlaylistsToRefresh.add(playlist);
            }
        }
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

            if(playlistImages.containsKey(playlist.id)){
                playlistAlbumArtImage.setImageBitmap(playlistImages.get(playlist.id).second);
            } else if(playlist.images.size() > 0){
                updateCachedImageFor(playlist);
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
