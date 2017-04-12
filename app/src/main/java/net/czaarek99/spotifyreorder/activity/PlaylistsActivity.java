package net.czaarek99.spotifyreorder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.adapter.PlaylistAdapter;
import net.czaarek99.spotifyreorder.util.CallbackGroup;
import net.czaarek99.spotifyreorder.util.Util;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.client.Response;

public class PlaylistsActivity extends SporderActivity {

    private final PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, new ArrayList<PlaylistSimple>());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        AdView playlistsAd = (AdView) findViewById(R.id.playlistsAd);
        playlistsAd.loadAd(Util.constructSafeAdRequest());
        setActivityAd(playlistsAd);

        final RecyclerView playlistList = (RecyclerView) findViewById(R.id.playlistList);
        playlistList.setAdapter(playlistAdapter);
        playlistList.setLayoutManager(new LinearLayoutManager(this));
        playlistList.setHasFixedSize(true);
        playlistList.setNestedScrollingEnabled(false);

        TextView playlistsTitleText = (TextView) findViewById(R.id.playlistsTitleText);
        playlistsTitleText.setText(R.string.playlists);

        ImageView settingsImage = (ImageView) findViewById(R.id.settingsImage);
        settingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        fetchPlaylists(new PlaylistFetchCallback() {
            @Override
            public void onFailure() {
                Util.errorWithFinish(PlaylistsActivity.this, R.string.fetch_playlists_error);
            }

            @Override
            public void onSuccess(List<PlaylistSimple> playlists) {
                /*
                After we finish fetching the playlists initially we schedule a task
                that updates them periodically
                 */

                scheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        fetchPlaylists(new PlaylistFetchCallback() {
                            @Override
                            public void onSuccess(List<PlaylistSimple> playlists) {
                                //Toast.makeText(PlaylistsActivity.this, "Updated playlists", Toast.LENGTH_LONG).show();
                                playlistAdapter.setItemList(playlists);
                            }
                        }, new PlaylistFetchCallback());
                    }
                }, 20, 20, TimeUnit.SECONDS);
            }
        }, new PlaylistFetchCallback() {
            @Override
            public void onSuccess(List<PlaylistSimple> playlists) {
                playlistAdapter.setItemList(playlists);
            }
        });
    }

    @Override
    protected void onStop() {
        scheduler.shutdown();
        playlistAdapter.killImageThreads();
        super.onStop();
    }

    private void fetchPlaylists(final PlaylistFetchCallback finishCallback, final PlaylistFetchCallback updateCallback) {
        final SpotifyService spotify = getSApplication().getSpotifyService();
        final int PLAYLISTS_PER_REQUEST = 50;

        final AtomicInteger iteration = new AtomicInteger();
        final String userId = getSApplication().getSpotifyUser().id;
        final List<PlaylistSimple> playlists = new ArrayList<>();

        //First get the total playlist amount
        spotify.getMyPlaylists(new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                int playlistCount = playlistSimplePager.total;
                int neededRequests = (int) Math.ceil((double) playlistCount / (double) PLAYLISTS_PER_REQUEST);

                final CallbackGroup<Pager<PlaylistSimple>> callbackGroup = new CallbackGroup<Pager<PlaylistSimple>>() {
                    @Override
                    public void onAllFinished() {
                        if (hasFailedCallbacks()) {
                            if(getRetries() < 4){
                                retry(RetryType.FAILED);
                            } else {
                                finishCallback.onFailure();
                            }
                        } else {
                            finishCallback.onSuccess(playlists);
                        }
                    }

                    @Override
                    public void callbackExecution(Callback<Pager<PlaylistSimple>> callback) {
                        Map<String, Object> options = new HashMap<>();
                        options.put("offset", iteration.getAndIncrement() * PLAYLISTS_PER_REQUEST);
                        options.put("limit", PLAYLISTS_PER_REQUEST);

                        spotify.getMyPlaylists(options, callback);
                    }
                };

                for (int i = 0; i < neededRequests; i++) {
                    callbackGroup.addCallback(new SpotifyCallback<Pager<PlaylistSimple>>() {
                        @Override
                        public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                            //Web API only allows editing of playlists that the user owns so we remove ones that they can't
                            for (PlaylistSimple playlist : playlistSimplePager.items) {
                                if (playlist.owner.id.equals(userId)) {
                                    playlists.add(playlist);
                                }
                            }

                            updateCallback.onSuccess(playlists);
                        }

                        @Override
                        public void failure(SpotifyError spotifyError) {
                            updateCallback.onFailure();
                        }
                    });
                }

                callbackGroup.executeCallbacks();
            }

            @Override
            public void failure(SpotifyError spotifyError) {
                finishCallback.onFailure();
            }
        });
    }

    private class PlaylistFetchCallback {

        public void onSuccess(List<PlaylistSimple> playlists){}

        void onFailure(){}

    }
}
