package com.tigran.projects.projectx.fragment.tasks;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.adapter.PlaceAutocompleteAdapter;
import com.tigran.projects.projectx.mapAnim.MapAnimator;
import com.tigran.projects.projectx.mapHelper.TaskLoadedCallback;
import com.tigran.projects.projectx.model.TaskViewModel;
import com.tigran.projects.projectx.model.TodaysTaskInfo;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

import static com.tigran.projects.projectx.fragment.map.MapFragment.DEFAULT_ZOOM;
import static com.tigran.projects.projectx.fragment.map.MapFragment.MY_LOCATION;
import static com.tigran.projects.projectx.fragment.map.MapFragment.START_POINT_MAP;
import static com.tigran.projects.projectx.fragment.map.MapFragment.START_ZOOM;
import static com.tigran.projects.projectx.fragment.tasks.DoneDialogFragment.LOOSE_WEIGHT_DONE;
import static com.tigran.projects.projectx.fragment.tasks.TaskInfoFragment.LOOSE_WEIGHT;


public class LooseWeightMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        TaskLoadedCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {

    private static final String TAG = "LooseWeightMapFragment";

    //views
    private MapView mapView;
    private Button mExitButton;
    private Button mDoneButton;
    private TextView mDistanceView;
    private TextView mCaloriesView;
    private BottomNavigationView mBottomNavigationView;

    //Map, Location
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mDeviceLocation;
    private Polyline currentPolyline;
    LatLng destinationPosition;
    List<LatLng> path;


    //firebase
    DatabaseReference mFirebaseDatabaseUser;

    //preferences
    SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();

    ConstraintSet constraintSet = new ConstraintSet();

    //tasks
    private TaskViewModel mTaskViewModel;

    //user
    private User mCurrentUser;
    private UserViewModel mUserViewModel;

    //navigation
    private Fragment mNavHostFragment;


    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
//                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                //move map camera
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

    public LooseWeightMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loose_weight_map, container, false);

        initViews(view);
        initTools();
        addListeners();
        hideBotNavBar();

        return view;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;

        mGoogleMap.setOnCameraIdleListener(this);
        mGoogleMap.setOnCameraMoveListener(this);

        moveCamera(START_POINT_MAP, START_ZOOM, null);

        setSettings();


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }


        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getDeviceLocation();
