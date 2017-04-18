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
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Czarek on 2017-03-22.
 */

public class TrackAdapter extends DragItemAdapter<Pair<Long, Track>, TrackAdapter.TrackViewHolder> {

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
        //To trigger recyclerview logic in ListSwipeItem on line 87
        holder.itemView.setTag(new Object());
        holder.itemView.setTag(R.id.VIEW_HOLDER_ID, holder);
        holder.itemView.setTag(R.id.PAIR_ID, entry);

        holder.initImage();
        holder.initCheckbox();
        holder.determineVisibility();
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

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    public void clearSelection() {
        tracksActivity.tracksSettingsImage.setVisibility(View.INVISIBLE);

        hasSelection = false;
        selectionStart = -1;
        selectionEnd = -1;
        notifyDataSetChanged();
    }

    public int getSelectionSize() {
        return selectionEnd - selectionStart + 1;
    }

    public void enableSelection() {
        canCreateSelection = true;
        notifyDataSetChanged();
    }

    /*
    If you edit this method please test that moving tracks up and down still works properly
     */
    public void changeSelectionPosition(int fromPosition, int toPosition) {
        boolean movedDown = toPosition > fromPosition;
        int sizeOfItemsLeftToMove = getSelectionSize();

        for (int i = 0; i < sizeOfItemsLeftToMove; i++) {
            changeItemPosition(fromPosition, toPosition);

            if(!movedDown){
                fromPosition++;
                toPosition++;
            }
        }
    }

    public void quietPositionChange(int fromPos, int toPos) {
        if (this.mItemList != null && this.mItemList.size() > fromPos && this.mItemList.size() > toPos) {
            Pair<Long, Track> item = this.mItemList.remove(fromPos);
            this.mItemList.add(toPos, item);
        }
    }

    public class TrackViewHolder extends DragItemAdapter.ViewHolder {
        private final TextView trackInfoText;
        private final ImageView reorderImage;
        private final TextView trackNameText;

        public final CheckBox checkBox;
        public final RelativeLayout mainLayout;

        TrackViewHolder(final View itemView) {
            super(itemView, R.id.reorderImage, false);
            trackNameText = (TextView) itemView.findViewById(R.id.trackNameText);
            trackInfoText = (TextView) itemView.findViewById(R.id.trackInfoText);
            checkBox = (CheckBox) itemView.findViewById(R.id.reorderCheckbox);
            reorderImage = (ImageView) itemView.findViewById(R.id.reorderImage);
            mainLayout = (RelativeLayout) itemView.findViewById(R.id.trackRelativeLayout);
        }

        private boolean isClickable(int position) {
            return !hasSelection || position >= selectionStart;
        }

        private boolean isInSelection(int position) {
            return position >= selectionStart && position <= selectionEnd;
        }

        void determineVisibility() {
            mainLayout.setVisibility(View.VISIBLE);

            if (hasSelection() && isDragging && isInSelection(getAdapterPosition())) {
                mainLayout.setVisibility(View.INVISIBLE);
            }
        }

        void initImage() {
            reorderImage.setVisibility(View.INVISIBLE);

            if (canCreateSelection) {
                reorderImage.setVisibility(View.VISIBLE);

                if (hasSelection()) {
                    if (getAdapterPosition() == selectionStart) {
                        reorderImage.setVisibility(View.VISIBLE);
                    } else {
                        reorderImage.setVisibility(View.INVISIBLE);
                    }

                } else {
                    reorderImage.setVisibility(View.VISIBLE);
                }
            }
        }

        void initCheckbox() {
            if (canCreateSelection) {
                final boolean isClickable = isClickable(getAdapterPosition());

                if (isDragging) {
                    checkBox.setVisibility(View.INVISIBLE);
                } else {
                    if (isClickable) {
                        checkBox.setVisibility(View.VISIBLE);
                    } else {
                        checkBox.setVisibility(View.INVISIBLE);
                    }
                }

                final int position = getAdapterPosition();
                if (isInSelection(position)) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }

                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isInSelection = isInSelection(position);
                        if (isClickable) {

                            if (isInSelection) {
                                if (hasSelection) {
                                    if (position == selectionStart) {
                                        tracksActivity.animateOutClearSelectionButton();
                                        clearSelection();
                                    } else {
                                        selectionEnd = position - 1;
                                        notifyDataSetChanged();
                                    }
                                }
                            } else {
                                if (hasSelection) {
                                    selectionEnd = position;
                                    notifyDataSetChanged();
                                } else {
                                    hasSelection = true;
                                    selectionStart = position;
                                    selectionEnd = position;

                                    notifyDataSetChanged();
                                    tracksActivity.animateInClearSelectionButton();
                                    tracksActivity.tracksSettingsImage.setVisibility(View.VISIBLE);
                                }

                            }

                        }
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
