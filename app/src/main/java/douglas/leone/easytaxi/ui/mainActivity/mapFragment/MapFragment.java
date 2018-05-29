package douglas.leone.easytaxi.ui.mainActivity.mapFragment;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.navigation.Navigation;
import douglas.leone.easytaxi.R;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnSuccessListener<LocationSettingsResponse>, OnFailureListener {
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int REQUEST_LOCATION_SETTING = 1;

    private static final String TAG = "LE1_MapFragment";
    private MapViewModel viewModel;
    private GoogleMap googleMap;


    private Location lastLocation;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        //Set up the map fragment
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        Button button = view.findViewById(R.id.login_button);
        button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mapFragment_to_loginActivity));


        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastLocation==null){
                    requestLocationUpdates();
                }else {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        lastLocation.getLatitude(),
                        lastLocation.getLongitude()), 15));

                }
            }
        });
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady: ");
        googleMap = map;
       googleMap.getUiSettings().setMyLocationButtonEnabled(false);
       requestLocationUpdates();

    }

    private void requestLocationUpdates(){
        if (viewModel.checkLocationPermission()) {
            viewModel.checkLocationSettings(this, this);
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        Log.d(TAG, "requestPermission: ");
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            Log.d(TAG, "onRequestPermissionsResult: ");
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
               requestLocationUpdates();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: permission not granted");
                Toast.makeText(getContext(), "You won't be able to use basic features", Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

        Log.d(TAG, "onSuccess: " + locationSettingsResponse);
        if (!locationSettingsResponse.getLocationSettingsStates().isLocationUsable()) {
            Log.d(TAG, "onSuccess: but not really");
            Toast.makeText(getContext() , "Activate Location Please", Toast.LENGTH_SHORT).show();
        } else if (viewModel.checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
            viewModel.startLocationUpdates().observe(this, new Observer<Location>() {
                @Override
                public void onChanged(@Nullable Location location) {
                    Log.d(TAG, "onChanged: " + location);
                    lastLocation = location;
                }
            });

        }


    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleMap!=null){
            this.onMapReady(googleMap);
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.d(TAG, "onFailure: " + e.toString());
        //TODO find out why it's not called even after a failure
    }
}
