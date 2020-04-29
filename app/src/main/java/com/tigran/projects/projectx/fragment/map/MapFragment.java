
package com.tigran.projects.projectx.fragment.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.fragment.event.CreateEventFragment;
import com.tigran.projects.projectx.fragment.tasks.TasksFragment;
import com.tigran.projects.projectx.mapHelper.TaskLoadedCallback;
import com.tigran.projects.projectx.model.Event;
import com.tigran.projects.projectx.model.EventViewModel;
import com.tigran.projects.projectx.model.MyLatLng;
import com.tigran.projects.projectx.model.User;
import com.tigran.projects.projectx.model.UserViewModel;
import com.tigran.projects.projectx.preferences.SaveSharedPreferences;
import com.tigran.projects.projectx.util.GpsUtils;
import com.tigran.projects.projectx.util.PermissionChecker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.tigran.projects.projectx.util.NavigationHelper.onClickNavigate;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        TaskLoadedCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {


//    public static final String DATABASE_PATH_EVENTS = "Events";

    private static final String TAG = "MapFragment";
    public static final int PLACE_PICKER_REQUEST = 2;
    public static final int REQUEST_LOCATION_PERMISSION_CODE = 1234;
    public static final String MY_LOCATION = "My Location";
    public static final float DEFAULT_ZOOM = 15f;
    public static final float START_ZOOM = 10f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    public static final LatLng START_POINT_MAP = new LatLng(40.183414, 44.514807);


    String[] mPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //views
    private ConstraintLayout mMainLayout;
    private BottomNavigationView mBottomNavigationView;
    private FloatingActionButton mAddEventButton;
    private FloatingActionButton mLocationButton;
    private FloatingActionButton mTasksButton;
    private ConstraintLayout mSearchLayout;
    private ConstraintLayout mTaskInfoLayout;
    private MapView mapView;
    private ImageView mCurrentLocationView;
    private Menu mBottomNavMenu;

    ConstraintSet constraintSet = new ConstraintSet();


    //Map, Location
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private Location mDeviceLocation;
    private Polyline currentPolyline;
    private Place mPlace;
    private Marker mEventMarker;
    private List<Marker> mMarkerList = new ArrayList<>();


    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;


    //navigation
    private Fragment mNavHostFragment;

    //preferences
    SaveSharedPreferences sharedPreferences = new SaveSharedPreferences();

    //user
    private List<User> mUserList = new ArrayList<>();
    private User mCurrentUser;
    private UserViewModel mUserViewModel;

    //event
    private List<Event> mEventList = new ArrayList<>();
    private Event mCurrentEvent;
    private EventViewModel mEventViewModel;


    //Firebase
    DatabaseReference mFirebaseDatabse;
    DatabaseReference mFirebaseDatabseUser;

    //date format
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm");

    //place autocomplete
    private AutocompleteSupportFragment placeAutocompleteSupportFragment;


    //constructor
    public MapFragment() {
    }


    //************************************ LIFECYCLE METHODS ****************************************
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
//        if(mTaskViewModel!=null)
//         mUser = sharedPreferences.getCurrentUser(getContext());
        initViews(view);
        showBotNavBar();
        initPlaceAutoComplete();
        constraintSet.clone(getActivity(), R.layout.fragment_map);
        //getDeviceLocation();


        Toast.makeText(getContext(), "Choose Location for your Event", Toast.LENGTH_LONG).show();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDeviceLocation();
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        mUserViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        mUserViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                mCurrentUser = user;
            }
        });
        mCurrentUser = mUserViewModel.getUser().getValue();

        mEventViewModel = ViewModelProviders.of(getActivity()).get(EventViewModel.class);
        mEventViewModel.getEvent().observe(getViewLifecycleOwner(), new Observer<Event>() {
            @Override
            public void onChanged(@Nullable final Event event) {
                mCurrentEvent = event;
            }
        });
        mCurrentEvent = mEventViewModel.getEvent().getValue();


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


    //************************************** METHODS ********************************************
    private void showBotNavBar() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
    }

    private void initViews(View v) {
        mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view_main);
        mAddEventButton = v.findViewById(R.id.fab_add_event_map);
        mMainLayout = v.findViewById(R.id.layout_main_map);
        mLocationButton = v.findViewById(R.id.fab_my_location_map);
        mTasksButton = v.findViewById(R.id.fab_tasks_map);
        mSearchLayout = v.findViewById(R.id.layout_search_map);
        mTaskInfoLayout = v.findViewById(R.id.layout_task_info_map);
        mCurrentLocationView = v.findViewById(R.id.iv_current_location_map);
        mNavHostFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        mBottomNavMenu = mBottomNavigationView.getMenu();


        mapView = v.findViewById(R.id.mv_map);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        onClickNavigate(mTasksButton, R.id.action_map_fragment_to_tasks_fragment);
        mTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(getContext());
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TasksFragment tasksFragment = new TasksFragment();
                tasksFragment.show(fm, null);
                if (sharedPreferences.getDoneTask(getContext()) != null) {
                    if (sharedPreferences.getDoneTask(getContext()) == 3) {
                        Toast.makeText(getContext(), "You have done all today's tasks. Come back tomorrow!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this.getActivity(), new Random().nextInt(3000), this)
                .build();


        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionChecker.hasLocationPermission(getContext()) && isLocationEnabled(getContext())) {
                    getDeviceLocation();
                } else {
                    requestPermissions(getContext());
                }
            }
        });


        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MarkerOptions markerOptions = new MarkerOptions().position(mGoogleMap.getCameraPosition().target);
                mEventMarker = mGoogleMap.addMarker(markerOptions);
                mMarkerList.add(mEventMarker);
                Event event = new Event();
                MyLatLng myLatLng = new MyLatLng(mEventMarker.getPosition().latitude, mEventMarker.getPosition().longitude);
                event.setPosition(myLatLng);
                event.setPlace(getAddress(myLatLng.getLatitude(), myLatLng.getLongitude()));
                mEventViewModel.setEvent(event);
                mEventViewModel.setToEdit(false);

                NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_map_fragment_to_create_event_fragment);
            }
        });

    }

    private void setEventsOnMap() {

        mFirebaseDatabse = FirebaseDatabase.getInstance().getReference("events");
        mFirebaseDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mEventList.clear();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    //remove old events
                    if (event.getDate().before(Calendar.getInstance().getTime())) {
                        mFirebaseDatabse.child(event.getUid()).removeValue();
                    } else {
                        mEventList.add(event);

                        LatLng latLng = new LatLng(event.getPosition().getLatitude(), event.getPosition().getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                        mGoogleMap.addMarker(markerOptions);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public static void requestPermissions(Context context) {
        new GpsUtils(context).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
//                isGPS = isGPSEnable;
            }
        });


        Dexter.withActivity((Activity) context)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {


            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.isAnyPermissionPermanentlyDenied()) {
                    requestPermissions(context);
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {

            }

        }).check();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;

    }

    protected void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissions, REQUEST_LOCATION_PERMISSION_CODE);
        }
