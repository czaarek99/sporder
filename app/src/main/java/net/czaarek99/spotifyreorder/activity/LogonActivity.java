package net.czaarek99.spotifyreorder.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.MobileAds;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.SporderApplication;
import net.czaarek99.spotifyreorder.util.NormanDialog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

//TODO: Make this screen look better
public class LogonActivity extends SporderActivity {

    private static final String CLIENT_ID = "ec7df65dbcf5446eb32cd5d10d5e7be3";
    private static final int SPOTIFY_AUTH_REQUEST_CODE = 1000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean runSessionChecks = new AtomicBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/CircularStd-Book.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        String appId = "ca-app-pub-7701342036796677~8906092947";
        MobileAds.initialize(getApplicationContext(), appId);

        attemptAuthentication(SPOTIFY_AUTH_REQUEST_CODE);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAuthentication(SPOTIFY_AUTH_REQUEST_CODE);
            }
        });

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(runSessionChecks.get()) {
                    getSApplication().getSpotifyService().getMe(new Callback<UserPrivate>() {
                        @Override
                        public void success(UserPrivate userPrivate, Response response) {

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (error.getResponse().getStatus() == 401) {
                                runSessionChecks.set(false);
                                new NormanDialog(currentActivity.get(), R.string.auth_token_error, R.string.Ok, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        Intent intent = new Intent(getApplicationContext(), LogonActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }).show();
                            }
                        }
                    });
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SPOTIFY_AUTH_REQUEST_CODE){
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            AuthenticationResponse.Type responseType = response.getType();

            if(responseType == AuthenticationResponse.Type.TOKEN){
                getSApplication().setAccessToken(response.getAccessToken());
                fetchUser();
                runSessionChecks.set(true);
            } else if(responseType == AuthenticationResponse.Type.ERROR){
                new NormanDialog(LogonActivity.this, R.string.log_in_error, R.string.Ok, null).show();
            }
        }
    }

    private void attemptAuthentication(int requestCode){
        AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, "reorder://callback")
                .setScopes(new String[]{
                        "playlist-read-private",
                        "playlist-read-collaborative",
                        "playlist-modify-private",
                        "playlist-modify-public",
                })
                .build();

        AuthenticationClient.openLoginActivity(this, requestCode, request);
    }

    private void fetchUser(){
        final SpotifyService spotifyService = getSApplication().getSpotifyService();

        spotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                getSApplication().setSpotifyUser(userPrivate);
                Intent intent = new Intent(getApplicationContext(), PlaylistsActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                new NormanDialog(LogonActivity.this, R.string.fetch_user_data_error, R.string.Ok, null).show();
            }
        });
    }
}
