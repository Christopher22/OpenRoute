package edu.uos.openroute.gui;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uos.openroute.R;
import edu.uos.openroute.util.LocationAccess;
import edu.uos.openroute.util.Position;

import java.text.NumberFormat;

/**
 * A wrapper around inputs used to enter a position.
 */
public class CoordinateControl {

    private final EditText latitude, longitude;

    /**
     * Apply the wrapper on an existing widgets.
     *
     * @param parent                     The activity.
     * @param latitudeId                 The text input for the latitude.
     * @param longitudeId                The text input for the longitude.
     * @param setCurrentLocationButtonId The button for setting both text inputs to the current position.
     */
    public CoordinateControl(Activity parent, int latitudeId, int longitudeId, int setCurrentLocationButtonId) {
        this.latitude = parent.findViewById(latitudeId);
        this.longitude = parent.findViewById(longitudeId);

        // Show the button for setting both text inputs to the current position iff the required services are available.
        if (LocationAccess.isAvailable(parent)) {
            Button setCurrentLocationButton = parent.findViewById(setCurrentLocationButtonId);
            setCurrentLocationButton.setVisibility(View.VISIBLE);
            setCurrentLocationButton.setOnClickListener(new SetLocationOperation(parent, latitude, longitude));
        }
    }

    /**
     * Return the position entered by the user.
     *
     * @return the position or 'null' if it is invalid.
     */
    public Position getPosition() {
        return Position.fromString(this.latitude.getText().toString(), this.longitude.getText().toString());
    }

    /**
     * An asychronious operation for setting the text inputs to the current position.
     */
    private static class SetLocationOperation extends LocationAccess {

        private final EditText latitude, longitude;

        /**
         * Create the operation which is started on click.
         *
         * @param activity  The activity.
         * @param latitude  The text input for the latitude.
         * @param longitude The text input for the longitude.
         */
        public SetLocationOperation(Activity activity, EditText latitude, EditText longitude) {
            super(activity);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void onSuccess(Location location) {
            // Show error if no valid location was provided by Android.
            if (location == null) {
                Toast.makeText(this.activity, R.string.location_not_available, Toast.LENGTH_LONG).show();
                return;
            }

            // Format the resulting position accordingly to the decimal layout of the user language.
            NumberFormat formatter = NumberFormat.getInstance();
            this.longitude.setText(formatter.format(location.getLongitude()));
            this.latitude.setText(formatter.format(location.getLatitude()));
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            // Get the exact reason for the missing rights
            LocationAccess.MissingRightsException missingRights = (LocationAccess.MissingRightsException) e;

            // Try to convince the user to grand the rights if he had not denied them permanently.
            if (!missingRights.isFinalDecision()) {
                missingRights.showExplanation(R.string.permission_reason_title, R.string.permission_reason_text, R.string.yes, R.string.no);
            } else {
                Toast.makeText(this.activity, R.string.location_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
}
