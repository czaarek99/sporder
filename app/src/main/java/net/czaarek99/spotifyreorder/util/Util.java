package net.czaarek99.spotifyreorder.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.activity.TracksActivity;

/**
 * Created by Czarek on 2017-03-19.
 */

public final class Util {

    private Util() {
    }

    public static void errorWithFinish(final Activity activity, int stringResId){
        new NormanDialog(activity, stringResId, R.string.Ok, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.finish();
            }
        }).show();
    }

    public static AdRequest constructSafeAdRequest(){
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("675D49C3556E566D0CEB9EE0076A6DBC")
                .build();
    }

    public static void collapseView(final View view, int duration) {
        final int initialHeight = view.getMeasuredHeight();

        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    view.setVisibility(View.GONE);
                }else{
                    view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    public static void expandView(final View view, int duration) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        animation.setDuration(duration);
        view.startAnimation(animation);
    }
}
