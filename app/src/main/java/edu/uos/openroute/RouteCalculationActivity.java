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

public class RouteCalculationActivity extends AppCompatActivity {

    public final static String START = "START";
    public final static String DESTINATION = "DESTINATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_calculation);

        Bundle data = this.getIntent().getExtras();
        new RouteCalculator(this, (Position) data.get(START), (Position) data.get(DESTINATION), new OpenRouteServiceManager(getString(R.string.OpenRouteServiceAPIKey))).execute();
    }

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
            ArrayList<GeoPoint> points = new ArrayList<>();
            points.add(new GeoPoint(start.getLatitude(), start.getLongitude()));
            points.add(new GeoPoint(destination.getLatitude(), destination.getLongitude()));
            return roadManager.getRoad(points);
        }

        @Override
        protected void onPostExecute(Road road) {
            if (road == null) {
                this.activity.finish();
                return;
            }

            Intent viewIntent = new Intent(activity, RouteViewer.class);
            viewIntent.putExtra(RouteViewer.ROAD, road);
            activity.startActivity(viewIntent);
        }
    }
}
