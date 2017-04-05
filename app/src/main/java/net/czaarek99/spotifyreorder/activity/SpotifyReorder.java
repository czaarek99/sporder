package net.czaarek99.spotifyreorder.activity;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;

/**
 * Created by Czarek on 2017-03-19.
 */

public class SpotifyReorder {

    private static SpotifyReorder instance;

    private final SpotifyApi spotifyApi = new SpotifyApi();
    private UserPrivate spotifyUser;

    public void setAccessToken(String accessToken){
        spotifyApi.setAccessToken(accessToken);
    }

    public UserPrivate getUser() {
        return spotifyUser;
    }

    public void setSpotifyUser(UserPrivate spotifyUser) {
        this.spotifyUser = spotifyUser;
    }

    public SpotifyApi getApi(){
        return spotifyApi;
    }

    public SpotifyService getService(){
        return spotifyApi.getService();
    }

    public static SpotifyReorder getInstance(){
        if(instance == null){
            instance = new SpotifyReorder();
        }

        return instance;
    }


}
