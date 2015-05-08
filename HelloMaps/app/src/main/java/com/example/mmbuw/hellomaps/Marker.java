package com.example.mmbuw.hellomaps;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

public class Marker
{
    private LatLng mLatLng;
    private Circle mCircle;
    private String mTitle;

    public Marker(LatLng latLng, String title, Circle circle)
    {
        mLatLng = latLng;
        mTitle = title;
        mCircle = circle;
    }

    public LatLng getLatLng()
    {
        return mLatLng;
    }

    public Circle getCircle()
    {
        return mCircle;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public String toString()
    {
        String lat = Double.toString(mLatLng.latitude);
        String lng = Double.toString(mLatLng.longitude);
        return mTitle + "," + lat + "," + lng;
    }
}
