package net.czaarek99.spotifyreorder.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

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

    private static final int REMOVE_ADS_REQUEST_CODE = 1002;
    public static final String SORT_METHOD_ID = "sort_method";
    public static final String ADS_REMOVED_ID = "ads_removed";

    private SpinnerSettingView playlistSortSpinnerView;
    private SwitchSettingView removeAdsSwitchView;
    private LinearLayout settingsActivityContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsActivityContainer = (LinearLayout) findViewById(R.id.settingsActivityContainer);

        TextView settingsTitleText = (TextView) findViewById(R.id.settingsTitle);
        settingsTitleText.setText(R.string.settings);

        playlistSortSpinnerView =
                new SpinnerSettingView(this,
                        SORT_METHOD_ID,
                        getResources().getString(R.string.spotify_sort),
                        R.string.playlist_sorting,
                        R.string.playlist_sorting_info,
                        R.string.alphabetically,
                        R.string.most_tracks,
                        R.string.spotify_sort);

        settingsActivityContainer.addView(playlistSortSpinnerView);

        removeAdsSwitchView = new SwitchSettingView(this, ADS_REMOVED_ID, false,
                R.string.remove_ads, R.string.remove_ads_info);

        final Switch removeAdsSwitch = removeAdsSwitchView.getSettingItem();
        if(getSApplication().hasUserRemovedAds()){
            removeAdsSwitch.setEnabled(false);
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

        settingsActivityContainer.addView(removeAdsSwitchView);

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
        setResult(RESULT_OK, data);

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        playlistSortSpinnerView.savePreference();

        super.onPause();
    }
}
