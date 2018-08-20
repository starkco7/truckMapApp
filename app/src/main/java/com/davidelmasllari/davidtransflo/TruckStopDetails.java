package com.davidelmasllari.davidtransflo;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

public final class TruckStopDetails {
    private String name;
    private String city;
    private String state;
    private String zip;
    private double lat;
    private double lng;
    private String rawLine1;
    private String rawLine2;
    private String rawLine3;
    private Marker mapMarker;

    public TruckStopDetails(JSONObject truckstopJSONObject) {
        try {
            name = truckstopJSONObject.getString("name");
            city = truckstopJSONObject.getString("city");
            state = truckstopJSONObject.getString("state");
            zip = truckstopJSONObject.getString("zip");
            lat = truckstopJSONObject.getDouble("lat");
            lng = truckstopJSONObject.getDouble("lng");
            rawLine1 = truckstopJSONObject.getString("rawLine1");
            rawLine2 = truckstopJSONObject.getString("rawLine2");
            rawLine3 = truckstopJSONObject.getString("rawLine3");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public LatLng getPoint() {
        return new LatLng(getLat(),getLng());
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getRawLine1() {
        return rawLine1;
    }

    public String getRawLine2() {
        return rawLine2;
    }

    public String getRawLine3() {
        return rawLine3;
    }

    public String getMarkerText() {
        String markerText = "";
        markerText += getName() + "\n";
        markerText += getCity() + ", " + getState() + " " + getZip();
        return markerText;
    }

    public void setMarker(Marker marker) {
        this.mapMarker = marker;
    }
    public void markerVisibility (GoogleMap map, int zooms){
        this.mapMarker.setVisible(map.getCameraPosition().zoom > zooms);
    }
}