//        ActivityCompat.requestPermissions(this.getActivity(),
//                mPermissions,
//                REQUEST_LOCATION_PERMISSION_CODE);
    }


    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            Task mTaskLocation = mFusedLocationProviderClient.getLastLocation();
            mTaskLocation.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {
//                        Toast.makeText(getContext(), "Location Found", Toast.LENGTH_SHORT).show();
                        mDeviceLocation = (Location) task.getResult();
                        moveCamera(new LatLng(mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude()), DEFAULT_ZOOM, MY_LOCATION);

                    } else {
                        Toast.makeText(getContext(), "Location Not Found", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        } catch (SecurityException e) {
            Log.d("Map", e.getMessage());
        }

    }


    private void moveCamera(LatLng latLng, float zoom, String title) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    public String getAddress(double lat, double lng) {
        String addressString = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses.get(0).getThoroughfare() != null) {
                addressString += addresses.get(0).getThoroughfare();
//                street = addresses.get(0).getThoroughfare();
            }
            if (addresses.get(0).getSubThoroughfare() != null) {
                addressString += " " + addresses.get(0).getSubThoroughfare();
//                houseNumber = addresses.get(0).getSubThoroughfare();
            }
            if (addresses.get(0).getLocality() != null) {
                if (addresses.get(0).getSubThoroughfare() != null) {
                    addressString += ", ";
                }
                addressString += addresses.get(0).getLocality();
//                city = addresses.get(0).getLocality();
            }

            Log.e(TAG, "getAddress: " + addressString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressString;
    }


    private boolean goingToEvent;

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

        setEventsOnMap();

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (final Event event : mEventList) {
                    if (marker.getPosition().latitude == event.getPosition().getLatitude()
                            && marker.getPosition().longitude == event.getPosition().getLongitude()) {
                        mEventViewModel.setEvent(event);

                        String goingToEventString = "Going";
                        for (String id : event.getParticipants()) {
                            if (id.equals(mCurrentUser.getId())) {
                                goingToEvent = true;
                                goingToEventString = "Not Going";
                            }
                        }
                        CreateEventFragment cef = new CreateEventFragment();

                        MaterialDialog.Builder eventDialogBuilder = new MaterialDialog.Builder(getContext());
                        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event_information, null);
                        eventDialogBuilder.customView(dialogView, false);
                        eventDialogBuilder.positiveText(goingToEventString);
                        eventDialogBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                goingToEvent = !goingToEvent;
                                if (goingToEvent) {
                                    eventDialogBuilder.positiveText("Not Going");
                                    event.getParticipants().add(mCurrentUser.getId());
                                    mCurrentUser.getmGoingEvents().add(event.getUid());
                                    updateUserInFirebase();

                                    cef.updateEventInFirebase(event);
                                } else {
                                    eventDialogBuilder.positiveText("Going");
                                    event.getParticipants().remove(mCurrentUser.getId());
                                    mCurrentUser.getmGoingEvents().remove(event.getUid());

                                    updateUserInFirebase();
                                    cef.updateEventInFirebase(event);
                                }
                            }
                        });

                        eventDialogBuilder.negativeText("Cancel");
                        eventDialogBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        });

                        Log.e(TAG, "onMarkerClick: " + event.getDate().toGMTString());

                        //setting Dialog Views ----------------------------------------------
                        EditText titleView = dialogView.findViewById(R.id.etv_title_dialog);
                        EditText descriptionView = dialogView.findViewById(R.id.etv_description_dialog);
                        EditText dateLocationView = dialogView.findViewById(R.id.etv_date_location_dialog);
                        EditText creatorView = dialogView.findViewById(R.id.etv_creator_dialog);
                        TextView participantsView = dialogView.findViewById(R.id.tv_participants_dialog);
                        Button editButton = dialogView.findViewById(R.id.btn_to_change_event_dialog);


                        titleView.setText(event.getTitle());
                        descriptionView.setText(event.getDescription());
                        dateLocationView.setText(simpleDateFormat.format(event.getDate()));
                        creatorView.setText(event.getCreator().getUsername());

                        MaterialDialog eventDialog = eventDialogBuilder.build();
                        eventDialog.show();

                        //checking if current user is the creator of event ------------------------------------------
                        if (event.getCreator().getId().equals(mCurrentUser.getId())) {
                            editButton.setVisibility(View.VISIBLE);
                            editButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mEventViewModel.setEvent(event);
                                    mEventViewModel.setToEdit(true);
                                    eventDialog.dismiss();
                                    NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_map_fragment_to_create_event_fragment);
                                }
                            });
                        }

                        //checking if current user is going to current event -------------------------------------
                        mFirebaseDatabseUser = FirebaseDatabase.getInstance().getReference("users");
                        mFirebaseDatabseUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mUserList.clear();
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    User user = userSnapshot.getValue(User.class);
                                    mUserList.add(user);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        //setting participants clickListener
                        participantsView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                eventDialog.dismiss();
                                NavHostFragment.findNavController(mNavHostFragment).navigate(R.id.action_map_fragment_to_participants_fragment);
                            }
                        });

                    }
                }
                return true;
            }
        });

    }

    private void updateUserInFirebase() {
        mFirebaseDatabseUser = FirebaseDatabase.getInstance().getReference("users");
        mFirebaseDatabseUser.child(mCurrentUser.getId()).setValue(mCurrentUser);

    }

    private void setSettings() {
        mGoogleMap.getUiSettings().setZoomControlsEnabled(sharedPreferences.getZoom(getContext()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    public static void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint, final Bitmap bitmap) {
        Marker marker = myMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(directionPoint.get(0))
                .flat(true));

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), DEFAULT_ZOOM));
        animateMarker(myMap, marker, directionPoint, false);

    }

    private static void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                i++;

                if (t < 1.0) {
                    handler.postDelayed(this, 15);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


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


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onCameraMove() {
        float biasedValue = 0.435f;
        constraintSet.setVerticalBias(mCurrentLocationView.getId(), biasedValue);
        constraintSet.applyTo(mMainLayout);

    }

    @Override
    public void onCameraIdle() {

        float biasedValue = 0.45f;
        constraintSet.setVerticalBias(mCurrentLocationView.getId(), biasedValue);
        constraintSet.applyTo(mMainLayout);


    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        currentPolyline = mGoogleMap.addPolyline((PolylineOptions) values[0]);
    }

    private void initPlaceAutoComplete() {
        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            com.google.android.libraries.places.api.Places.initialize(getContext(), "AIzaSyB1ukXEZhJdXq_eVeb2u5nBPD2FzjOE9YQ");
        }

        placeAutocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutocompleteSupportFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));

        placeAutocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                if (place.getLatLng() != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(place.getLatLng());
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
                }
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = (fm.findFragmentById(R.id.place_autocomplete));
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }
}
