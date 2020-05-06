package com.tigran.projects.projectx.fragment.launch;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tigran.projects.projectx.R;
import com.tigran.projects.projectx.fragment.event.CreateEventFragment;
import com.tigran.projects.projectx.model.Event;
import com.tigran.projects.projectx.util.GpsUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.tigran.projects.projectx.fragment.map.MapFragment.START_POINT_MAP;
import static com.tigran.projects.projectx.fragment.map.MapFragment.START_ZOOM;
import static com.tigran.projects.projectx.util.NavigationHelper.onClickNavigate;
import static java.lang.String.valueOf;


public class LaunchFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "LaunchFragment";

    //views
    private Button mSignInButton;
    private Button mSignUpButton;
    private BottomNavigationView mBottomNavigationView;
    private MapView mapView;

    private GoogleMap mGoogleMap;

    //firebase
    DatabaseReference mFirebaseDatabse;

    //event
    List<Event> mEventList = new ArrayList<>();

    //date format
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm");


    //constructor
    public LaunchFragment() {
    }

    //************************************* LIFECYCLE METHODS **************************************
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_launch_modern, container, false);

        initViews(view);
        changeDesignStyle();

        requestPermissions(getContext());

        onClickNavigate(mSignInButton, R.id.action_launch_fragment_to_sign_in_fragment);
        onClickNavigate(mSignUpButton, R.id.action_launch_fragment_to_sign_up_fragment);

        return view;
    }


    //**************************************** METHODS *********************************************
    private void changeDesignStyle() {
        mBottomNavigationView.setVisibility(View.GONE);
    }

    private void initViews(View v) {
        mSignInButton = v.findViewById(R.id.btn_sign_in_launch);
        mSignUpButton = v.findViewById(R.id.btn_sign_up_launch);
        mBottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view_main);
        mapView = v.findViewById(R.id.mv_launch);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        moveCamera(START_POINT_MAP, START_ZOOM, null);

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

                        CreateEventFragment cef = new CreateEventFragment();

                        MaterialDialog.Builder eventDialogBuilder = new MaterialDialog.Builder(getContext());
                        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event_information,null);
                        eventDialogBuilder.customView(dialogView,false);
                        MaterialDialog eventDialog = eventDialogBuilder.build();

                        eventDialogBuilder.negativeText("Cancel");
                        eventDialogBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        });

                        //setting Dialog Views ----------------------------------------------
                        EditText titleView = dialogView.findViewById(R.id.etv_title_dialog);
                        EditText descriptionView = dialogView.findViewById(R.id.etv_description_dialog);
                        EditText dateLocationView = dialogView.findViewById(R.id.etv_date_location_dialog);
                        EditText creatorView = dialogView.findViewById(R.id.etv_creator_dialog);


                        titleView.setText(event.getTitle());
                        descriptionView.setText(event.getDescription());
                        dateLocationView.setText(simpleDateFormat.format(event.getDate()));
                        creatorView.setText(event.getCreator().getUsername());

                        eventDialog.show();

                    }
                }
                return true;
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
                    mEventList.add(event);
                    LatLng latLng = new LatLng(event.getPosition().getLatitude(), event.getPosition().getLongitude());

                    MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                    mGoogleMap.addMarker(markerOptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {


            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {

            }

        }).check();
    }
}