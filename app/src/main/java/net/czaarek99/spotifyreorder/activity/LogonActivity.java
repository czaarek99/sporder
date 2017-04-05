package net.czaarek99.spotifyreorder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.MobileAds;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.util.NormanDialog;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

//TODO: Make this screen look better
public class LogonActivity extends SporderActivity {

    private static final String CLIENT_ID = "ec7df65dbcf5446eb32cd5d10d5e7be3";
    private static final int REQUEST_CODE = 1337;

    private final SpotifyReorder reorder = SpotifyReorder.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);

        //TODO: Move this to theme and test all the way down to API level 16
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        String appId = "ca-app-pub-7701342036796677~8906092947";
        MobileAds.initialize(getApplicationContext(), appId);

        attemptAuthentication();

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAuthentication();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE){
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            AuthenticationResponse.Type responseType = response.getType();
            if(responseType == AuthenticationResponse.Type.TOKEN){
                reorder.setAccessToken(response.getAccessToken());

                fetchUser();
            } else if(responseType == AuthenticationResponse.Type.ERROR){
                new NormanDialog(LogonActivity.this, R.string.log_in_error, R.string.Ok, null).show();
            }
        }
    }

    private void attemptAuthentication(){
        AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, "reorder://callback")
                .setScopes(new String[]{
                        "playlist-read-private",
                        "playlist-read-collaborative",
                        "playlist-modify-private",
                        "playlist-modify-public",
                })
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void fetchUser(){
        final SpotifyService spotifyService = reorder.getService();

        spotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                reorder.setSpotifyUser(userPrivate);
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
