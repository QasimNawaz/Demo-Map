package com.example.alisons.demo_application;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alisons.demo_application.dialoge.WaitDialog;
import com.example.alisons.demo_application.routes.DecodePoly;
import com.example.alisons.demo_application.routes.Example;
import com.example.alisons.demo_application.routes.RetrofitMaps;
import com.example.alisons.demo_application.utils.AppLogs;
import com.example.alisons.demo_application.utils.SharedPref;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 120000; //60 seconds

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final String TAG = "HomeScreen";
    public static final int SELECT_START_POINT = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 111;
    List<String> listPermissionsNeeded;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int UPDATE_ADDRESS = 0;
    private static final int UPDATE_ADDRESS_EXCEPTION = 1;
    private static final int UPDATE_ADDRESS_SEARCHING = 2;
    private MapView mapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean cameraFirstMove = true;
    private LocationManager manager = null;
    private LatLng currentLatlng = null;
    private LinearLayout chooseLoc = null;
    private TextView addTxt;
    private LatLng destLatLng = null;
    private String pickUpName = null;
    private String pickUpCity = null;
    private Handler mHandler = null;
    private Marker mCurrentCentreMarker = null;
    private ImageView startNav = null;
    private Polyline line = null;
    private String estimatedDistance = null;
    private String estimatedDuration = null;
    private ImageButton curLocBtn = null;
    DrawerLayout drawer = null;
    private LinearLayout currentLoc, getDirection, drawerMenu;
    private Switch satelliteCheck, terrainCheck, trafficCheck;
    private ArrayList<Marker> markerArrayList;

    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(50);
    private static final PatternItem GAP = new Gap(20);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DASH, DOT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_drawer);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        drawer = findViewById(R.id.drawer_layout);
        markerArrayList = new ArrayList<>();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        initDrawerViews();
        initViews();
        mapView = findViewById(R.id.home_mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

//        if (Build.VERSION.SDK_INT > 23) {
//            if (checkAndRequestPermissions()) {
        initializeMap();
//            }
//        }

        chooseLoc.setOnClickListener(this);
        startNav.setOnClickListener(this);
        curLocBtn.setOnClickListener(this);
        drawerMenu.setOnClickListener(this);
        currentLoc.setOnClickListener(this);
        getDirection.setOnClickListener(this);
        satelliteCheck.setOnCheckedChangeListener(this);
        terrainCheck.setOnCheckedChangeListener(this);
        trafficCheck.setOnCheckedChangeListener(this);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_ADDRESS:
                        Address address = (Address) msg.obj;
                        String addressText = String.format("%s",
                                address.getAddressLine(0));
                        addTxt.setText(addressText);
//                        cityTxt.setText(address.getLocality());
                        if (destLatLng != null) {
                            createMarker(destLatLng, BitmapDescriptorFactory.HUE_GREEN, addressText);
                        }
                        pickUpName = addressText;
                        pickUpCity = address.getLocality();
                        break;
                    case UPDATE_ADDRESS_SEARCHING:
                        addTxt.setText("Searching Address");
//                        cityTxt.setText("");
                        destLatLng = null;
                        pickUpName = null;
                        pickUpCity = null;
                        break;
                    case UPDATE_ADDRESS_EXCEPTION:
                        addTxt.setText(msg.obj.toString());
                        destLatLng = null;
                        pickUpName = null;
                        pickUpCity = null;
                        break;
                }
                return false;
            }
        });
        if (SharedPref.getTraffic(MainActivity.this)) {
            trafficCheck.setChecked(true);
        } else {
            trafficCheck.setChecked(false);
        }
        if (SharedPref.getMapTypeSatellite(MainActivity.this)) {
            satelliteCheck.setChecked(true);
            terrainCheck.setChecked(false);
        } else {
            satelliteCheck.setChecked(false);
            terrainCheck.setChecked(true);
        }
    }

    private void initDrawerViews() {
        currentLoc = findViewById(R.id.drawer_my_location_view);
        getDirection = findViewById(R.id.drawer_get_direction_view);
        drawerMenu = findViewById(R.id.home_drawer_icon);
        satelliteCheck = findViewById(R.id.drawer_satellite_check);
        terrainCheck = findViewById(R.id.drawer_terrain_check);
        trafficCheck = findViewById(R.id.drawer_traffic_check);
    }

    private void createMarker(LatLng pickUpLatLng, float color, String addressText) {
        if (mCurrentCentreMarker != null) {
            mCurrentCentreMarker.remove();
        }
        mCurrentCentreMarker = googleMap.addMarker(new MarkerOptions()
                .position(pickUpLatLng)
                .title(addressText)
                .icon(BitmapDescriptorFactory.defaultMarker(color)));
    }

    private void initViews() {
        chooseLoc = findViewById(R.id.home_search_loc_intent);
        addTxt = findViewById(R.id.home_currentLoc_name);
//        cityTxt = findViewById(R.id.home_currentLoc_city);
        startNav = findViewById(R.id.start_navigation);
        curLocBtn = findViewById(R.id.home_curLoc_btn);
    }

