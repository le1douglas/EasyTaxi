package douglas.leone.easytaxi.ui.mainActivity.mapFragment;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.navigation.Navigation;
import douglas.leone.easytaxi.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String TAG = "LE1_MapFragment";
    private MapViewModel viewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;

    private static boolean checkLocationPermission(Context context) {
        boolean result;
        result = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "checkLocationPermission: " + result);

        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.getContext());


        //Set up the map fragment
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        Button button = view.findViewById(R.id.login_button);

        button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_mapFragment_to_loginActivity));

    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady: ");
        googleMap = map;
        getLastPosition();
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
                getLastPosition();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: permission not granted");
                Toast.makeText(getContext(), "You won't be able to use basic features", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void getLastPosition() {
        Log.d(TAG, "getLastPosition: ");
        if (checkLocationPermission(getContext())) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.d(TAG, "onSuccess: " + location);
                                // Logic to handle location object
                                if (checkLocationPermission(getContext()))
                                    googleMap.setMyLocationEnabled(true);
                            } else {
                                Log.d(TAG, "onSuccess: but location is null");
                            }
                        }
                    });
        } else {
            requestPermission();
        }

    }
}
