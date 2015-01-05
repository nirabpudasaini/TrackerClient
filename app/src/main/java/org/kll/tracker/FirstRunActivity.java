package org.kll.tracker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FirstRunActivity extends Activity {

    Button btnWelcome,btnSetSms, btnSetInterval;
    LinearLayout llWelcome, llSetSms, llSetInterval;
    CheckBox chkSms;
    EditText txtInterval;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstrun);
        btnWelcome = (Button) findViewById(R.id.btn_welcome_ok);
        btnSetSms = (Button) findViewById(R.id.btn_set_sms);
        btnSetInterval = (Button) findViewById(R.id.btn_set_interval);

        llWelcome = (LinearLayout) findViewById(R.id.linearlayout_welcome);
        llSetSms = (LinearLayout) findViewById(R.id.linearlayout_set_sms);
        llSetInterval = (LinearLayout) findViewById(R.id.linearlayout_set_interval);

        chkSms = (CheckBox) findViewById(R.id.chk_sms);

        txtInterval = (EditText) findViewById(R.id.edittext_interval);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(FirstRunActivity.this);

        btnWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llWelcome.setVisibility(View.GONE);
                llSetSms.setVisibility(View.VISIBLE);
            }
        });

        btnSetSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (chkSms.isChecked()){
                    sharedPrefs.edit().putString("prefnetwork","sms").commit();
                }else{
                    sharedPrefs.edit().putString("prefnetwork","3g").commit();
                }
                llSetSms.setVisibility(View.GONE);
                llSetInterval.setVisibility(View.VISIBLE);
            }
        });

        btnSetInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String interval = txtInterval.getText().toString();
                if (!interval.equals("")){
                    sharedPrefs.edit().putString("prefinterval",interval).commit();
                    sharedPrefs.edit().putBoolean("firstrun", false).commit();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.interval_empty),Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
