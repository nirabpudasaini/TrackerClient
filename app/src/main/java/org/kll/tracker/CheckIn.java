package org.kll.tracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckIn extends AsyncTask<String, Integer, String> implements LocationListener{

    private Activity activity;
    private ProgressDialog dialog;
    private AsyncTaskCompleteListener callback;
    private String result;
    private Location location;
    private Handler handler = null;

    public CheckIn(Activity a) {
        this.activity = a;
        this.callback = (AsyncTaskCompleteListener) a;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new ProgressDialog(activity);
        dialog.setMessage("Checking In...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(
                "http://www.kathmandulivinglabs.org/tracker-kll/checkin.php");

        // get username and password from sharedprefs
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.activity);
        String phoneNumber = sharedPrefs
                .getString("prefnumber", "9808403834");
        String username = sharedPrefs.getString("prefusername", "");
        String id = sharedPrefs.getString("prefusrid", "");
        getCurrentLocation();
        String accuracy = String.valueOf(location.getAccuracy());
        String x = String.valueOf(location.getLatitude());
        String y = String.valueOf(location.getLongitude());
        String timestamp = getCurrentTimeStamp();

        //Message if to send via SMS
        String message = "trackerapp,checkin," + id + "," + username + ","
                + x + "," + y + "," + accuracy + ","
                + timestamp;


        // Building post parameters, key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("name", username));
        nameValuePair.add(new BasicNameValuePair("id", id));
        nameValuePair.add(new BasicNameValuePair("accuracy",accuracy));
        nameValuePair.add(new BasicNameValuePair("X",x));
        nameValuePair.add(new BasicNameValuePair("Y",y));
        nameValuePair.add(new BasicNameValuePair("timestamp",timestamp));

        // Url Encoding the POST parameters
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        // Making HTTP Request
        try {
            HttpResponse response = httpClient.execute(httpPost);
            System.out.println("Response:"+response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                System.out.println("Entity:" + entity);
                if (entity != null) {
                    result = EntityUtils.toString(entity);
                    System.out.println("Result:"+result);
                    result = activity.getResources().getString(R.string.checkin_sucessful);
                }
            }

        } catch (Exception e) {
            // writing exception to log
            e.printStackTrace();
            if(sharedPrefs.getString("prefnetwork","3g").equals("sms")) {
                try {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, message, null, null);
                result = activity.getResources().getString(R.string.checkin_sucessful_sms);
            } catch (Exception exp) {
                exp.printStackTrace();
                result = activity.getResources().getString(R.string.checkin_sms_error);
            }}
            else{
                result = activity.getResources().getString(R.string.no_internet_sms_disabled);
            }
        }

        return result;
    }

    private void getCurrentLocation(){
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        try {
            Looper.prepare();
            handler = new Handler();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 20, this, null);
            if (location != null) {
                lm.removeUpdates(this);
            }
            Looper.loop();

        }catch(Exception e){
            e.printStackTrace();
        }

    }



    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
        callback.onTaskComplete(result);
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdfDate.format(now);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.i("Location", location.toString());
            if (handler != null) {
                handler.getLooper().quit();
            }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

