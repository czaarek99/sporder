package net.czaarek99.spotifyreorder.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.activity.TracksActivity;
import net.czaarek99.spotifyreorder.util.DownloadImageTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by Czarek on 2017-03-22.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final Map<String, Pair<Image, Bitmap>> playlistImages = new HashMap<>();
    private final Set<String> queuedImages = Collections.synchronizedSet(new HashSet<String>());
    private final Context context;
    private List<PlaylistSimple> itemList;

    public PlaylistAdapter(Context context, List<PlaylistSimple> list) {
        setHasStableIds(true);
        this.context = context;
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
        holder.setPlaylist(playlist, position);
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

    public void clearImageCache(){
        playlistImages.clear();
        notifyDataSetChanged();
    }

    public void updateCachedImageFor(final PlaylistSimple playlist){
        if(!queuedImages.contains(playlist.id)){

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
                    queuedImages.add(playlist.id);
                    final Image newImage = playlist.images.get(0);
                    new DownloadImageTask(){
                        @Override
                        protected void onPostExecute(Bitmap bitmap) {

                            playlistImages.put(playlist.id, new Pair<>(newImage, bitmap));
                            queuedImages.remove(playlist.id);
                            notifyDataSetChanged();
                        }
                    }.execute(newImage.url);
                }
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

        void setPlaylist(final PlaylistSimple playlist, int position){
            playlistNameText.setText(playlist.name);
            playlistTrackCountText.setText(context.getResources().getString(R.string.track_amount, playlist.tracks.total));

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
                    itemView.getContext().startActivity(intent);
                }
            });
        }

    }
}
