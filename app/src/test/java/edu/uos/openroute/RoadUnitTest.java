package edu.uos.openroute;

import edu.uos.openroute.routing.OpenRouteServiceManager;
import org.junit.Test;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class RoadUnitTest {
    @Test
    public void testRoute() {
        ArrayList<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(49.41461, 8.681495));
        points.add(new GeoPoint(49.420318, 8.687872));

        RoadManager manager = new OpenRouteServiceManager();
        Road road = manager.getRoad(points);
        assertNotNull(manager.getRoad(points));
        assertEquals(293.9, road.mDuration, 0.01);
    }
}
