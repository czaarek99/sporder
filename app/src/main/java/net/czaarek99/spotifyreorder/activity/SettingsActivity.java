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

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.SporderApplication;

/**
 * Created by Czarek on 2017-04-11.
 */

public class SettingsActivity extends SporderActivity {

    private static final int REMOVE_ADS_REQUEST_CODE = 1002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        TextView settingsTitleText = (TextView) findViewById(R.id.settingsTitle);
        settingsTitleText.setText(R.string.settings);

        TextView removeAdsText = (TextView) findViewById(R.id.removeAdsText);
        removeAdsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle buyIntentBundle = getSApplication().getBillingService().getBuyIntent(3, getPackageName(),
                            SporderApplication.REMOVE_ADS_SKU, "inapp", "");

                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    startIntentSenderForResult(pendingIntent.getIntentSender(), REMOVE_ADS_REQUEST_CODE, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REMOVE_ADS_REQUEST_CODE && resultCode == RESULT_OK){
            getSApplication().disableAds();
        }
    }
}
