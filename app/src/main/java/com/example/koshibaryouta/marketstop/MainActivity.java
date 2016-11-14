package com.example.koshibaryouta.marketstop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity
        implements LocationListener{

    Button stopButton;
    private LocationManager locationManager = null;
    Double nowLatitude;
    Double nowLongtitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("stopButton", "サービスの終了");
            }
        });

        getLocationPoint();
        getShopInfo();
    }

    public void getShopInfo(){
        if (nowLongtitude != null && nowLatitude != null) {

            final StringBuilder urlString = new StringBuilder();
            urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            urlString.append("location=" + nowLatitude + "," + nowLongtitude);
            urlString.append("&radius=1000");
            urlString.append("&types=grocery_or_supermarket");
            urlString.append("&key=AIzaSyBVBUGCXX4Ot7z8CP4ynTkWXWffDF9QB2k");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(new String(urlString));
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.connect();
                        String str = InputStreamToString(con.getInputStream());
                        // JSONObject に変換します
                        JSONObject json = new JSONObject(str);
                        parseJson(json);
                        Log.d("HTTP", json.toString(4));
                    } catch (Exception ex) {
                        Log.d("ERROR", "" + ex);
                    }
                }
            }).start();
        }
    }

    // InputStream -> String
    static String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    public ArrayList parseJson(JSONObject jsonObject){
        ArrayList storeList = new ArrayList();

        try {
            JSONArray results = jsonObject.getJSONArray("results");
            for(int i = 0; i < results.length(); i++){
                JSONObject result = results.getJSONObject(i);
                String name = result.getString("name");
                Log.d("parseJson", name);
                storeList.add(name);
            }
        }catch (Exception ex){
            Log.d("ERROR", "" + ex);
        }
        return storeList;
    }

    public void getLocationPoint() {
        // GPSサービス取得
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager != null){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            5000,
                            1,
                            this);
                    Log.d("isProviderEnabled", "Wifiとか使えるやつ");
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    Log.d("isProviderEnabled", "GPSとか使えるやつ");
                }
            }
        }
    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onLocationChanged(Location location) {

        nowLatitude = location.getLatitude();
        nowLongtitude = location.getLongitude();
        Log.d("onLocationChanged", "緯度：" + nowLatitude + " 経度：" + nowLongtitude);
        getShopInfo();
    }

    @Override
    protected void onStop(){
        super.onDestroy();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
            Log.d("onStop", "終了: " + locationManager);
        }
    }
}
