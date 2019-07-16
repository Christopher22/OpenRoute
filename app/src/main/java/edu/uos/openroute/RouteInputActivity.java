package edu.uos.openroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import edu.uos.openroute.gui.CoordinateControl;
import edu.uos.openroute.gui.ProfileSpinner;
import edu.uos.openroute.util.LocationAccess;
import edu.uos.openroute.util.Position;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * This activity provides parameters and helpers for calculating a custom route.
 */
public class RouteInputActivity extends AppCompatActivity {

    private CoordinateControl start, destination;
    private ProfileSpinner profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_input);

        // Group the widgets by their semantic role using independent "controllers"
        this.start = new CoordinateControl(this, R.id.input_start_latitude, R.id.input_start_longitude, R.id.btn_set_start);
        this.destination = new CoordinateControl(this, R.id.input_destination_latitude, R.id.input_destination_longitude, R.id.btn_set_destination);
        this.profile = new ProfileSpinner(this, R.id.spinner_profile);

        // Add the actual functionality
        findViewById(R.id.btn_navigate).setOnClickListener(new CalculateRoute(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Redirect the permission events to the global permission controller
        LocationAccess.handlePermissionRequest(requestCode, permissions, grantResults);
    }

    /**
     * A OneClickListener starting the route calculation with a reference to the parent activity.
     */
    private static class CalculateRoute implements View.OnClickListener {
        private final RouteInputActivity activity;

        public CalculateRoute(RouteInputActivity activity) {
            this.activity = activity;
        }

        public void onClick(View view) {
            // Parse the given positions
            Position start = this.activity.start.getPosition();
            Position destination = this.activity.destination.getPosition();

            // Check the positions for validity ...
            if (start == null) {
                Toast.makeText(activity, R.string.start_invalid, LENGTH_SHORT).show();
            } else if (destination == null) {
                Toast.makeText(activity, R.string.destination_invalid, LENGTH_SHORT).show();
            } else {
                // ... and start the route calculation.
                Intent viewIntent = new Intent(activity, RouteCalculationActivity.class);
                viewIntent.putExtra(RouteCalculationActivity.START, start);
                viewIntent.putExtra(RouteCalculationActivity.DESTINATION, destination);
                viewIntent.putExtra(RouteCalculationActivity.PROFILE, this.activity.profile.getProfile());
                activity.startActivity(viewIntent);
            }
        }
    }
}
