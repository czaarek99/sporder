package net.czaarek99.spotifyreorder.view.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import net.czaarek99.spotifyreorder.activity.SporderActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Czarek on 2017-04-15.
 */

public class SpinnerSettingView extends SettingView<Spinner, String, AdapterView.OnItemSelectedListener>{

    public SpinnerSettingView(SporderActivity context, String preferenceKey, String defaultValue, int titleTextResId, int infoTextResId, int... items) {
        super(context, preferenceKey, defaultValue, titleTextResId, infoTextResId);
        setSettingItem(new Spinner(getContext()));


        List<String> itemStrings = new ArrayList<>();
        for (int item : items) {
            itemStrings.add(getContext().getResources().getString(item));
        }

        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, itemStrings);
        getSettingItem().setAdapter(spinnerArrayAdapter);
        loadFromPreference();
    }

    @Override
    public String getItemState() {
        return (String) getSettingItem().getSelectedItem();
    }

    @Override
    protected void savePreference(String preferenceKey, SharedPreferences.Editor editor) {
        editor.putString(preferenceKey, getItemState());
    }

    @Override
    public void setOnEvent(AdapterView.OnItemSelectedListener event) {
        getSettingItem().setOnItemSelectedListener(event);
    }

    @Override
    protected void loadFromPreference() {
        SpinnerAdapter adapter = getSettingItem().getAdapter();

        String preference = getSavedPreference();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = (String) adapter.getItem(i);

            if(item.equals(preference)){
                getSettingItem().setSelection(i);
            }

        }
    }
}