//        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
//        mTaskViewModel.setDoneTask(sharedPreferences.getTask(getContext()));
//        String taskStatus = mTaskViewModel.getTask().getValue();
//        if (taskStatus != null) {
//            switch (taskStatus) {
//                case LOOSE_WEIGHT:
//                    if (mDeviceLocation != null) {
//                        mGoogleMap.clear();
//                        looseWeight();
//                        startUserLocationsRunnable();
//                        MapAnimator.getInstance().setPrimaryLineColor(getResources().getColor(R.color.colorPrimary));
//                        MapAnimator.getInstance().setSecondaryLineColor(getResources().getColor(R.color.colorPrimaryLight));
//                        MapAnimator.getInstance().animateRoute(mGoogleMap, path);
//                        mGoogleMap.addMarker(new MarkerOptions().position(destinationPosition).title("Destination"));
//                    }
//                    break;
//                case LOOSE_WEIGHT_DONE:
//                    mGoogleApiClient.stopAutoManage(getActivity());
//                    mGoogleApiClient.disconnect();
//                    NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_global_map_fragment);
//                    mTaskViewModel.setTask(null);
//                    stopLocationUpdates();
//
//                    mTaskViewModel.setTask(null);
//                    break;
//            }
//        }
//        mTaskViewModel.getTask().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                if (s != null) {
//                    switch (s) {
//                        case LOOSE_WEIGHT:
//                            if (mDeviceLocation != null) {
//                                mGoogleMap.clear();
//                                looseWeight();
//                                startUserLocationsRunnable();
//                                MapAnimator.getInstance().setPrimaryLineColor(getResources().getColor(R.color.colorPrimary));
//                                MapAnimator.getInstance().setSecondaryLineColor(getResources().getColor(R.color.colorPrimaryLight));
//                                MapAnimator.getInstance().animateRoute(mGoogleMap, path);
//                                mGoogleMap.addMarker(new MarkerOptions().position(destinationPosition).title("Destination"));
//                                break;
//                            }
//                        case LOOSE_WEIGHT_DONE:
//                            mGoogleApiClient.stopAutoManage(getActivity());
//                            mGoogleApiClient.disconnect();
//                            NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_global_map_fragment);
//                            mTaskViewModel.setTask(null);
//                            stopLocationUpdates();
//
//                            mTaskViewModel.setTask(null);
//                            break;
//                    }
//                }
//            }
//        });

    }


    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        currentPolyline = mGoogleMap.addPolyline((PolylineOptions) values[0]);
    }

    private void initTask() {
        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
        mTaskViewModel.setDoneTask(sharedPreferences.getDoneTask(getContext()));
        String taskStatus = mTaskViewModel.getTask().getValue();
        if (taskStatus != null) {
            switch (taskStatus) {
                case LOOSE_WEIGHT:
                    if (mDeviceLocation != null) {
                        mGoogleMap.clear();
                        looseWeight();
                        startUserLocationsRunnable();
                        MapAnimator.getInstance().setPrimaryLineColor(getResources().getColor(R.color.colorPrimary));
                        MapAnimator.getInstance().setSecondaryLineColor(getResources().getColor(R.color.colorPrimaryLight));
                        MapAnimator.getInstance().animateRoute(mGoogleMap, path);
                        mGoogleMap.addMarker(new MarkerOptions().position(destinationPosition).title("Destination"));
                    }
                    break;
                case LOOSE_WEIGHT_DONE:
                    mGoogleApiClient.stopAutoManage(getActivity());
                    mGoogleApiClient.disconnect();
                    NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_global_map_fragment);
                    mTaskViewModel.setTask(null);
                    stopLocationUpdates();

                    mTaskViewModel.setTask(null);
                    break;
            }
        }
    }

    private void initViews(View v) {
        mExitButton = v.findViewById(R.id.btn_exit_loose_weight_map);
        mDoneButton = v.findViewById(R.id.btn_done_loose_weight_map);
        mDistanceView = v.findViewById(R.id.tv_distance_loose_weight_map);
        mCaloriesView = v.findViewById(R.id.tv_calories_loose_weight_map);
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view_main);

        mapView = v.findViewById(R.id.mv_loose_weight_map);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

    }

    private void initTools() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this.getActivity(), new Random().nextInt(3000), this)
                .build();

        mTaskViewModel = ViewModelProviders.of(getActivity()).get(TaskViewModel.class);
        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                mCurrentUser = user;
            }
        });
        mCurrentUser = mUserViewModel.getUser().getValue();
    }

    private void addListeners() {
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (/*mTaskViewModel.getDoneTask().getValue() == null || mTaskViewModel.getDoneTask().getValue() == 0*/
                        sharedPreferences.getDoneTask(getContext()) == null || sharedPreferences.getDoneTask(getContext()) == 0) {
                    mTaskViewModel.setDoneTask(1);
                    sharedPreferences.setDoneTask(getContext(), 1);
                    setTodaysTaskInfoForCurrentUserAndUpdateInFirebase(1, System.currentTimeMillis() / 1000);
                } else {
                    mTaskViewModel.setDoneTask(3);
                    sharedPreferences.setDoneTask(getContext(), 3);
                    setTodaysTaskInfoForCurrentUserAndUpdateInFirebase(3, System.currentTimeMillis() / 1000);
                }
                mTaskViewModel.setLooseWeightTimestamp(System.currentTimeMillis() / 1000);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DoneDialogFragment tasksFragment = new DoneDialogFragment("Loose Weight");
                tasksFragment.show(fm, null);
            }
        });
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDataLoseInfo();
            }
        });
    }

    private void setSettings() {
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private LatLng getDestinationPoint(LatLng source, double brng, double dist) {
        dist = dist / 6371;
        brng = Math.toRadians(brng);

        double lat1 = Math.toRadians(source.latitude), lon1 = Math.toRadians(source.longitude);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) +
                Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(dist) *
                        Math.cos(lat1),
                Math.cos(dist) - Math.sin(lat1) *
                        Math.sin(lat2));
        if (Double.isNaN(lat2) || Double.isNaN(lon2)) {
            return null;
        }
        return new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    private void looseWeight() {
        double angle = ThreadLocalRandom.current().nextDouble(0, 360);
        LatLng sourcePosition = new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude());
        String origin = mDeviceLocation.getLatitude() + "," + mDeviceLocation.getLongitude();

        destinationPosition = getDestinationPoint(sourcePosition, angle, 1);
        String destination = destinationPosition.latitude + "," + destinationPosition.longitude;

        mGoogleMap.addMarker(new MarkerOptions().position(sourcePosition).title("Origin")
//                .icon(convertToBitmap(getResources().getDrawable(R.drawable.map_marker_radius), 130, 130)));
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

//        mGoogleMap.addMarker(new MarkerOptions().position(destinationPosition).title("Destination"));

        //Define list to get all latlng for the route
        path = new ArrayList();

        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, origin, destination);
        try {
            DirectionsResult res = req.mode(TravelMode.WALKING).await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    double mPastDistance;
    double mStep;
    LatLng mPreviousStep;

    private void startUserLocationsRunnable() {
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                getDeviceLocation();
                double d = calculationByDistance(new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude()), destinationPosition);
                if (mPreviousStep != null) {
                    if (mDeviceLocation.getLatitude() != mPreviousStep.latitude || mDeviceLocation.getLongitude() != mPreviousStep.longitude) {
                        mStep = calculationByDistance(new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude()), mPreviousStep);
                        mPastDistance = mPastDistance + mStep * 1000;
                        int burnedCalories = (int) (mPastDistance * 0.055);
                        mCaloriesView.setText(" • " + String.valueOf(burnedCalories) + " calories burned");
                    }
                }
                mPreviousStep = new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude());
                DecimalFormat df2 = new DecimalFormat("#.##");
                mDistanceView.setText(" • " + df2.format(d) + " km to destination");
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    private FusedLocationProviderClient fusedLocationClient;

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (mDeviceLocation == null) {
                            mDeviceLocation = location;
                            initTask();
                        }
                        if (location != null) {
                            mDeviceLocation = location;
                            moveCamera(new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude()), DEFAULT_ZOOM, MY_LOCATION);
                        }
                    }
                });

    }


    private void moveCamera(LatLng latLng, float zoom, String title) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user'mPastDistance response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void updateUserInFirebase() {
        mFirebaseDatabaseUser = FirebaseDatabase.getInstance().getReference("users");
        mFirebaseDatabaseUser.child(mCurrentUser.getId()).setValue(mCurrentUser);
    }

    private void setTodaysTaskInfoForCurrentUserAndUpdateInFirebase(int doneTask, long timestamp) {
        TodaysTaskInfo todaysTaskInfo;
        if (mCurrentUser.getTodaysTaskInfo() == null) {
            todaysTaskInfo = new TodaysTaskInfo();
        } else {
            todaysTaskInfo = mCurrentUser.getTodaysTaskInfo();
        }
        todaysTaskInfo.setDoneTasksStatus(doneTask);
        todaysTaskInfo.setTimestampLooseWeight(timestamp);
        mCurrentUser.setTodaysTaskInfo(todaysTaskInfo);

        updateUserInFirebase();
    }


    private void showDataLoseInfo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Are you sure you want to exit?")
                .setMessage("If you exit now all data will be lost")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        mGoogleApiClient.stopAutoManage(getActivity());
                        mGoogleApiClient.disconnect();
                        NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_global_map_fragment);
                        mTaskViewModel.setTask(null);
                        stopLocationUpdates();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void hideBotNavBar() {
        mBottomNavigationView.setVisibility(View.GONE);
    }
}