//    private boolean checkAndRequestPermissions() {
//
//        int internetPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.INTERNET);
//
//        int fineLocationPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//
//        int coarseLocationPermission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        listPermissionsNeeded = new ArrayList<>();
//
//        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.INTERNET);
//        }
//
//        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
//        if (!listPermissionsNeeded.isEmpty()) {
//            ActivityCompat.requestPermissions(this,
//                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_ACCOUNTS);
//            return false;
//        }
//
//        return true;
//    }

    private void initializeMap() {
        if (googleMap == null) {
            mapView = findViewById(R.id.home_mapView);
            mapView.getMapAsync(this);
            //setup markers etc...
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapLongClickListener(clickListener);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
                if (SharedPref.getMapTypeSatellite(MainActivity.this)) {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else {
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                map.setMyLocationEnabled(true);
                if (SharedPref.getTraffic(MainActivity.this)) {
                    map.setTrafficEnabled(true);
                } else {
                    map.setTrafficEnabled(false);
                }
                map.setIndoorEnabled(true);
                map.setBuildingsEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.getUiSettings().setCompassEnabled(false);
                map.getUiSettings().setMapToolbarEnabled(true);
                googleMap.getMyLocation();
                googleMap.setOnPolylineClickListener(MainActivity.this);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
            if (SharedPref.getMapTypeSatellite(MainActivity.this)) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
            map.setMyLocationEnabled(true);
            if (SharedPref.getTraffic(MainActivity.this)) {
                map.setTrafficEnabled(true);
            } else {
                map.setTrafficEnabled(false);
            }
            map.setIndoorEnabled(true);
            map.setBuildingsEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getMyLocation();
            googleMap.setOnPolylineClickListener(MainActivity.this);
        }

        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else if (manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = getLocation();
            if (location != null) {
                currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, 18));
            }
        }
    }


    protected synchronized void buildGoogleApiClient() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
//            case MY_PERMISSIONS_REQUEST_ACCOUNTS:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //Permission Granted Successfully. Write working code here.
//                    initializeMap();
//                } else {
//                    //You did not accept the request can not use the functionality.
//                    Toast.makeText(this, "Permission Denied. Denying permission may cause it no longer function intended.", Toast.LENGTH_SHORT).show();
//                    initializeMap();
//                }
//                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("USerHoMe", "" + location.getLatitude());
        currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
        if (cameraFirstMove) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, 18));
            cameraFirstMove = false;
        }
    }

    private void buildAlertMessageNoGps() {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private Location getLocation() {
        Location currentLocation = null;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        } else {
            Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                currentLocation = location;
            } else if (location1 != null) {
                currentLocation = location1;
            } else if (location2 != null) {
                currentLocation = location2;
            } else {
                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();
            }
        }
        return currentLocation;
    }

    @Override
    public void onClick(View v) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        switch (v.getId()) {
            case R.id.home_search_loc_intent:
                if (currentLatlng != null) {
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .setBoundsBias(new LatLngBounds(new LatLng(currentLatlng.latitude, currentLatlng.longitude), new LatLng(currentLatlng.latitude, currentLatlng.longitude)))
                                .build(this);
                        startActivityForResult(intent, SELECT_START_POINT);
                    } catch (GooglePlayServicesRepairableException e) {
                        AppLogs.logd("GooglePlayServicesRepairableException: " + e.getMessage(), TAG + "# Search");
                        // TODO: Handle the error.
                    } catch (GooglePlayServicesNotAvailableException e) {
                        AppLogs.logd("GooglePlayServicesNotAvailableException: " + e.getMessage(), TAG + "# Search");
                        // TODO: Handle the error.
                    }
                }
                break;
            case R.id.start_navigation:
                if (currentLatlng != null && destLatLng != null) {
                    WaitDialog.showWaitDialog("Loading...", MainActivity.this);
                    drawNavUsingRetrofit(currentLatlng, destLatLng);
                }
                break;
            case R.id.home_curLoc_btn:
