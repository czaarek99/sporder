package net.czaarek99.spotifyreorder;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;

/**
 * Created by Czarek on 2017-04-11.
 */

public class SporderApplication extends Application {

    public static final String REMOVE_ADS_SKU = "remove_ads";

    private final AtomicReference<IInAppBillingService> billingService = new AtomicReference<>();
    private final SpotifyApi spotifyApi = new SpotifyApi();
    private boolean ads = true;
    private UserPrivate spotifyUser;

    public void setAccessToken(String accessToken){
        spotifyApi.setAccessToken(accessToken);
    }

    public UserPrivate getSpotifyUser() {
        return spotifyUser;
    }

    public String getSpotifyUserID(){
        return getSpotifyUser().id;
    }

    public void setSpotifyUser(UserPrivate spotifyUser) {
        this.spotifyUser = spotifyUser;
    }

    public SpotifyApi getSpotifyApi(){
        return spotifyApi;
    }

    public SpotifyService getSpotifyService(){
        return spotifyApi.getService();
    }

    public boolean areAdsEnabled(){
        return ads;
    }

    public void disableAds(){
        ads = false;
    }

    public IInAppBillingService getBillingService(){
        return billingService.get();
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        ServiceConnection mServiceConn = new ServiceConnection() {
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                billingService.set(null);
//            }
//
//            @Override
//            public void onServiceConnected(ComponentName name,
//                                           IBinder service) {
//                billingService.set(IInAppBillingService.Stub.asInterface(service));
//
//                try {
//                    Bundle ownedItems = getBillingService().getPurchases(3, getPackageName(), "inapp", null);
//
//                    int response = ownedItems.getInt("RESPONSE_CODE");
//                    if(response == 0){
//                        ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
//                        if(ownedSkus.contains(REMOVE_ADS_SKU)){
//                            disableAds();
//                        }
//                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        Intent serviceIntent =
//                new Intent("com.android.vending.billing.InAppBillingService.BIND");
//        serviceIntent.setPackage("com.android.vending");
//        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }
}
