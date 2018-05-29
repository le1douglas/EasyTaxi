package douglas.leone.easytaxi.ui.mainActivity.mapFragment;


import android.Manifest;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapViewModel extends AndroidViewModel {
    private static final String TAG = "LE1_MapViewModel";


    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2 * 1000; /* 2 sec */

    public MapViewModel(@NonNull Application application) {
        super(application);
        // Construct a FusedLocationProviderClient.

    }

    boolean checkLocationPermission() {
        boolean result;
        result = ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "checkLocationPermission: " + result);

        return result;
    }


    void checkLocationSettings(OnFailureListener onFailureListener, OnSuccessListener<LocationSettingsResponse> onSuccessListener) {
        Log.d(TAG, "checkLocationSettings: ");
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        final LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(getApplication());
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

    }

    // Trigger new location updates at interval
    LiveData<Location> startLocationUpdates() {
        final MutableLiveData<Location> locationLiveData = new MutableLiveData<>();

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
                locationLiveData.setValue(locationResult.getLastLocation());
            }
        };

        if (checkLocationPermission()) {
            getFusedLocationProviderClient(getApplication()).requestLocationUpdates(mLocationRequest, locationCallback, null);
        }

        return locationLiveData;
    }

    public LiveData<Location> getLastLocation() {

        final MutableLiveData<Location> locationLiveData = new MutableLiveData<>();

        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getApplication());

        if (checkLocationPermission()) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d(TAG, "onSuccess: but position is null");
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                locationLiveData.setValue(location);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
        return locationLiveData;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        stopLocationUpdates();
    }

    void stopLocationUpdates() {
        getFusedLocationProviderClient(getApplication()).removeLocationUpdates(locationCallback);

    }
}
