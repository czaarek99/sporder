package net.czaarek99.spotifyreorder.activity;

import android.graphics.PorterDuff;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.woxthebox.draglistview.DragListView;
import com.woxthebox.draglistview.swipe.ListSwipeHelper;
import com.woxthebox.draglistview.swipe.ListSwipeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.adapter.TrackAdapter;
import net.czaarek99.spotifyreorder.item.TrackDragItem;
import net.czaarek99.spotifyreorder.util.CallbackGroup;
import net.czaarek99.spotifyreorder.util.NormanDialog;
import net.czaarek99.spotifyreorder.util.ProgressBarAnimation;
import net.czaarek99.spotifyreorder.util.Util;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.SnapshotId;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksToRemoveByPosition;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

//TODO: Auto refresh algorithm
//TODO: Better animations for multiple items change
public class TracksActivity extends SporderActivity {

    private final Queue<CallbackGroup<SnapshotId>> modificationCalls = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean shouldThreadRun = new AtomicBoolean(true);
    private final AtomicReference<String> snapshotId = new AtomicReference<>();

    private SpotifyService spotify;
    private TrackAdapter trackListAdapter;
    private ProgressBarAnimation progressBarAnimation;
    private LinearLayout progressLayout;
    private RelativeLayout selectionOptionsMainLayout;
    private View selectionOptionsDismissView;
    private FloatingActionButton clearSelectionButton;
    private String playlistId;
    private String currentUserId;
    private int trackAmount;

    public ImageView tracksSettingsImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        spotify = getSApplication().getSpotifyService();
        currentUserId = getSApplication().getSpotifyUserID();

