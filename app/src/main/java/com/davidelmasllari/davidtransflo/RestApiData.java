package com.davidelmasllari.davidtransflo;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class RestApiData {
    public static final String AUTH_TOKEN = "Basic amNhdGFsYW5AdHJhbnNmbG8uY29tOnJMVGR6WmdVTVBYbytNaUp6RlIxTStjNmI1VUI4MnFYcEVKQzlhVnFWOEF5bUhaQzdIcjVZc3lUMitPTS9paU8=";
    public static final String ENDPOINT_URL = "http://webapp.transflodev.com/svc1.transflomobile.com/api/v3/stations/100";

    public static void addJsonData(GoogleMap googleMap, Double latitude,
                                   Double longtitude, Context context) {

        // Request a JSON response from the provided URL.
        JSONObject requestBody = new JSONObject();
        // Pass in parameters to the Request Body
        try {
            // Pass in parameters to the Request Body
            requestBody.put("lng", longtitude);
            requestBody.put("lat", latitude);
            createRequest(requestBody, googleMap, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void createRequest(JSONObject requestBody, final GoogleMap googleMap, final Context context) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.POST,
                ENDPOINT_URL,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("rest-response", response.toString());
                        TruckStopGatheredDetails dataBundle = new TruckStopGatheredDetails(response);
                        dataBundle.addMarkerstoMap(googleMap);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("rest-response", error.toString());

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", AUTH_TOKEN);
                return params;
            }
        };

        requestQueue.add(objectRequest);

    }


}
