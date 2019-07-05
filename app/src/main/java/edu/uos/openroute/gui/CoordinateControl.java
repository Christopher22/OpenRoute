package edu.uos.openroute.gui;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import edu.uos.openroute.R;
import edu.uos.openroute.util.LocationAccess;
import edu.uos.openroute.util.Position;

import java.text.NumberFormat;

public class CoordinateControl {

    private EditText latitude, longitude;

    public CoordinateControl(Activity parent, int latitudeId, int longitudeId, int setCurrentLocationButtonId) {
        this.latitude = parent.findViewById(latitudeId);
        this.longitude = parent.findViewById(longitudeId);

        if (ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(parent)) {
            Button setCurrentLocationButton = parent.findViewById(setCurrentLocationButtonId);
            setCurrentLocationButton.setVisibility(View.VISIBLE);
            setCurrentLocationButton.setOnClickListener(new SetLocationOperation(parent, latitude, longitude));
        }
    }

    public Position getPosition() {
        return Position.fromString(this.latitude.getText().toString(), this.longitude.getText().toString());
    }

    private static class SetLocationOperation extends LocationAccess {
        private final EditText latitude, longitude;

        public SetLocationOperation(Activity activity, EditText latitude, EditText longitude) {
            super(activity);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void onSuccess(Location location) {
            if (location == null) {
                Toast.makeText(this.activity, R.string.location_not_available, Toast.LENGTH_LONG).show();
                return;
            }

            NumberFormat formatter = NumberFormat.getInstance();
            this.longitude.setText(formatter.format(location.getLongitude()));
            this.latitude.setText(formatter.format(location.getLatitude()));
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            LocationAccess.MissingRightsException missingRights = (LocationAccess.MissingRightsException) e;
            if (!missingRights.isFinalDecision()) {
                missingRights.showExplanation(R.string.permission_reason_title, R.string.permission_reason_text, R.string.yes, R.string.no);
            } else {
                Toast.makeText(this.activity, R.string.location_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
}
