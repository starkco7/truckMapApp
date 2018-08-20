package com.davidelmasllari.davidtransflo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener {
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private Button trackingBtn;
    private Marker lastClicked = null;
    private LocationManager locationManager;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SharedPreferences prefs;
    private int mapTypePref = 0;
    private Location currentLocation;
    private boolean trackingModeOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);
        trackingBtn = findViewById(R.id.tracking_mode);

        // call permissions
        getLocationPermission();
        if (!mLocationPermissionGranted) {
            Toast.makeText(MapsActivity.this, "You need to accept permissions for the app to work properly",
                    Toast.LENGTH_SHORT).show();
            getLocationPermission();
        }
        Log.d(TAG, "onCreate: getLocationPermission map");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //preferences for mapType and tracking mode on/off
        mapTypePref = prefs.getInt("mapTypePref", 0);
        trackingModeOn = prefs.getBoolean("trackingModePref", false);
        if(!trackingModeOn){
            trackingBtn.setBackground(getDrawable(R.drawable.ic_tracking_mode));
        } else {
            trackingBtn.setBackground(getDrawable(R.drawable.ic_tracking_mode_red));
        }

        Button mapTypeBtn = findViewById(R.id.ic_maptype);
        if (mapTypePref == 1) {
            mapTypeBtn.setBackground(getDrawable(R.drawable.ic_satellite_map));
        } else {
            mapTypeBtn.setBackground(getDrawable(R.drawable.ic_road_map));
        }


        // Gps button to trigger Current Location
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");

                if (!mLocationPermissionGranted) {
                    Toast.makeText(MapsActivity.this, "You need to accept permissions for the app to work properly",
                            Toast.LENGTH_SHORT).show();
                    getLocationPermission();
                }
                getDeviceLocation(DEFAULT_ZOOM);
            }
        });

    }

    //initializing mapClickListener, CameraMoveListener, map windowInfo adapter
    private void init() {
        Log.d(TAG, "init: initializing");
        //when user clicks on marker, onMarkerClick is called
        mMap.setOnMarkerClickListener(this);
/*        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastClicked=null;
            }
        });*/
/*        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int var) {
                Log.d(TAG, "camera moved");
                      for (int i = 0; i < TruckStopGatheredDetails.truckstops.size(); i++) {
                    TruckStopGatheredDetails.truckstops.get(i).markerVisibility(mMap, 15);

                }
            }
        });*/
        //todo put this onCreate ?
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == KeyEvent.ACTION_DOWN
                        || actionId == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        if(mMap != null){
            // Setting a custom info window adapter for the google map
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                // Use default InfoWindow frame
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                // Defines the contents of the InfoWindow
                @Override
                public View getInfoContents(Marker arg0) {

                    return infoWindowView(arg0);
                }
            });
        }
        hideSoftKeyboard();
    }

    //locate TruckStop according to the search input. Implementing the search functionality
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();
        for (int i = 0; i < TruckStopGatheredDetails.truckstops.size(); i++) {


        if(!searchString.equals("")) {

            if (TruckStopGatheredDetails.truckstops.get(i).getCity().equalsIgnoreCase(searchString)) {
                moveCamera(new LatLng(TruckStopGatheredDetails.truckstops.get(i).getLat(),
                                TruckStopGatheredDetails.truckstops.get(i).getLng()), DEFAULT_ZOOM,
                        "searchString geoLocate");
            } else if (TruckStopGatheredDetails.truckstops.get(i).getState().equalsIgnoreCase(searchString)) {
                moveCamera(new LatLng(TruckStopGatheredDetails.truckstops.get(i).getLat(),
                                TruckStopGatheredDetails.truckstops.get(i).getLng()), DEFAULT_ZOOM,
                        "searchString geoLocate");
            } else if (TruckStopGatheredDetails.truckstops.get(i).getZip().equalsIgnoreCase(searchString)) {
                moveCamera(new LatLng(TruckStopGatheredDetails.truckstops.get(i).getLat(),
                                TruckStopGatheredDetails.truckstops.get(i).getLng()), DEFAULT_ZOOM,
                        "searchString geoLocate");
            } else if (TruckStopGatheredDetails.truckstops.get(i).getName().toLowerCase().contains(searchString.toLowerCase())) {
                moveCamera(new LatLng(TruckStopGatheredDetails.truckstops.get(i).getLat(),
                                TruckStopGatheredDetails.truckstops.get(i).getLng()), DEFAULT_ZOOM,
                        "searchString geoLocate");
            } else {
                Log.d(TAG, "geoLocate: No Match Found");
                //Toast.makeText(this, "No Match Found", Toast.LENGTH_SHORT).show();
            }
        }


        }
    }

    //get device current location
    private void getDeviceLocation(final float zoom) {
        Log.d(TAG, "getDeviceLocation: getting the current device location");


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {

                final com.google.android.gms.tasks.Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            currentLocation = (Location) task.getResult();
                            Log.d(TAG,"cuurloc= " + currentLocation);
                            if (currentLocation != null) {

                                //clear the previous markers from the map before moving camera
                                mMap.clear();

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        zoom,
                                        "My Location");
                                //adding markers to map
                                RestApiData.addJsonData(mMap, currentLocation.getLatitude(),
                                        currentLocation.getLongitude(), MapsActivity.this);
                            } else {
                                Log.d(TAG, "onComplete: COULDNT find location!");
                                boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                if(!gpsIsEnabled){
                                    alertTurnOnGps(MapsActivity.this);
                                    Toast.makeText(MapsActivity.this, "unable to get current location," +
                                                    " turn on your GPS",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MapsActivity.this, "No Last known location, " +
                                                    "please wait for gps to find location first",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }


                        } else {
                            Log.d(TAG, "onComplete: COULDNT found location!");
                            Toast.makeText(MapsActivity.this, "unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception" + e.getMessage());
        }
    }

    //alert dialog for opening GPS Settings on the phone
    private void alertTurnOnGps(Context context) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Turn ON GPS?")
                .setMessage("You need GPS for this feature")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with intent
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    //move camera and add a custom marker for my location
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving camera to: lat:" + latLng.latitude + ", lmg:" + latLng.longitude);


        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        //replaced moveCamera with animate for a smoother transition
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));


        if (title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc_marker))
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    //initializing map
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }
    //get location permissions
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                //if true initMap
                initMap();
                //permS=true;
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    // initialize our map
                    initMap();
                }
            }
        }
    }

    //custom info Window view
    private View infoWindowView(Marker arg0){

        // Getting view from the layout file info_window_layout
        View v = getLayoutInflater().inflate(R.layout.windowlayout, null);

        // Getting the position from the marker
        LatLng latLng = arg0.getPosition();

        TextView tvName = (TextView) v.findViewById(R.id.tv_name);
        TextView tvAddress = (TextView) v.findViewById(R.id.tv_address);
        TextView tvDist = (TextView) v.findViewById(R.id.tv_distance);
        TextView tvCity = (TextView) v.findViewById(R.id.tv_city);
        TextView tvState = (TextView) v.findViewById(R.id.tv_state);
        TextView tvZip = (TextView) v.findViewById(R.id.tv_zip);
        TextView tvOther = (TextView) v.findViewById(R.id.tv_other_info);
        for (int i = 0; i < TruckStopGatheredDetails.truckstops.size(); i++) {
            String truckName = TruckStopGatheredDetails.truckstops.get(i).getName();
            String truckAddress = TruckStopGatheredDetails.truckstops.get(i).getRawLine1();
            String truckCity = TruckStopGatheredDetails.truckstops.get(i).getCity();
            String truckState = TruckStopGatheredDetails.truckstops.get(i).getState();
            String truckZip = TruckStopGatheredDetails.truckstops.get(i).getZip();
            Location truckLoc = new Location("");
            truckLoc.setLatitude(TruckStopGatheredDetails.truckstops.get(i).getLat());
            truckLoc.setLongitude(TruckStopGatheredDetails.truckstops.get(i).getLng());
            int truckDistance = 0;
            if (currentLocation != null) {
                truckDistance = (int) currentLocation.distanceTo(truckLoc);
                //meters to miles
                truckDistance = (int) (truckDistance / 1609.34);
            }

            String truckOther = TruckStopGatheredDetails.truckstops.get(i).getRawLine2();

            if (TruckStopGatheredDetails.truckstops.get(i).getPoint().longitude ==
                    latLng.longitude && TruckStopGatheredDetails.truckstops.get(i).getPoint().latitude ==
                    latLng.latitude) {
                tvName.setText("Name: " + truckName);
                tvAddress.setText("Address: " + truckAddress);
                tvCity.setText("City: " + truckCity);
                tvState.setText("State: " + truckState);
                tvZip.setText("Zip: " + truckZip);
                tvDist.setText("Distance from you: " + truckDistance + " miles");
                if (!TruckStopGatheredDetails.truckstops.get(i).getRawLine2().equals("")) {
                    tvOther.setText("Other Info: " + truckOther);
                    if (!TruckStopGatheredDetails.truckstops.get(i).getRawLine3().equals("")) {
                        tvOther.append(" Extra info: " + TruckStopGatheredDetails.truckstops.get(i).getRawLine3());
                    }
                }
            }
        }


        // Returning the view containing InfoWindow contents
        return v;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mapTypePref == 1) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if (mLocationPermissionGranted) {
            getDeviceLocation(DEFAULT_ZOOM);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            //set this to false to remove default blue marker
            mMap.setMyLocationEnabled(false);
            //because we cant change position of Google default Mylocation button and the searchbar will block it otherwise.
            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
            //geolocationg places on google maps. replace this with Transflo truck stops
            init();
        }

        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onPause() {
        /*        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }*/

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        //initialize location manager for tracking
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (networkIsEnabled) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        1000L, 0, this);
            } else if (gpsIsEnabled) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000L, 10F, this);
            }  else {
                //Show an error dialog that GPS is disabled.
                Toast.makeText(this, "Gps is disabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Show a generic error dialog since LocationManager is null for some reason
            Toast.makeText(this, "Loc manager is null", Toast.LENGTH_SHORT).show();

        }

    }




    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "loc changed", Toast.LENGTH_SHORT).show();
        if(currentLocation != null && trackingModeOn){
            //moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),20,"My Location");
            getDeviceLocation(20);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Toast.makeText(this, "Marker Clicked", Toast.LENGTH_SHORT).show();

        if(!marker.getTitle().equals("My Location")){
            //adding markers to map
            if(currentLocation != null){
                RestApiData.addJsonData(mMap, currentLocation.getLatitude(),
                        currentLocation.getLongitude(), MapsActivity.this);
            }
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_clicked_truck));


        } else {
            Toast.makeText(this, "Your Location", Toast.LENGTH_SHORT).show();
            // so that it will not show infoWindow when you click yourself [current location]
            return true;
        }

        return false;
    }

    public void maptypeOnclick(View view) {

        if(mMap != null){
            if(mapTypePref == 1){
                view.setBackground(getDrawable(R.drawable.ic_road_map));
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                prefs.edit().putInt("mapTypePref", 0).apply();
                mapTypePref = 0;
            } else {
                view.setBackground(getDrawable(R.drawable.ic_satellite_map));
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                prefs.edit().putInt("mapTypePref", 1).apply();
                mapTypePref = 1;
            }
        } else {
            Toast.makeText(this, "Location info not available", Toast.LENGTH_SHORT).show();
        }

    }

    private void trackingMode(){
        if(trackingModeOn){
            trackingBtn.setBackground(getDrawable(R.drawable.ic_tracking_mode));
            Toast.makeText(this, "Tracking Mode Off", Toast.LENGTH_SHORT).show();
            trackingModeOn = false;
            prefs.edit().putBoolean("trackingModePref",false).apply();
        } else {
            trackingBtn.setBackground(getDrawable(R.drawable.ic_tracking_mode_red));
            Toast.makeText(this, "Tracking Mode ON", Toast.LENGTH_SHORT).show();
            trackingModeOn = true;
            prefs.edit().putBoolean("trackingModePref",true).apply();
        }

    }

    public void trackingOnclick(View view) {
            trackingMode();
    }

    public void searchBtnOnclick(View view) {
        geoLocate();
    }
}
