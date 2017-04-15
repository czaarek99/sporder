package net.czaarek99.spotifyreorder.view.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.Switch;

import net.czaarek99.spotifyreorder.activity.SporderActivity;

/**
 * Created by Czarek on 2017-04-15.
 */

public class SwitchSettingView extends SettingView<Switch, Boolean, Switch.OnCheckedChangeListener> {

    public SwitchSettingView(SporderActivity context, String preferenceKey, Boolean defaultState, int titleTextResId, int infoTextResId) {
        super(context, preferenceKey, defaultState, titleTextResId, infoTextResId);
        setSettingItem(new Switch(context));
        loadFromPreference();
    }

    @Override
    public void setOnEvent(Switch.OnCheckedChangeListener event) {
        getSettingItem().setOnCheckedChangeListener(event);
    }

    @Override
    public Boolean getItemState() {
        return getSettingItem().isChecked();
    }

    @Override
    protected void savePreference(String preferenceKey, SharedPreferences.Editor editor) {
        editor.putBoolean(preferenceKey, getItemState());
    }

    @Override
    protected void loadFromPreference() {
        getSettingItem().setChecked(getSavedPreference());
    }
}
