package org.kll.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements AsyncTaskCompleteListener {

	ToggleButton toggleTracking;
    Button checkin;
	Boolean tracking;
    TextView status;
	SharedPreferences sharedPrefs;
	String network;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		toggleTracking = (ToggleButton) findViewById(R.id.toggle_tracking);
        checkin = (Button) findViewById(R.id.button_check_in);
        status = (TextView) findViewById(R.id.text_tracking_status);

        checkin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                    return;
                }
                new CheckIn(MainActivity.this).execute();


            }
        });

		toggleTracking.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Triggred when the button tracking button is clicked
				if (!tracking) {
					// Start the tracking here
					// check if GPS is on
					LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					if (!locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						buildAlertMessageNoGps();
						return;
					}
					tracking = true;
                    toggleTracking.setChecked(true);
                    status.setText(getResources().getString(R.string.tracking_in_progress));
					startService(new Intent(MainActivity.this,
							TrackingService.class));
					Log.i(network, network);

				} else {
					// Stop the tracking here
					tracking = false;
                    toggleTracking.setChecked(false);
                    status.setText(getResources().getString(R.string.tracking_stopped));
					stopService(new Intent(MainActivity.this,
							TrackingService.class));
				}

			}
		});

	}

	// Alert dialog to propmt user to enable gps
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.gps_disabled))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.action_ok), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						startActivity(new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton(getResources().getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

	@Override
	protected void onResume() {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		network = sharedPrefs.getString("prefnetwork", "wifi");
		tracking = sharedPrefs.getBoolean("preftracking", false);

		if (sharedPrefs.getString("prefusrid", "").equals("")
				|| sharedPrefs.getString("prefusrid", "").equals("0")) {
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
		}

		if (tracking) {
			// If current status is tracking set the Button to allow stopping
			// the Service
            toggleTracking.setChecked(true);
            status.setText(getResources().getString(R.string.tracking_in_progress));

		} else if (!tracking) {
			// If current status is not tracking set button to allow starting
			// the service
            toggleTracking.setChecked(false);
            status.setText(getResources().getString(R.string.tracking_stopped));

		}

		super.onResume();

	}

	@Override
	protected void onPause() {
		if (sharedPrefs.getString("prefusrid", "").equals("")
				|| sharedPrefs.getString("prefusrid", "").equals("0")) {
			finish();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(getBaseContext(), UserSettings.class);
			startActivity(i);
		}
		if (id == R.id.action_logout) {
			if (!tracking) {
				sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this);
				sharedPrefs.edit().putString("prefusrid", "").commit();
				sharedPrefs.edit().putString("prefusername", "").commit();
				sharedPrefs.edit().putString("prefpassword", "").commit();
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.logout_trackingon),
						Toast.LENGTH_LONG).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onTaskComplete(String result) {
        Toast.makeText(this,result,Toast.LENGTH_LONG).show();
    }
}
