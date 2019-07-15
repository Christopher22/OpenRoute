package edu.uos.openroute;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import edu.uos.openroute.util.Position;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * This activity visualizes a calculated road on an interactive map.
 */
public class RouteViewer extends AppCompatActivity implements MapView.OnFirstLayoutListener, MapEventsReceiver {

    /**
     * Named constants for required data of the activity provided in the bundle on create.
     */
    public static final String ROAD = "ROAD", START = "DESTINATION", DESTINATION = "START";

    private MapView map;
    private Road road;
    private Position start, destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare activity
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_route_viewer);

        // Load the required arguments for the activity: The road, start point and destination point and fail otherwise.
        Bundle data = this.getIntent().getExtras();
        if (data != null) {
            this.road = (Road) data.get(ROAD);
            this.start = (Position) data.get(START);
            this.destination = (Position) data.get(DESTINATION);
        }

        if (this.road == null || this.start == null || this.destination == null) {
            throw new IllegalArgumentException("Required extras for the activity are missing!");
        }

        // Instantiate the map
        this.map = this.findViewById(R.id.map);
        this.map.setMultiTouchControls(true);
        this.map.addOnFirstLayoutListener(this);
        this.map.setTileSource(TileSourceFactory.MAPNIK);

        // Create all the markers of the route to the map
        this.drawRoute();
    }

    /**
     * Add a marker with a specific title and a message on a specific position and return it for further customization.
     *
     * @param title    The title of the marker.
     * @param content  The text content of the marker.
     * @param position The position of the marker.
     * @return The created marker provided for further customization.
     */
    private Marker addMarker(String title, String content, GeoPoint position) {
        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(position);
        nodeMarker.setTitle(title);
        nodeMarker.setSnippet(content);
        map.getOverlays().add(nodeMarker);
        return nodeMarker;
    }

    /**
     * Add a marker with a specific title and a message on a specific position and return it for further customization.
     *
     * @param title    The title of the marker.
     * @param position he position of the marker.
     * @return The created marker provided for further customization.
     */
    private Marker addMarker(String title, Position position) {
        return this.addMarker(
                title,
                getString(R.string.marker, Position.toDegreeMinutesSeconds(position.getLatitude()), Position.toDegreeMinutesSeconds(position.getLongitude())),
                position.toGeoPoint()
        );
    }

    /**
     * Draw the route on the map.
     */
    private void drawRoute() {
        // Add the route overlay
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);

        // Add the nodes
        for (int i = 0; i < road.mNodes.size(); i++) {
            RoadNode node = road.mNodes.get(i);

            // Add the marker with specific icon and transparencey effect.
            Marker marker = this.addMarker(getString(R.string.step, i + 1, road.mNodes.size()), node.mInstructions, node.mLocation);
            marker.setAlpha(0.5f);
            marker.setIcon(getDrawable(R.mipmap.marker));
        }

        // Add start and destination marker with bubbles
        this.addMarker(getString(R.string.start), start).setIcon(getDrawable(R.mipmap.marker_start_end));
        this.addMarker(getString(R.string.destination), destination).setIcon(getDrawable(R.mipmap.marker_start_end));

        // Enforce recalculation of the map
        map.invalidate();
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        // Zoom towards the bounding box provided from the calculated road.
        map.zoomToBoundingBox(road.mBoundingBox, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(this.map);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
