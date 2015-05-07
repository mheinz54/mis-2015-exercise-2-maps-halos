package com.example.mmbuw.hellomaps;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends Activity implements OnMapReadyCallback
{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private EditText mMessageView;
    private static final String SHARED_PREF = "MY_MAP_APP";
    private static final String PREF_MARKERS = "PREF_MARKERS";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.maps,menu);
        View view = (View) menu.findItem(R.id.message).getActionView();
        mMessageView = (EditText) view.findViewById(R.id.messageView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        UiSettings settings = mMap.getUiSettings();
        settings.setTiltGesturesEnabled(false);
        settings.setRotateGesturesEnabled(false);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng latLng)
            {
                String title = mMessageView.getText().toString();
                if(title.length() == 0)
                    title = "Marker";
                mMap.addMarker(new MarkerOptions().position(latLng).title(title));
                saveMarker(title,latLng);
            }
        });

        SharedPreferences pref = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
        Set<String> markers = pref.getStringSet(PREF_MARKERS,new HashSet<String>());
        for(String marker : markers)
        {
            String[] pieces = marker.split(",");
            if(pieces.length == 3)
            {
                double lat = Double.parseDouble(pieces[1]);
                double lon = Double.parseDouble(pieces[2]);
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).title(pieces[0]));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        setUpMap();
    }

    private void saveMarker(String title, LatLng latLng)
    {
        SharedPreferences pref = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
        Set<String> markers = pref.getStringSet(PREF_MARKERS,new HashSet<String>());
        String lat = Double.toString(latLng.latitude);
        String lng = Double.toString(latLng.longitude);
        markers.add(title + "," + lat + "," + lng);

        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putStringSet(PREF_MARKERS, markers);
        editor.apply();
    }
}
