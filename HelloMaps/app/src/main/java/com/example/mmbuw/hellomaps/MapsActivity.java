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
import com.google.android.gms.maps.model.VisibleRegion;

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

        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.maps,menu);
        View view = menu.findItem(R.id.message).getActionView();
        mMessageView = (EditText) view.findViewById(R.id.messageView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        if(mMap == null) //just do it once
        {
            mMap = googleMap;
            setUpMap();
        }
    }

    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    private void setUpMap()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        UiSettings settings = mMap.getUiSettings();
        settings.setTiltGesturesEnabled(false);
        settings.setRotateGesturesEnabled(false);

        mMap.setOnMapLongClickListener(mMapLongClickListener);
        mMap.setOnCameraChangeListener(mOnCameraChangeListener);

        loadMarkers();
    }



    private void saveMarker(LatLng latLng)
    {
        String title = mMessageView.getText().toString();
        if(title.length() == 0)
            title = "Marker";

        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMarkers.add(new Marker(latLng,title,createCircle(latLng)));

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

    private void loadMarkers()
    {
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
                mMarkers.add(new Marker(latLng,title,createCircle(latLng)));
            }
        }
    }

    private Circle createCircle(LatLng latLng)
    {
        double meters = circleSize(latLng);
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(meters); // In meters

        return mMap.addCircle(circleOptions);
    }

    private double circleSize(LatLng latLng)
    {
        double radius = 0.0;
        VisibleRegion region = mMap.getProjection().getVisibleRegion();

        if(!region.latLngBounds.contains(latLng))
        {
            double latPadding = Math.abs(region.farRight.latitude
                    - region.nearLeft.latitude) * 0.02;
            double lonPadding = Math.abs(region.farRight.longitude
                    - region.nearLeft.longitude) * 0.02;

            double longitude, latitude;
            if(latLng.longitude < region.farLeft.longitude)
                longitude = region.farLeft.longitude + lonPadding;
            else if(latLng.longitude > region.farLeft.longitude
                    && latLng.longitude < region.farRight.longitude)
                longitude = latLng.longitude;
            else
                longitude = region.farRight.longitude - lonPadding;

            if(latLng.latitude < region.nearLeft.latitude)
                latitude = region.nearLeft.latitude + latPadding;
            else if(latLng.latitude > region.nearLeft.latitude
                    && latLng.latitude < region.farLeft.latitude)
                latitude = latLng.latitude;
            else
                latitude = region.farLeft.latitude - latPadding;

            LatLng nearPoint = new LatLng(latitude,longitude);
            radius = findDistance(nearPoint, latLng);
        }
        return radius;
    }

    // equation from: http://andrew.hedges.name/experiments/haversine/
    private double findDistance(LatLng latLng1, LatLng latLng2)
    {
        double lat1 = Math.toRadians(latLng1.latitude);
        double lat2 = Math.toRadians(latLng2.latitude);
        double dlon = Math.toRadians(latLng2.longitude - latLng1.longitude);
        double dlat = Math.toRadians(latLng2.latitude - latLng1.latitude);
        final double R = 6373.0;
        double a = Math.pow(Math.sin(dlat/2.0),2) +
                (Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dlon/2.0),2));
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
            for(Marker marker : mMarkers)
            {
                double radius = circleSize(marker.getLatLng());
                marker.getCircle().setRadius(radius);
            }
        }
    };
}
