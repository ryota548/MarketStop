package com.example.koshibaryouta.marketstop;

import android.content.pm.PackageManager;
import android.media.browse.MediaBrowser;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity
        extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        urlString.append("location=34.6954798,135.6156105");
        urlString.append("&radius=1000");
        urlString.append("&types=grocery_or_supermarket");
        urlString.append("&key=AIzaSyBVBUGCXX4Ot7z8CP4ynTkWXWffDF9QB2k");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(new String(urlString));
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.connect();
                    String str = InputStreamToString(con.getInputStream());
                    // JSONObject に変換します
                    JSONObject json = new JSONObject(str);
                    Log.d("HTTP", json.toString(4));
                } catch(Exception ex) {
                    Log.d("ERROR", "" + ex);
                }
            }
        }).start();
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
}
