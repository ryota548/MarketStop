package com.example.koshibaryouta.marketstop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity
        extends AppCompatActivity
        implements LocationListener{

    TextView placeTextView;
    TextView providerTextView;
    ListView placeListView;
    Button stopButton;

    private LocationManager locationManager = null;
    int minMillSec = 3000;
    int minMater = 1;

    Double nowLatitude;
    Double nowLongitude;
    ArrayList<ToPlace> toPlaceList = new ArrayList<>();
    ArrayList<String> placeNameList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placeTextView    = (TextView) findViewById(R.id.PlaceTextView);
        providerTextView = (TextView) findViewById(R.id.ProviderTextView);
        placeListView    = (ListView) findViewById(R.id.placeListView);
        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("stopButton", "サービスの終了");
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, placeNameList);
        placeListView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();

        getLocationPoint();
        getShopInfo();
    }

    public void firebase(){

        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        database.getReference("Date").setValue(df.format(date));
        database.getReference("shopList").setValue(toPlaceList);

        Log.d("FIREBASE", "setValueしたつもり");
    }

    public void getLocationPoint() {

        // GPSサービス取得
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager != null){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            minMillSec,
                            minMater,
                            this);
                    Log.d("isProviderEnabled", "GPSとか使えるやつ" + locationManager);
                    providerTextView.setText("GPS");
                }else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            minMillSec,
                            minMater,
                            this);
                    Log.d("isProviderEnabled", "Wifiとか使えるやつ");
                    providerTextView.setText("NETWORK");
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        nowLatitude  = location.getLatitude();
        nowLongitude = location.getLongitude();
        placeTextView.setText("緯度：" + nowLatitude + " 経度：" + nowLongitude);
        Log.d("getShopInfo", "緯度：" + nowLatitude + " 経度：" + nowLongitude);
        getShopInfo();
        if(toPlaceList != null) {

            placeNameList.clear();
            Double mostCloseDistance = 0.5;
            for (ToPlace place : toPlaceList) {

                placeNameList.add(place.getShopName());
                Double distance = calculatePoint(nowLatitude, nowLongitude, place.getLatitude(), place.getLongitude());
                Log.d("onLocationChanged", "奈良先端大までの距離" + calculatePoint(nowLatitude, nowLongitude, 34.732615, 135.7317214));
                Log.d("onLocationChanged",
                        "店名" + place.getShopName()
                                + " 緯度：" + place.getLatitude()
                                + " 経度：" + place.getLongitude()
                                + " 距離：" + distance
                );
                if (mostCloseDistance > distance && distance <= 0.010) {
                    mostCloseDistance = distance;
                    stopButton.setText(place.getShopName());
                    stopButton.setBackgroundColor(Color.GREEN);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    public Double calculatePoint(Double nowLatitude, Double nowLongitude, Double toLatitude, Double toLongitude){

        Double calculatedLatitude = Math.pow((nowLatitude - toLatitude), 2.0);
        Double calculatedLongitude = Math.pow((nowLongitude - toLongitude), 2.0);
        Double calculatedPoint = Math.pow((calculatedLatitude + calculatedLongitude), 0.5);

        return  calculatedPoint;
    }

    public void getShopInfo(){

        if (nowLongitude != null && nowLatitude != null) {

            final StringBuilder urlString = new StringBuilder();
            urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            urlString.append("location=" + nowLatitude + "," + nowLongitude);
            urlString.append("&radius=1500");
            urlString.append("&types=grocery_or_supermarket|convenience_store");
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
                    } catch (Exception ex) { Log.d("httpERROR", "" + ex); }
                }
            }).start();
        }
    }

    // InputStream -> String
    static String InputStreamToString(InputStream is) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder  sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {

            sb.append(line);
        }
        br.close();

        return sb.toString();
    }

    public void parseJson(JSONObject jsonObject){

        try {

            JSONArray results = jsonObject.getJSONArray("results");
            toPlaceList.clear();
            for(int i = 0; i < results.length(); i++) {

                JSONObject result = results.getJSONObject(i);
                ToPlace toPlace = new ToPlace(
                        result.getString("name"),
                        result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                        result.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                );
                toPlaceList.add(toPlace);
            }
            firebase();
        }catch (Exception ex){ Log.d("parseERROR", "" + ex); }
    }

    @Override
    protected void onStop(){
        super.onStop();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
            locationManager = null;
            Log.d("onStop", "終了: " + locationManager);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}
}
