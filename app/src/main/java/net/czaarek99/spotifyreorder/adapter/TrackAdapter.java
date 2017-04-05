package net.czaarek99.spotifyreorder.adapter;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.activity.TracksActivity;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Czarek on 2017-03-22.
 */

public class TrackAdapter extends DragItemAdapter<Pair<Long, Track>, TrackAdapter.TrackViewHolder> {

    private final List<Long> checkedBoxes = new ArrayList<>();
    private final TracksActivity tracksActivity;
    private boolean isDragging = false;
    private boolean canCreateSelection = false;
    private boolean hasSelection = false;
    private int selectionStart = -1;
    private int selectionEnd = -1;

    public TrackAdapter(TracksActivity tracksActivity, List<Pair<Long, Track>> list) {
        this.tracksActivity = tracksActivity;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_layout, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Pair<Long, Track> entry = mItemList.get(position);
        Track track = entry.second;
        holder.setTrack(track);
        holder.itemView.setTag(entry);

        holder.initCheckbox(entry.first);
        holder.determineVisibility(entry.first);
        holder.initImage();
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public void onDragFinish() {
        isDragging = false;
        clearSelection();
    }

    public void onDragStart(){
        isDragging = true;
        notifyDataSetChanged();
    }

    public void clearSelection(){
        hasSelection = false;
        selectionStart = -1;
        selectionEnd = -1;
        checkedBoxes.clear();
        notifyDataSetChanged();
    }

    public List<Long> getCheckedBoxes(){
        return checkedBoxes;
    }

    public int getSelectionSize() {
        return selectionEnd - selectionStart + 1;
    }

    public void enableSelection() {
        canCreateSelection = true;
        notifyDataSetChanged();
    }

    public void changeSelectionPosition(int fromPosition, int toPosition){
        boolean movedDown = toPosition > fromPosition;

        int prevPosition = toPosition - 1;
        for (Long itemId : checkedBoxes) {
            int position = getPositionForItemId(itemId);
            if (movedDown) {
                changeItemPosition(position, prevPosition);
            } else {
                changeItemPosition(position, prevPosition + 1);
            }

            prevPosition = getPositionForItemId(itemId);
        }
    }

    class TrackViewHolder extends DragItemAdapter.ViewHolder {
        private final TextView trackInfoText;
        private final LinearLayout textCutOffLayout;
        private final CheckBox checkBox;
        private final ImageView reorderImage;
        private final ImageView settingsImage;
        private final RelativeLayout mainLayout;
        private final TextView trackNameText;

        TrackViewHolder(final View itemView) {
            super(itemView, R.id.reorderImage, false);
            trackNameText = (TextView) itemView.findViewById(R.id.trackNameText);
            trackInfoText = (TextView) itemView.findViewById(R.id.trackInfoText);
            checkBox = (CheckBox) itemView.findViewById(R.id.reorderCheckbox);
            textCutOffLayout = (LinearLayout) itemView.findViewById(R.id.textCutOffLayout);
            reorderImage = (ImageView) itemView.findViewById(R.id.reorderImage);
            mainLayout = (RelativeLayout) itemView.findViewById(R.id.trackRelativeLayout);
            settingsImage = (ImageView) itemView.findViewById(R.id.settingsImage);

            textCutOffLayout.bringToFront();
            reorderImage.bringToFront();
            settingsImage.bringToFront();
        }

        private boolean isClickable(int position) {
            return !hasSelection || (position >= selectionStart && position <= selectionEnd + 1);
        }

        void determineVisibility(final long id){
            if(hasSelection()){
                if(checkedBoxes.contains(id) && isDragging){
                    mainLayout.setVisibility(View.INVISIBLE);
                } else {
                    mainLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        void initImage() {
            settingsImage.setVisibility(View.INVISIBLE);

            if (canCreateSelection) {
                if (hasSelection && getAdapterPosition() != selectionStart) {
                    reorderImage.setVisibility(View.INVISIBLE);

                    if(!isDragging && selectionStart + 1 == getAdapterPosition()){
                        settingsImage.setVisibility(View.VISIBLE);
                    }
                } else {
                    reorderImage.setVisibility(View.VISIBLE);
                }
            } else {
                reorderImage.setVisibility(View.INVISIBLE);
            }
        }

        void initCheckbox(final long id) {
            if (canCreateSelection) {
                final boolean isClickable = isClickable(getAdapterPosition());

                if(isDragging){
                    checkBox.setVisibility(View.INVISIBLE);
                } else {
                    if (isClickable) {
                        checkBox.setVisibility(View.VISIBLE);
                    } else {
                        checkBox.setVisibility(View.INVISIBLE);
                        checkedBoxes.remove(id);
                    }
                }

                if (checkedBoxes.contains(id)) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }

                final int position = getAdapterPosition();
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isClickable) {

                            if (checkedBoxes.contains(id)) {
                                checkedBoxes.remove(id);
                                checkBox.setChecked(false);

                                if (hasSelection) {
                                    if (position == selectionStart) {
                                       clearSelection();
                                    } else {
                                        int prevSelectionEnd = selectionEnd;
                                        selectionEnd = position - 1;
                                        for(int position2 = selectionStart; position2 <= prevSelectionEnd + 1; position2++){
                                            notifyItemChanged(position2);
                                        }
                                    }
                                }
                            } else {
                                if (hasSelection) {
                                    selectionEnd = position;
                                    notifyItemChanged(position + 1);
                                } else {
                                    hasSelection = true;
                                    selectionStart = position;
                                    selectionEnd = position;
                                    notifyDataSetChanged();
                                }

                                checkedBoxes.add(id);
                                checkBox.setChecked(true);
                            }

                        }
                    }
                });

                settingsImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tracksActivity.animateInSelectionOptions();
                    }
                });
            } else {
                checkBox.setVisibility(View.INVISIBLE);
            }
        }

        void setTrack(Track track) {
            trackNameText.setText(track.name);
            trackInfoText.setText(String.format("%s - %s", track.artists.get(0).name, track.album.name));
        }

    }
}
