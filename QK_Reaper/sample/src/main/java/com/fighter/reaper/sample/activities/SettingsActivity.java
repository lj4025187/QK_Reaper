package com.fighter.reaper.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.SpManager;
import com.fighter.reaper.sample.config.SampleConfig;

/**
 * Created by jia on 8/3/17.
 */
public class SettingsActivity extends Activity {

    private Switch mHoldAdSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mHoldAdSwitch = (Switch) findViewById(R.id.id_settings_hold_ad);
        mHoldAdSwitch.setChecked(SpManager.getInstance(getApplicationContext()).getBooleanTrue(SampleConfig.KEY_NEED_HOLD_AD));
        mHoldAdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpManager.getInstance(getApplicationContext()).saveBoolean(SampleConfig.KEY_NEED_HOLD_AD, isChecked);
            }
        });
    }
}
