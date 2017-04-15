package net.czaarek99.spotifyreorder.view.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.com.spotifyreorder.R;
import net.czaarek99.spotifyreorder.activity.SporderActivity;

/**
 * Created by Czarek on 2017-04-14.
 */

public abstract class SettingView<SettingItem extends View, ItemState, EventType> extends LinearLayout {

    private String preferenceKey;
    private LinearLayout settingItemContainer;
    private SettingItem settingItem;
    private ItemState defaultState;


    public SettingView(SporderActivity context, String preferenceKey, ItemState defaultState, int titleTextResId, int infoTextResId) {
        super(context);
        inflate(getContext(), R.layout.setting_view, this);

        TextView settingTitleText = (TextView) findViewById(R.id.settingTitleText);
        TextView settingDescriptionText = (TextView) findViewById(R.id.settingDescriptionText);
        settingItemContainer = (LinearLayout) findViewById(R.id.settingItemContainer);;

        this.preferenceKey = preferenceKey;
        this.defaultState = defaultState;

        settingTitleText.setText(titleTextResId);
        settingDescriptionText.setText(infoTextResId);
    }

    protected void setSettingItem(SettingItem settingItem){
        this.settingItem = settingItem;
        settingItemContainer.addView(settingItem);
    }

    protected SporderActivity getActivity(){
        return (SporderActivity) getContext();
    }

    protected ItemState getSavedPreference(){
        SharedPreferences preferences = getActivity().getSApplication().getPreferences();
        Object preference = preferences.getAll().get(preferenceKey);

        if(preference == null){
            return defaultState;
        }

        return (ItemState) preference;
    }

    public SettingItem getSettingItem(){
        return settingItem;
    }

    public void savePreference(){
        SharedPreferences preferences = getActivity().getSApplication().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        savePreference(preferenceKey, editor);
        editor.apply();
    }

    public abstract void setOnEvent(EventType event);

    public abstract ItemState getItemState();

    protected abstract void savePreference(String preferenceKey, SharedPreferences.Editor editor);

    protected abstract void loadFromPreference();

}