        ProgressBar playlistLoadProgress = (ProgressBar) findViewById(R.id.playlistLoadProgress);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            playlistLoadProgress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.glueGreen), PorterDuff.Mode.SRC_IN);
        }

        progressBarAnimation = new ProgressBarAnimation(playlistLoadProgress, 2500);
        progressLayout = (LinearLayout) findViewById(R.id.playlistProgressLayout);
        selectionOptionsMainLayout = (RelativeLayout) findViewById(R.id.selectionOptionsMainLayout);
        selectionOptionsDismissView = findViewById(R.id.selectionOptionsDismissView);
        clearSelectionButton = (FloatingActionButton) findViewById(R.id.clearSelectionButton);
        tracksSettingsImage = (ImageView) findViewById(R.id.tracksSettingsImage);

        tracksSettingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateInSelectionOptions();
            }
        });

        clearSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackListAdapter.clearSelection();
                animateOutClearSelectionButton();
            }
        });

        if(getSApplication().hasUserRemovedAds()){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) clearSelectionButton.getLayoutParams();
            params.bottomMargin = (int) (25 * getResources().getDisplayMetrics().density);
            clearSelectionButton.setLayoutParams(params);
        }

        AdView tracksAdView = (AdView) findViewById(R.id.tracksAd);
        tracksAdView.loadAd(Util.constructSafeAdRequest());
        setActivityAd(tracksAdView);

        trackListAdapter = new TrackAdapter(this, new ArrayList<Pair<Long, Track>>());

        TextView sendToTopText = (TextView) findViewById(R.id.sendToTopText);
        sendToTopText.setText(R.string.send_to_top);
        sendToTopText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int topPosition = 0;
                int fromPosition = trackListAdapter.getSelectionStart();

                if(topPosition != fromPosition){
                    dispatchReorder(fromPosition, topPosition);
                }

                trackListAdapter.clearSelection();
                animateOutSelectionOptions();
            }
        });

        TextView sendToBottomText = (TextView) findViewById(R.id.sendToBottomText);
        sendToBottomText.setText(R.string.send_to_bottom);
        sendToBottomText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int bottomPosition = trackListAdapter.getItemCount() - 1;
                int fromPosition = trackListAdapter.getSelectionStart();

                if(bottomPosition != fromPosition){
                    dispatchReorder(fromPosition, bottomPosition);
                }

                trackListAdapter.clearSelection();
                animateOutSelectionOptions();
            }
        });

        TextView removeSelectionText = (TextView) findViewById(R.id.removeSelectionText);
        removeSelectionText.setText(R.string.remove_selection);
        removeSelectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackListAdapter.clearSelection();
                animateOutSelectionOptions();
            }
        });

        TextView deleteTracksText = (TextView) findViewById(R.id.deleteTracksText);
        deleteTracksText.setText(R.string.delete_tracks);
        deleteTracksText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trackListAdapter.getSelectionSize() > 100){
                    new NormanDialog(TracksActivity.this, R.string.too_many_delete_error, R.string.Ok, null).show();
                } else {
                    dispatchDelete(trackListAdapter.getSelectionStart(), trackListAdapter.getSelectionEnd());
                    trackListAdapter.clearSelection();
                }

                animateOutSelectionOptions();
            }
        });

        selectionOptionsDismissView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateOutSelectionOptions();
            }
        });

        final DragListView trackList = (DragListView) findViewById(R.id.trackList);
        trackList.setAdapter(trackListAdapter, false);
        trackList.setLayoutManager(new LinearLayoutManager(this));
        trackList.setCanDragHorizontally(false);
        trackList.setCustomDragItem(new TrackDragItem(this, trackList));
        trackList.getRecyclerView().setVerticalScrollBarEnabled(false);
        trackList.setDragListCallback(new DragListView.DragListCallback() {
            @Override
            public boolean canDragItemAtPosition(int dragPosition) {
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int dropPosition) {
                //Disallow dropping on selection
                return !(dropPosition > trackListAdapter.getSelectionStart() && dropPosition < trackListAdapter.getSelectionEnd());
            }
        });

        trackList.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
                trackListAdapter.setDragging(true);
                trackListAdapter.notifyDataSetChanged();

                if(trackListAdapter.hasSelection()){
                    animateOutClearSelectionButton();
                }
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if(fromPosition != toPosition){
                    if(trackListAdapter.hasSelection()){
                        /*
                        * When a user finishes a drag the DragListView will automatically move
                        * the currently dragged DragItem to the correct position
                        * This is problematic with a selection because you now have an
                        * item that is out of sync. We solve this by undoing what
                        * the DragListView does
                        * */
                        trackListAdapter.quietPositionChange(toPosition, fromPosition);
                    }

                    dispatchReorder(fromPosition, toPosition);
                }

                trackListAdapter.setDragging(false);
                trackListAdapter.clearSelection();
            }
        });

        trackList.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {

            private LinearLayout trashLayout;

            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                trashLayout = (LinearLayout) item.findViewById(R.id.trashLayout);
                if(trackListAdapter.hasSelection()){
                    trackListAdapter.clearSelection();
                }

                animateOutClearSelectionButton();
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                if(swipedDirection == ListSwipeItem.SwipeDirection.LEFT){
                    Pair<Long, Track> adapterItem = (Pair<Long, Track>) item.getTag(R.id.PAIR_ID);
                    int position = trackListAdapter.getPositionForItem(adapterItem);
                    dispatchDelete(position, position);
                }
            }

            @Override
            public void onItemSwiping(ListSwipeItem item, float swipedDistanceX) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) trashLayout.getLayoutParams();
                params.width = (int) -swipedDistanceX;
                trashLayout.setLayoutParams(params);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(shouldThreadRun.get()){
                    CallbackGroup<SnapshotId> reorderCallback = modificationCalls.peek();

                    if(reorderCallback != null){
                        if(reorderCallback.getState() == CallbackGroup.CallbackState.WAITING){
                            reorderCallback.executeCallbacks();
                        } else if(reorderCallback.getState() == CallbackGroup.CallbackState.FINISHED){
                            if(reorderCallback.hasFailedCallbacks()){
                                if(reorderCallback.getRetries() < 3){
                                    reorderCallback.retry(CallbackGroup.RetryType.FAILED);
                                } else {
                                    shouldThreadRun.set(false);
                                    TracksActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Util.errorWithFinish(TracksActivity.this, R.string.modify_playlist_error);
                                        }
                                    });
                                }
                            } else {
                                modificationCalls.remove();
                            }
                        }
                    }


                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        if(getSApplication().hasUserRemovedAds()){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) trackList.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            trackList.setLayoutParams(params);
        }

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.tracksFastScroller);
        fastScroller.setRecyclerView(trackList.getRecyclerView());
        fastScroller.setTimeout(2000);
        fastScroller.setScrollAlwaysVisible(false);
        fastScroller.setFadeOutDuration(250);
        trackList.getRecyclerView().setOnScrollListener(fastScroller.getOnScrollListener());

        Bundle extras = getIntent().getExtras();
        playlistId = extras.getString("playlistId");
        String playlistName = extras.getString("playlistName");

        spotify.getPlaylist(currentUserId, playlistId, new Callback<Playlist>() {
            @Override
            public void success(Playlist playlist, Response response) {
                trackAmount = playlist.tracks.total;
                snapshotId.set(playlist.snapshot_id);
                fetchAllPlaylistTracks();
            }

            @Override
            public void failure(RetrofitError error) {
                Util.errorWithFinish(TracksActivity.this, R.string.fetch_playlist_info_error);
            }
        });

        TextView playlistNameText = (TextView) findViewById(R.id.playlistNameText);
        playlistNameText.setText(playlistName);
    }

    @Override
    public void onBackPressed() {
        if(selectionOptionsMainLayout.getVisibility() == View.VISIBLE){
            animateOutSelectionOptions();
        } else {
            shouldThreadRun.set(false);
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        shouldThreadRun.set(false);
        super.onStop();
    }

    public void animateInSelectionOptions(){
        selectionOptionsDismissView.setVisibility(View.VISIBLE);

        Animation fromBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        selectionOptionsMainLayout.startAnimation(fromBottom);
        selectionOptionsMainLayout.setVisibility(View.VISIBLE);

        animateOutClearSelectionButton();
    }

    private void animateOutSelectionOptions(){
        selectionOptionsDismissView.setVisibility(View.GONE);

        Animation slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        selectionOptionsMainLayout.startAnimation(slideOutBottom);
        selectionOptionsMainLayout.setVisibility(View.GONE);

        if(trackListAdapter.hasSelection()){
            animateInClearSelectionButton();
        }
    }

    public void animateInClearSelectionButton(){
        clearSelectionButton.setVisibility(View.VISIBLE);

        Animation fadeInAndScale = AnimationUtils.loadAnimation(this, R.anim.floating_button_in);
        clearSelectionButton.startAnimation(fadeInAndScale);
    }

    public void animateOutClearSelectionButton(){
        Animation fadeOutAndScale = AnimationUtils.loadAnimation(this, R.anim.floating_button_out);
        clearSelectionButton.startAnimation(fadeOutAndScale);
        clearSelectionButton.setVisibility(View.GONE);
    }

    private void dispatchReorder(int fromPosition, int toPosition){
        boolean movedDown = toPosition > fromPosition;
        final Map<String, Object> options = new HashMap<>();

        options.put("range_start", fromPosition);
        options.put("insert_before", toPosition);
        options.put("range_length", 1);

        if(movedDown){
            options.put("insert_before", toPosition + 1);
        }

        if(trackListAdapter.hasSelection()){
            options.put("range_length", trackListAdapter.getSelectionSize());
            trackListAdapter.changeSelectionPosition(fromPosition, toPosition);
        }

        CallbackGroup<SnapshotId> callbackGroup = new CallbackGroup<SnapshotId>() {
            @Override
            public void onAllFinished() {

            }

            @Override
            public void callbackExecution(Callback<SnapshotId> callback) {
                options.put("snapshot_id", snapshotId.get());
                spotify.reorderPlaylistTracks(currentUserId, playlistId, options, callback);
            }
        };

        callbackGroup.addCallback(new SpotifyCallback<SnapshotId>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                //Toast.makeText(TracksActivity.this, spotifyError.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void success(SnapshotId snapshotId, Response response) {
                TracksActivity.this.snapshotId.set(snapshotId.snapshot_id);
            }
        });

        modificationCalls.add(callbackGroup);
    }

    private void dispatchDelete(final int fromPosition, final int toPosition){
        final TracksToRemoveByPosition tracksToRemoveByPosition = new TracksToRemoveByPosition();
        tracksToRemoveByPosition.snapshot_id = snapshotId.get();
        tracksToRemoveByPosition.positions = new ArrayList<>();

        for(int position = fromPosition; position <= toPosition; position++){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    trackListAdapter.removeItem(fromPosition);
                }
            });

            tracksToRemoveByPosition.positions.add(position);
        }

        CallbackGroup<SnapshotId> callbackGroup = new CallbackGroup<SnapshotId>() {
            @Override
            public void onAllFinished() {

            }

            @Override
            public void callbackExecution(Callback<SnapshotId> callback) {
                spotify.removeTracksFromPlaylist(currentUserId, playlistId, tracksToRemoveByPosition, callback);
            }
        };

        callbackGroup.addCallback(new SpotifyCallback<SnapshotId>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                //Toast.makeText(TracksActivity.this, spotifyError.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void success(SnapshotId snapshotId, Response response) {
                TracksActivity.this.snapshotId.set(snapshotId.snapshot_id);
            }

        });

        modificationCalls.add(callbackGroup);
    }

    private void fetchAllPlaylistTracks() {
        final int TRACKS_PER_REQUEST = 100;
        final int neededRequests = (int) Math.ceil((double) trackAmount / (double) TRACKS_PER_REQUEST);

        final ArrayList<Pair<Long, Track>> tracks = new ArrayList<>();
        final AtomicInteger iteration = new AtomicInteger();

        final CallbackGroup<Pager<PlaylistTrack>> callbackGroup = new CallbackGroup<Pager<PlaylistTrack>>() {
            @Override
            public void onAllFinished() {
                if (hasFailedCallbacks()) {
                    Util.errorWithFinish(TracksActivity.this, R.string.fetch_tracks_error);

                } else {
                    trackListAdapter.enableSelection();
                    Util.collapseView(progressLayout, 1000);
                }
            }

            @Override
            public void callbackExecution(Callback<Pager<PlaylistTrack>> callback) {
                Map<String, Object> options = new HashMap<>();
                options.put("offset", iteration.getAndIncrement() * TRACKS_PER_REQUEST);
                options.put("limit", TRACKS_PER_REQUEST);

                spotify.getPlaylistTracks(currentUserId, playlistId, options, callback);

            }
        };

        if(neededRequests == 0){
            Util.collapseView(progressLayout, 1000);
        }

        for (int i = 0; i < neededRequests; i++) {
            callbackGroup.addCallback(new Callback<Pager<PlaylistTrack>>() {
                @Override
                public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                    long trackIndex = playlistTrackPager.offset;
                    for (PlaylistTrack track : playlistTrackPager.items) {
                        tracks.add(new Pair<>(trackIndex, track.track));
                        trackIndex++;
                    }

                    trackListAdapter.setItemList(tracks);
                    double progress = (double) callbackGroup.getSuccessfulCallbacks() / (double) callbackGroup.getSize();
                    progressBarAnimation.setProgress((int) (progress * 100));
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

        callbackGroup.executeCallbacks();

    }

}
