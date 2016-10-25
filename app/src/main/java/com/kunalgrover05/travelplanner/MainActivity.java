package com.kunalgrover05.travelplanner;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;


public class MainActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    public GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MainLoginFragment fragment = new MainLoginFragment();
        getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();


        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

}