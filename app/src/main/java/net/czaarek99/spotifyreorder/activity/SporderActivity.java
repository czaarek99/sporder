package net.czaarek99.spotifyreorder.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdView;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.SporderApplication;

import java.lang.ref.WeakReference;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Czarek on 2017-04-02.
 */

public abstract class SporderActivity extends AppCompatActivity {

    protected static WeakReference<SporderActivity> currentActivity;

    private AdView activityAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void startActivity(Intent intent) {
        currentActivity = new WeakReference<>(this);
        super.startActivity(intent);
        overridePendingTransition(R.anim.enter_activity_slide_in, R.anim.enter_activity_slide_out);
    }

    @Override
    protected void onResume() {
        currentActivity = new WeakReference<>(this);
        updateAdVisibility();

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.leave_activity_slide_in, R.anim.leave_activity_slide_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.leave_activity_slide_in, R.anim.leave_activity_slide_out);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void updateAdVisibility(){
        if(!getSApplication().areAdsEnabled()){
            activityAd.getLayoutParams().height = 1;
            activityAd.setVisibility(View.INVISIBLE);
        }
    }

    protected void setActivityAd(AdView ad){
        activityAd = ad;
        updateAdVisibility();

    }

    public SporderApplication getSApplication(){
        return (SporderApplication) super.getApplication();
    }

}
