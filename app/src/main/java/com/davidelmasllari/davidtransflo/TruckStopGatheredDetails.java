package com.davidelmasllari.davidtransflo;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class TruckStopGatheredDetails {
    public static ArrayList<TruckStopDetails> truckstops = new ArrayList<>();

    public TruckStopGatheredDetails(JSONObject apiData) {
        try {
            JSONArray items = apiData.getJSONArray("truckStops");
            for (int i = 0; i < items.length(); i++) {
                truckstops.add(new TruckStopDetails(items.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addMarkerstoMap(GoogleMap googleMap) {
        for (int i = 0; i < truckstops.size(); i++) {

            MarkerOptions options = new MarkerOptions();
            options.title(truckstops.get(i).getMarkerText());
            options.position(truckstops.get(i).getPoint());
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_truck_marker));
            truckstops.get(i).setMarker(googleMap.addMarker(options));
            Log.d("TRUCKDAVE","truckstops" + truckstops);
        }
    }

}
