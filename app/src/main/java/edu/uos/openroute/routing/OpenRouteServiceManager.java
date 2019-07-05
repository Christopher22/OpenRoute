package edu.uos.openroute.routing;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class OpenRouteServiceManager extends RoadManager {

    private final Profile profile;
    private final String apiKey;
    public OpenRouteServiceManager(String apiKey) {
        this(apiKey, Profile.CAR);
    }

    public OpenRouteServiceManager(String apiKey, Profile currentProfile) {
        super();
        this.profile = currentProfile;
        this.apiKey = apiKey;
    }

    private static Road parseRoad(JSONObject response) throws JSONException {
        Road road = new Road();

        JSONArray bbox = response.getJSONArray("bbox");
        road.mBoundingBox = new BoundingBox(bbox.getDouble(1), bbox.getDouble(2), bbox.getDouble(3), bbox.getDouble(0));

        JSONObject route = response.getJSONArray("routes").getJSONObject(0).getJSONArray("segments").getJSONObject(0);
        road.mLength = route.getDouble("distance");
        road.mDuration = route.getDouble("duration");

        JSONArray nodes = route.getJSONArray("steps");
        for (int i = 0, length = nodes.length(); i < length; ++i) {
            RoadNode node = OpenRouteServiceManager.parseStep(nodes.getJSONObject(i));
            if (node != null) {
                road.mNodes.add(node);
                road.mRouteHigh.add(node.mLocation);
            }
        }

        road.mStatus = Road.STATUS_OK;
        road.buildLegs(road.mRouteHigh);

        return road;
    }

    private static RoadNode parseStep(JSONObject step) throws JSONException {
        RoadNode node = new RoadNode();
        node.mDuration = step.getDouble("duration");
        node.mLength = step.getDouble("distance");
        node.mManeuverType = step.getInt("type");
        node.mInstructions = step.getString("instruction");

        // A maneuver type of 10 just mean "arrive". Skip that.
        if (node.mManeuverType == 10) {
            return null;
        }

        JSONArray location = step.getJSONObject("maneuver").getJSONArray("location");
        node.mLocation = new GeoPoint(location.getDouble(1), location.getDouble(0));
        return node;
    }

    private static String generateRequestJson(ArrayList<GeoPoint> waypoints) {
        JSONObject request = new JSONObject();
        try {
            request.put("instructions", true);
            request.put("instructions_format", "text");
            request.put("language", "en");
            request.put("maneuvers", true);
            request.put("units", "km");

            JSONArray coordinates = new JSONArray();
            for (GeoPoint waypoint : waypoints) {
                JSONArray waypointArray = new JSONArray();
                waypointArray.put(waypoint.getLongitude());
                waypointArray.put(waypoint.getLatitude());
                coordinates.put(waypointArray);
            }

            request.put("coordinates", coordinates);
        } catch (JSONException ignored) {
        }
        return request.toString();
    }

    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road[] roads = this.getRoads(waypoints);
        return roads != null ? roads[0] : null;
    }

    @Override
    public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
        HttpURLConnection client = null;
        try {
            client = (HttpURLConnection) profile.getEndpoint().openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Authorization", apiKey);
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setDoInput(true);
            client.setUseCaches(false);
            client.setConnectTimeout(2000);

            String requestJsonString = OpenRouteServiceManager.generateRequestJson(waypoints);
            byte[] requestJson = requestJsonString.getBytes();
            client.setFixedLengthStreamingMode(requestJson.length);
            try (OutputStream requestStream = client.getOutputStream()) {
                requestStream.write(requestJson);
                requestStream.flush();
            }

            final int code = client.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                String jsonString = new Scanner(client.getInputStream()).useDelimiter("\\A").next();
                JSONObject response = new JSONObject(jsonString);
                return new Road[]{OpenRouteServiceManager.parseRoad(response)};
            } else {
                String error = new Scanner(client.getErrorStream()).useDelimiter("\\A").next();
                Log.e("OpenRouteService", String.format("Got unsuccessful access code %d: %s", code, error));
                return null;
            }
        } catch (MalformedURLException | ProtocolException connection) {
            Log.e("OpenRouteService", "Unable to initialize response");
            return null;
        } catch (IOException io) {
            Log.e("OpenRouteService", "IO error while accessing route");
            return null;
        } catch (JSONException json) {
            Log.e("OpenRouteService", "JSON error while interpreting response");
            return null;
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
    }

    public enum Profile {
        CAR("driving-car"),
        BICYCLE("cycling-regular"),
        WALKING("foot-walking");

        private String code;

        Profile(String code) {
            this.code = code;
        }

        public URL getEndpoint() throws MalformedURLException {
            return new URL(String.format("https://api.openrouteservice.org/v2/directions/%s/json", this.code));
        }
    }
}
