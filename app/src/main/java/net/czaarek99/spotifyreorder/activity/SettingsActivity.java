package net.czaarek99.spotifyreorder.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.SporderApplication;
import net.czaarek99.spotifyreorder.util.NormanDialog;
import net.czaarek99.spotifyreorder.util.Util;

/**
 * Created by Czarek on 2017-04-11.
 */

public class SettingsActivity extends SporderActivity {

    private static final int REMOVE_ADS_REQUEST_CODE = 1002;

    private TextView removeAdsText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        TextView settingsTitleText = (TextView) findViewById(R.id.settingsTitle);
        settingsTitleText.setText(R.string.settings);

        removeAdsText = (TextView) findViewById(R.id.removeAdsText);

        if(getSApplication().areAdsEnabled()){
            removeAdsText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        IInAppBillingService billingService = getSApplication().getBillingService();

                        if(billingService == null){
                            Util.errorWithFinish(SettingsActivity.this, R.string.billing_failed_error);
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
        } else {
            removeAdsText.setText(R.string.ads_removed);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REMOVE_ADS_REQUEST_CODE && resultCode == RESULT_OK){
            getSApplication().disableAds();
            removeAdsText.setText(R.string.ads_removed);
        }
    }
}
