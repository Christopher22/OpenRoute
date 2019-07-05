package edu.uos.openroute;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class RouteViewer extends AppCompatActivity implements MapView.OnFirstLayoutListener, MapEventsReceiver {

    public static final String ROAD = "ROAD";

    private MapView map;
    private Road road;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_route_viewer);

        Bundle data = this.getIntent().getExtras();
        this.road = (Road) data.get(ROAD);

        this.map = this.findViewById(R.id.map);
        this.map.setMultiTouchControls(true);
        this.map.addOnFirstLayoutListener(this);
        this.map.setTileSource(TileSourceFactory.MAPNIK);

        this.drawRoad();
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
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        map.zoomToBoundingBox(road.mBoundingBox, false);
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

    private void drawRoad() {
        // Add the route overlay
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);

        // Add the nodes
        for (int i = 0; i < road.mNodes.size(); i++) {
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            //nodeMarker.setIcon(nodeIcon);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setTitle("Step " + i);
            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
            map.getOverlays().add(nodeMarker);
            //nodeMarker.setImage(icon);
        }

        map.invalidate();
    }
}
