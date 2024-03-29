package edu.uos.openroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    // Constant used to mark the specific request
    private static final int CALCULATION_REQUEST_CODE = 42;

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
        this.findViewById(R.id.btn_navigate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Parse the given positions
                Position start = RouteInputActivity.this.start.getPosition();
                Position destination = RouteInputActivity.this.destination.getPosition();

                // Check the positions for validity ...
                if (start == null) {
                    Toast.makeText(RouteInputActivity.this, R.string.start_invalid, LENGTH_SHORT).show();
                } else if (destination == null) {
                    Toast.makeText(RouteInputActivity.this, R.string.destination_invalid, LENGTH_SHORT).show();
                } else if (start.distance(destination) < 0.0001) {
                    Toast.makeText(RouteInputActivity.this, R.string.distance_invalid, LENGTH_SHORT).show();
                } else {
                    // ... and start the route calculation.
                    Intent viewIntent = new Intent(RouteInputActivity.this, RouteCalculationActivity.class);
                    viewIntent.putExtra(RouteCalculationActivity.START, start);
                    viewIntent.putExtra(RouteCalculationActivity.DESTINATION, destination);
                    viewIntent.putExtra(RouteCalculationActivity.PROFILE, RouteInputActivity.this.profile.getProfile());
                    RouteInputActivity.this.startActivityForResult(viewIntent, CALCULATION_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Redirect the permission events to the global permission controller
        LocationAccess.handlePermissionRequest(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Show an error if the calculation was not successful.
        if (requestCode == CALCULATION_REQUEST_CODE && resultCode < 0) {
            Toast.makeText(this, R.string.calculation_failed, Toast.LENGTH_LONG).show();
        }
    }
}
