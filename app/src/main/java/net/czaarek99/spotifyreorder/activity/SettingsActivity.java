package net.czaarek99.spotifyreorder.activity;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdView;

import net.com.spotifyreorder.BuildConfig;
import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.SporderApplication;
import net.czaarek99.spotifyreorder.util.NormanDialog;
import net.czaarek99.spotifyreorder.util.Util;
import net.czaarek99.spotifyreorder.view.setting.SpinnerSettingView;
import net.czaarek99.spotifyreorder.view.setting.SwitchSettingView;

/**
 * Created by Czarek on 2017-04-11.
 */

public class SettingsActivity extends SporderActivity {

    private static final int SETTINGS_START_INDEX = 1;
    private static final int REMOVE_ADS_REQUEST_CODE = 1002;
    public static final String SORT_METHOD_ID = "sort_method";
    public static final String ADS_REMOVED_ID = "ads_removed";
    public static final String HIDE_EMPTY_PLAYLISTS_ID = "hide_empty_playlists";

    private SpinnerSettingView playlistSortSpinnerView;
    private SwitchSettingView removeAdsSwitchView;
    private SwitchSettingView hideEmptyPlaylistsSwitchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AdView settingsAd = (AdView) findViewById(R.id.settingsAd);
        settingsAd.loadAd(Util.constructSafeAdRequest());
        setActivityAd(settingsAd);

        TextView settingsTitleText = (TextView) findViewById(R.id.settingsTitle);
        settingsTitleText.setText(R.string.settings);

        LinearLayout rateAppLayout = (LinearLayout) findViewById(R.id.rateAppLayout);
        rateAppLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });

        LinearLayout settingsContainer = (LinearLayout) findViewById(R.id.settingsContainer);

        playlistSortSpinnerView =
                new SpinnerSettingView(this,
                        SORT_METHOD_ID,
                        getResources().getString(R.string.spotify_sort),
                        R.string.playlist_sorting,
                        R.string.playlist_sorting_info,
                        R.string.alphabetically,
                        R.string.most_tracks,
                        R.string.spotify_sort);

        settingsContainer.addView(playlistSortSpinnerView, SETTINGS_START_INDEX);

        removeAdsSwitchView = new SwitchSettingView(this, ADS_REMOVED_ID, false,
                R.string.remove_ads, R.string.remove_ads_info);

        final Switch removeAdsSwitch = removeAdsSwitchView.getSettingItem();
        if(getSApplication().hasUserRemovedAds()){
            removeAdsSwitch.setEnabled(false);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) settingsContainer.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, 0);
            settingsContainer.setLayoutParams(params);
        }

        removeAdsSwitchView.setOnEvent(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                removeAdsSwitch.setChecked(false);

                try {
                    IInAppBillingService billingService = getSApplication().getBillingService();

                    if(billingService == null){
                        new NormanDialog(SettingsActivity.this, R.string.billing_failed_error, R.string.Ok, null).show();
                    } else {
                        Bundle buyIntentBundle = billingService.getBuyIntent(3, getPackageName(),
                                SporderApplication.REMOVE_ADS_SKU, "inapp", "");

                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        startIntentSenderForResult(pendingIntent.getIntentSender(), REMOVE_ADS_REQUEST_CODE, new Intent(), 0, 0, 0);
                    }

                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

        settingsContainer.addView(removeAdsSwitchView, SETTINGS_START_INDEX);

        hideEmptyPlaylistsSwitchView = new SwitchSettingView(this, HIDE_EMPTY_PLAYLISTS_ID, false,
                R.string.hide_empty_playlists, R.string.hide_empty_playlists_info);
        settingsContainer.addView(hideEmptyPlaylistsSwitchView, SETTINGS_START_INDEX);

        TextView versionText = new TextView(this);
        versionText.setText(getString(R.string.running_version, BuildConfig.VERSION_NAME));
        versionText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        versionText.setGravity(Gravity.CENTER);

        settingsContainer.addView(versionText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REMOVE_ADS_REQUEST_CODE && resultCode == RESULT_OK){
            getSApplication().disableAds();
            final Switch removeAdsSwitch = removeAdsSwitchView.getSettingItem();
            removeAdsSwitch.setChecked(true);
            removeAdsSwitch.setEnabled(false);

            removeAdsSwitchView.savePreference();
        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra(SORT_METHOD_ID, playlistSortSpinnerView.getItemState());
        data.putExtra(HIDE_EMPTY_PLAYLISTS_ID, hideEmptyPlaylistsSwitchView.getItemState());

        setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        playlistSortSpinnerView.savePreference();
        hideEmptyPlaylistsSwitchView.savePreference();

        super.onPause();
    }
}
