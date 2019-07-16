package edu.uos.openroute;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import edu.uos.openroute.routing.OpenRouteServiceManager;
import edu.uos.openroute.util.Position;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * This activity calculates a route between two points in the background while showing an waiting animation.
 */
public class RouteCalculationActivity extends AppCompatActivity {

    /**
     * Named constants for required data of the activity provided in the bundle on create.
     */
    public final static String START = "START", DESTINATION = "DESTINATION", PROFILE = "PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_calculation);

        // Load the required data for this activity
        Bundle data = this.getIntent().getExtras();
        if (data != null) {
            Position start = (Position) data.get(START);
            Position destination = (Position) data.get(DESTINATION);
            OpenRouteServiceManager.Profile profile = (OpenRouteServiceManager.Profile) data.get(PROFILE);

            // Check all required data is available ...
            if (start != null && destination != null && profile != null) {
                RoadManager roadManager = new OpenRouteServiceManager(getString(R.string.OpenRouteServiceAPIKey), profile);

                // ... and start the calculate asynchronously in background.
                new RouteCalculator(this, start, destination, roadManager).execute();
                return;
            }
        }

        // Fail, if the required data is not provided.
        throw new IllegalArgumentException("Required extras for the activity are missing!");
    }

    /**
     * The asynchronously route calculation running in the background.
     */
    private static class RouteCalculator extends AsyncTask<Void, Void, Road> {
        private final RoadManager roadManager;
        private final Position start, destination;
        private Activity activity;

        public RouteCalculator(Activity parent, Position start, Position destination, RoadManager roadManager) {
            this.start = start;
            this.destination = destination;
            this.roadManager = roadManager;
            this.activity = parent;
        }

        @Override
        protected Road doInBackground(Void... nothing) {
            // Prepare the inputs
            ArrayList<GeoPoint> points = new ArrayList<>();
            points.add(start.toGeoPoint());
            points.add(destination.toGeoPoint());

            // Calculate the route
            return roadManager.getRoad(points);
        }

        @Override
        protected void onPostExecute(Road road) {
            // If the calculation was not successful ...
            if (road == null) {
                // ... return to the previous activity with indication that there was a problem ...
                this.activity.setResult(-1);
                this.activity.finish();
                this.activity = null;
                return;
            }

            // ... , otherwise visualize the route in a new activity.
            Intent viewIntent = new Intent(activity, RouteViewer.class);
            viewIntent.putExtra(RouteViewer.ROAD, road);
            viewIntent.putExtra(RouteViewer.START, start);
            viewIntent.putExtra(RouteViewer.DESTINATION, destination);
            activity.startActivity(viewIntent);
        }
    }
}
