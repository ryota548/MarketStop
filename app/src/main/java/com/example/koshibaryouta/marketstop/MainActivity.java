package com.example.koshibaryouta.marketstop;

import android.media.browse.MediaBrowser;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

public class MainActivity
        extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //接続成功時に呼ばれる
    @Override
    public void onConnected(Bundle bundle) {Log.v("接続", "Connected");}

    // 接続が中断された時に呼ばれる。原因は引数で渡される
    @Override
    public void onConnectionSuspended(int i) {Log.d("Test", "中断");}

    // 接続失敗時に呼ばれる。
    @Override
    public void onConnectionFailed(ConnectionResult result) {Log.d("Test", "失敗");}

}
