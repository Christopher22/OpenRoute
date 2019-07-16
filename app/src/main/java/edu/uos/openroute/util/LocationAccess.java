package edu.uos.openroute.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * This class represents a access of the user location.
 * Once started (by default on click), it handles all the permission management and fire the location asynchronously if possible.
 */
public abstract class LocationAccess implements View.OnClickListener, OnSuccessListener<android.location.Location>, OnFailureListener {

    private static SparseArray<LocationAccess> locationOperations;
    protected final Activity activity;
    private final FusedLocationProviderClient locationProvider;
    private int id;

    /**
     * Create a new access operation.
     *
     * @param activity The parent activity.
     */
    public LocationAccess(Activity activity) {
        this.activity = activity;
        this.locationProvider = new FusedLocationProviderClient(activity);

        // Set an unique id to this specific operation and save it in a global list
        this.id = LocationAccess.getOperations().size();
        LocationAccess.getOperations().append(this.id, this);
    }

    /**
     * Check if the required services are available.
     *
     * @param context The context of the call.
     * @return true iff location access is available.
     */
    public static boolean isAvailable(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    /**
     * Handle the global response towards a permission request.
     * WARNING: You need to call this function in 'onRequestPermissionsResult' of the current activity.
     *
     * @param requestCode  The request code.
     * @param permissions  The permissions.
     * @param grantResults The granted results.
     */
    public static void handlePermissionRequest(int requestCode, String[] permissions, int[] grantResults) {
        // Find the operation which was requesting the position.
        LocationAccess operation = LocationAccess.getOperations().get(requestCode);

        // Search for the exact requested right ...
        for (int i = 0; i < permissions.length; ++i) {
            if (!permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                continue;
            }

            // ... and get the location if it was grated, otherwise report failure.
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                operation.getLocation();
            } else {
                operation.onFailure(new MissingRightsException(operation));
            }
        }
    }

    /**
     * A singletone for a global list of operations dealing with locations.
     *
     * @return existing operations.
     */
    private static SparseArray<LocationAccess> getOperations() {
        if (LocationAccess.locationOperations == null) {
            LocationAccess.locationOperations = new SparseArray<>();
        }
        return LocationAccess.locationOperations;
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions();
        } else {
            this.getLocation();
        }
    }

    /**
     * Get the location asynchronously. Once available, the OnSuccessListener callback will be called.
     */
    private void getLocation() {
        try {
            this.locationProvider.getLastLocation().addOnSuccessListener(this);
        } catch (SecurityException ex) {
            this.onFailure(new MissingRightsException(this));
        }
    }

    /**
     * Request the required permissions.
     */
    private void requestPermissions() {
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissions, this.id);
    }

    /**
     * An exception which is thrown on missing rights for the operation.
     */
    public static class MissingRightsException extends Exception {

        private final boolean finalDecision;
        private final LocationAccess access;

        /**
         * Creates a new exception.
         *
         * @param access The parent emitting the exception.
         */
        private MissingRightsException(LocationAccess access) {
            super("The user did not granded the required permissions.");

            this.access = access;
            this.finalDecision = !ActivityCompat.shouldShowRequestPermissionRationale(access.activity, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        /**
         * Check, if the user has denied the rights permanently.
         *
         * @return true iff the user has denied the rights permanently.
         */
        public boolean isFinalDecision() {
            return finalDecision;
        }

        /**
         * Show a message to the users explaining why the rights are required and provide a way of changing theri minds.
         *
         * @param title    The string id of the title.
         * @param messages The string id of the message.
         * @param yes      The string id of the yes, if the user has changed his/her mind.
         * @param no       The string id of the no.
         */
        public void showExplanation(int title, int messages, int yes, int no) {
            new AlertDialog.Builder(access.activity)
                    .setTitle(title)
                    .setMessage(messages)
                    .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Request permissions again
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
