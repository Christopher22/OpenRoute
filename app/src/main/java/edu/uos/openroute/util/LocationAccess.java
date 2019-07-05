package edu.uos.openroute.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.View;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


public abstract class LocationAccess implements View.OnClickListener, OnSuccessListener<android.location.Location>, OnFailureListener {

    private static SparseArray<LocationAccess> locationOperations;
    protected final Activity activity;
    private final FusedLocationProviderClient locationProvider;
    private int id;
    public LocationAccess(Activity activity) {
        this.activity = activity;
        this.locationProvider = new FusedLocationProviderClient(activity);

        this.id = LocationAccess.getOperations().size();
        LocationAccess.getOperations().append(this.id, this);
    }

    public static void handlePermissionRequest(int requestCode, String[] permissions, int[] grantResults) {
        LocationAccess operation = LocationAccess.getOperations().get(requestCode);
        if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            operation.getLocation();
        } else {
            operation.onFailure(new MissingRightsException(operation));
        }
    }

    private static SparseArray<LocationAccess> getOperations() {
        if (LocationAccess.locationOperations == null) {
            LocationAccess.locationOperations = new SparseArray<>();
        }
        return LocationAccess.locationOperations;
    }

    private void getLocation() {
        try {
            this.locationProvider.getLastLocation().addOnSuccessListener(this);
        } catch (SecurityException ex) {
            this.onFailure(new MissingRightsException(this));
        }
    }

    private void requestPermissions() {
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissions, this.id);
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions();
        } else {
            this.getLocation();
        }
    }

    public static class MissingRightsException extends Exception {
        private final boolean finalDecision;
        private final LocationAccess access;

        private MissingRightsException(LocationAccess access) {
            super("The user did not granded the required permissions.");
            this.access = access;
            this.finalDecision = !ActivityCompat.shouldShowRequestPermissionRationale(access.activity, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        public boolean isFinalDecision() {
            return finalDecision;
        }

        public void showExplanation(int title, int messages, int yes, int no) {
            new AlertDialog.Builder(access.activity)
                    .setTitle(title)
                    .setMessage(messages)
                    .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            access.requestPermissions();
                        }
                    })
                    .setNegativeButton(no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .create()
                    .show();
        }
    }
}
