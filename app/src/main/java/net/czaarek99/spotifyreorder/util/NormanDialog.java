package net.czaarek99.spotifyreorder.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.com.spotifyreorder.R;

/**
 * Created by Czarek on 2017-04-03.
 */

public class NormanDialog extends Dialog {

    public NormanDialog(Context context, int errorTextResId, int buttonTextResId, OnDismissListener dismissListener) {
        super(context);
        setContentView(R.layout.norman_error_dialog);

        Button dismissButton = (Button) findViewById(R.id.normanDismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        dismissButton.setText(buttonTextResId);

        TextView errorText = (TextView) findViewById(R.id.errorText);
        errorText.setText(errorTextResId);

        TextView errorTitleText = (TextView) findViewById(R.id.errorTitleText);
        errorTitleText.setText(R.string.error);

        setOnDismissListener(dismissListener);
    }

}
