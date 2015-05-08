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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
    private Set<Marker> mMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mMarkers = new HashSet<Marker>();

    //    setUpMapIfNeeded();
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
        if (mMap != null)
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            UiSettings settings = mMap.getUiSettings();
            settings.setTiltGesturesEnabled(false);
            settings.setRotateGesturesEnabled(false);

            mMap.setOnMapLongClickListener(mMapLongClickListener);
            mMap.setOnCameraChangeListener(mOnCameraChangeListener);

            SharedPreferences pref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
            Set<String> markers = pref.getStringSet(PREF_MARKERS, new HashSet<String>());
            for (String marker : markers)
            {
                String[] pieces = marker.split(",");
                if (pieces.length == 3)
                {
                    String title = pieces[0];
                    double lat = Double.parseDouble(pieces[1]);
                    double lon = Double.parseDouble(pieces[2]);
                    LatLng latLng = new LatLng(lat, lon);

                    mMap.addMarker(new MarkerOptions().position(latLng).title(title));
                    double radius = findDistance(mMap.getCameraPosition().target, latLng);
                    mMarkers.add(new Marker(latLng,title,createCircle(latLng,radius)));
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        setUpMap();
    }

    private void saveMarker(LatLng latLng)
    {
        String title = mMessageView.getText().toString();
        if(title.length() == 0)
            title = "Marker";

        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        double radius = findDistance(mMap.getCameraPosition().target, latLng);
        mMarkers.add(new Marker(latLng,title,createCircle(latLng, radius)));

        Set<String> strMarkers = new HashSet<String>();
        for(Marker marker : mMarkers)
        {
            strMarkers.add(marker.toString());
        }

        SharedPreferences pref = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putStringSet(PREF_MARKERS, strMarkers);
        editor.apply();
    }

    private Circle createCircle(LatLng latLng, double meters)
    {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(meters); // In meters

        return mMap.addCircle(circleOptions);
    }

    // equation from: http://andrew.hedges.name/experiments/haversine/
    private double findDistance(LatLng latLng1, LatLng latLng2)
    {
        double lat1 = Math.toRadians(latLng1.latitude);
        double lat2 = Math.toRadians(latLng2.latitude);
        double dlon = Math.toRadians(latLng2.longitude - latLng1.longitude);
        double dlat = Math.toRadians(latLng2.latitude - latLng1.latitude);
        final long R = 6373;
        double a = Math.pow(Math.sin(dlat/2),2) +
                (Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dlon/2),2));
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double d = R * c;

        return d * 1000;
    }

    private GoogleMap.OnMapLongClickListener mMapLongClickListener =
            new GoogleMap.OnMapLongClickListener()
    {
        @Override
        public void onMapLongClick(LatLng latLng)
        {
            saveMarker(latLng);
        }
    };

    private GoogleMap.OnCameraChangeListener mOnCameraChangeListener =
            new GoogleMap.OnCameraChangeListener()
    {
        @Override
        public void onCameraChange(CameraPosition cameraPosition)
        {
            LatLng cameraLatLng = cameraPosition.target;
            for(Marker marker : mMarkers)
            {
                double radius = findDistance(cameraLatLng,marker.getLatLng());
                marker.getCircle().setRadius(radius);
            }
        }
    };
}