//                if (googleMap.getMapType() == 1) {
//                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//                } else {
//                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                }
                if (currentLatlng != null) {
                    CameraPosition cameraPosition = googleMap.getCameraPosition();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, cameraPosition.zoom));
                }
                break;
            case R.id.drawer_my_location_view:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    //drawer is open
                    drawer.closeDrawers();
                }
                if (currentLatlng != null) {
                    CameraPosition cameraPosition = googleMap.getCameraPosition();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, cameraPosition.zoom));
                }
                break;
            case R.id.drawer_get_direction_view:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    //drawer is open
                    drawer.closeDrawers();
                }
                if (currentLatlng != null && destLatLng != null) {
                    WaitDialog.showWaitDialog("Loading...", MainActivity.this);
                    drawNavUsingRetrofit(currentLatlng, destLatLng);
                }
                break;
            case R.id.home_drawer_icon:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_START_POINT) {
//            getActivity();
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                addTxt.setText("Searching Address");
//                cityTxt.setText("");
                destLatLng = null;
                pickUpName = null;
                pickUpCity = null;
                LatLng latLng = place.getLatLng();
                Location location = new Location("");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                (new ReverseGeocodingTask(MainActivity.this)).execute(location);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                destLatLng = null;
                pickUpName = null;
                pickUpCity = null;
                // TODO: Handle the error.
                AppLogs.logd("Search Error: " + status.getStatusMessage(), TAG);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                destLatLng = null;
                pickUpName = null;
                pickUpCity = null;
            }
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.d(TAG, "click: " + polyline.getPattern());
        if ((polyline.getPattern() == null) || (polyline.getPattern().contains(GAP))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else if ((polyline.getPattern() == null) || (polyline.getPattern().contains(DASH))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else if ((polyline.getPattern() == null) || (polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        switch (compoundButton.getId()) {
            case R.id.drawer_satellite_check:
//                Toast.makeText(this, "Satellite", Toast.LENGTH_SHORT).show();
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    //drawer is open
                    drawer.closeDrawers();
                }
                if (googleMap != null) {
                    if (b) {
                        terrainCheck.setChecked(false);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        SharedPref.setMapTypeSatellite(MainActivity.this, true);
                    } else {
                        terrainCheck.setChecked(true);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        SharedPref.setMapTypeSatellite(MainActivity.this, false);
                    }
                }
                break;
            case R.id.drawer_terrain_check:
//                Toast.makeText(this, "Terrain", Toast.LENGTH_SHORT).show();
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    //drawer is open
                    drawer.closeDrawers();
                }
                if (googleMap != null) {
                    if (b) {
                        satelliteCheck.setChecked(false);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        SharedPref.setMapTypeSatellite(MainActivity.this, false);
                    } else {
                        satelliteCheck.setChecked(true);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        SharedPref.setMapTypeSatellite(MainActivity.this, true);
                    }
                }
                break;
            case R.id.drawer_traffic_check:
//                Toast.makeText(this, "Traffic", Toast.LENGTH_SHORT).show();
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    //drawer is open
                    drawer.closeDrawers();
                }
                if (googleMap != null) {
                    if (b) {
                        googleMap.setTrafficEnabled(true);
                    } else {
                        googleMap.setTrafficEnabled(false);
                    }
                }
                break;
        }
    }


    GoogleMap.OnMapLongClickListener clickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(latLng.latitude + " : " + latLng.longitude));
            markerArrayList.add(marker);
            if (markerArrayList.size() > 2) {
                googleMap.clear();
                markerArrayList.clear();
                Marker marker1 = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(latLng.latitude + " : " + latLng.longitude));
                markerArrayList.add(marker1);
            }

            if (markerArrayList.size() == 2) {
                WaitDialog.showWaitDialog("Loading...", MainActivity.this);
                drawNavUsingRetrofit(markerArrayList.get(0).getPosition(), markerArrayList.get(1).getPosition());
            }
        }
    };


    // get location address
    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            Location loc = params[0];
            List<Address> addresses = null;
            try {
                // Call the synchronous getFromLocation() method by passing in the lat/long values.
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                // Update UI field with the exception.
                Message.obtain(mHandler, UPDATE_ADDRESS_EXCEPTION, e.toString()).sendToTarget();
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                Message.obtain(mHandler, UPDATE_ADDRESS, address).sendToTarget();
//                createMarker(new LatLng(latitude, longitude), BitmapDescriptorFactory.HUE_GREEN, addressText);
                destLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());


            } else {
                Message.obtain(mHandler, UPDATE_ADDRESS_SEARCHING, "Searching Address").sendToTarget();
            }

            return null;
        }
    }

    private void drawNavUsingRetrofit(final LatLng origin, final LatLng dest) {
        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getDistanceDuration("metric", origin.latitude + "," + origin.longitude, dest.latitude + "," + dest.longitude, "driving");
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(retrofit.Response<Example> response, Retrofit retrofit) {
                Log.d(TAG, "Navigation Response: " + response.message());
                Log.d(TAG, "Navigation Size: " + response.body().getRoutes().size());
                try {
                    //Remove previous line from map
                    if (line != null) {
                        line.remove();
                    }
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(destLatLng.latitude, destLatLng.longitude), 15));
//                    if (markers.size() > 1) {
//                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                        for (Marker marker : markers) {
//                            builder.include(marker.getPosition());
//                        }
//                        LatLngBounds bounds = builder.build();
//                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 15);
//                        googleMap.moveCamera(cu);
//                    }
                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(origin)
                            .include(dest)
                            .build();
                    // begin new code:
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
//                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin-150, margin-150, margin-150);
                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                    googleMap.moveCamera(update);

                    String distance = null;
                    String time = null;
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
//                        ShowDistanceDuration.setText("Distance:" + distance + ", Duration:" + time);
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
//                        Log.d(TAG, "Navigation Distance: " + distance);
//                        Log.d(TAG, "Navigation Duration: " + time);
//                        Log.d(TAG, "Navigation Encoded: " + encodedString);
                        List<LatLng> list = DecodePoly.decodePolyLine(encodedString);
                        line = googleMap.addPolyline(new PolylineOptions()
                                        .addAll(list)
                                        .width(16)
                                        .color(Color.parseColor("#0C1558"))
                                        .geodesic(true)
//                                .pattern(PATTERN_POLYLINE_DOTTED)
                                        .clickable(true)
                        );
                    }
                    estimatedDistance = distance;
                    estimatedDuration = time;
                    Log.d(TAG, "Navigation Result: " + estimatedDistance);
                    Log.d(TAG, "Navigation Result: " + estimatedDuration);
                    showSnackBar("Distance:" + estimatedDistance + ", Duration:" + estimatedDuration);
                    WaitDialog.closeWaitDialog();
                } catch (Exception e) {
                    Log.d(TAG, "Navigation Exception: " + e);
                    WaitDialog.closeWaitDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                WaitDialog.closeWaitDialog();
                Log.d("onFailure", t.toString());
            }
        });
    }

    private void showSnackBar(String s) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, s, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

}
