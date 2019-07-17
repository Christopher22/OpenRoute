package edu.uos.openroute.routing;

import android.support.annotation.NonNull;
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
import java.util.Locale;
import java.util.Scanner;

/**
 * This class allows the calculation of routes utilizing the Open Route Service.
 */
public class OpenRouteServiceManager extends RoadManager {

    // Name for the used logger.
    private final static String LOGGER_ID = "OpenRouteService";

    private final Profile profile;
    private final String apiKey;

    /**
     * Creates a new manager for cars.
     *
     * @param apiKey The API key for the service.
     */
    public OpenRouteServiceManager(String apiKey) {
        this(apiKey, Profile.CAR);
    }

    /**
     * Create a new manager.
     *
     * @param apiKey         The API key for the service.
     * @param currentProfile The profile.
     */
    public OpenRouteServiceManager(String apiKey, Profile currentProfile) {
        this.profile = currentProfile;
        this.apiKey = apiKey;
    }

    /**
     * Parse the response given by Open Route Service.
     *
     * @param response The response given.
     * @return parsed road.
     * @throws JSONException if the response is not valid.
     */
    private static Road parseRoad(JSONObject response) throws JSONException {
        Road road = new Road();

        // Parse the bounding box
        JSONArray bbox = response.getJSONArray("bbox");
        road.mBoundingBox = new BoundingBox(bbox.getDouble(1), bbox.getDouble(2), bbox.getDouble(3), bbox.getDouble(0));

        // Extract the segments of interest and get distance and duration
        JSONObject route = response.getJSONArray("routes").getJSONObject(0).getJSONArray("segments").getJSONObject(0);
        road.mLength = route.getDouble("distance");
        road.mDuration = route.getDouble("duration");

        // Parse all the nodes and add them if they are of interest
        JSONArray nodes = route.getJSONArray("steps");
        for (int i = 0, length = nodes.length(); i < length; ++i) {
            RoadNode node = OpenRouteServiceManager.parseStep(nodes.getJSONObject(i));
            if (node != null) {
                road.mNodes.add(node);
                road.mRouteHigh.add(node.mLocation);
            }
        }

        // Build the legs and mark the route as valid
        road.buildLegs(road.mRouteHigh);
        road.mStatus = Road.STATUS_OK;
        return road;
    }

    /**
     * Parse a single step of the route.
     *
     * @param step The step as part of the JSON response.
     * @return The parsed step.
     * @throws JSONException if the response is not valid.
     */
    private static RoadNode parseStep(JSONObject step) throws JSONException {
        RoadNode node = new RoadNode();

        // Fill all the attributes
        node.mDuration = step.getDouble("duration");
        node.mLength = step.getDouble("distance");
        node.mManeuverType = step.getInt("type");
        node.mInstructions = step.getString("instruction");

        // A maneuver type of 10 just mean "arrive". Skip that.
        if (node.mManeuverType == 10) {
            return null;
        }

        // Parse the location
        JSONArray location = step.getJSONObject("maneuver").getJSONArray("location");
        node.mLocation = new GeoPoint(location.getDouble(1), location.getDouble(0));
        return node;
    }

    /**
     * Generate an appropriate JSON for request.
     *
     * @param waypoints Waypoints of interest.
     * @return an appropriate JSON for request, formatted as string.
     */
    private static String generateRequestJson(ArrayList<GeoPoint> waypoints) {
        JSONObject request = new JSONObject();
        try {
            // Include (english) instructions and maneuvers and set kilometers as unit.
            request.put("instructions", true);
            request.put("instructions_format", "text");
            request.put("language", Language.currentLanguage().toString());
            request.put("maneuvers", true);
            request.put("units", "km");

            // Add the waypoints to the JSON - longitude first!
            JSONArray coordinates = new JSONArray();
            for (GeoPoint waypoint : waypoints) {
                JSONArray waypointArray = new JSONArray();
                waypointArray.put(waypoint.getLongitude());
                waypointArray.put(waypoint.getLatitude());
                coordinates.put(waypointArray);
            }
            request.put("coordinates", coordinates);
        } catch (JSONException ignored) {
            // Should never be called. But well...
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
            // Get the url and specify all the required header data and a timeout of 2 seconds
            client = (HttpURLConnection) profile.getEndpoint().openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Authorization", apiKey);
            client.setRequestProperty("Content-Type", "application/json");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setDoInput(true);
            client.setUseCaches(false);
            client.setConnectTimeout(2000);

            // Generate the request JSON and send it to the server
            String requestJsonString = OpenRouteServiceManager.generateRequestJson(waypoints);
            byte[] requestJson = requestJsonString.getBytes();
            client.setFixedLengthStreamingMode(requestJson.length);
            try (OutputStream requestStream = client.getOutputStream()) {
                requestStream.write(requestJson);
                requestStream.flush();
            }

            // Recieve the response and try to parse it
            final int code = client.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                // Read the stream into a string and parse it as JSON.
                String jsonString = new Scanner(client.getInputStream()).useDelimiter("\\A").next();
                JSONObject response = new JSONObject(jsonString);

                // Generate the road from the json
                return new Road[]{OpenRouteServiceManager.parseRoad(response)};
            } else {
                // The server report some kind of error. Log that.
                String error = new Scanner(client.getErrorStream()).useDelimiter("\\A").next();
                Log.e(LOGGER_ID, String.format("Got unsuccessful access code %d: %s", code, error));
                return null;
            }
        } catch (MalformedURLException | ProtocolException connection) {
            // Something is wrong with the connection.
            Log.e(LOGGER_ID, "Unable to initialize response");
            return null;
        } catch (IOException io) {
            // Something is wrong with the IO. Probably a time out?
            Log.e(LOGGER_ID, "IO error while accessing route");
            return null;
        } catch (JSONException json) {
            // There was an error when parsing the JSON. The response might be corrupted or our parser wrong.
            Log.e(LOGGER_ID, "JSON error while interpreting response");
            return null;
        } finally {
            // Independent of the success ensure that the connection is closed properly
            if (client != null) {
                client.disconnect();
            }
        }
    }

    /**
     * Different available profiles for route generation.
     */
    public enum Profile {
        CAR("driving-car"),
        BICYCLE("cycling-regular"),
        WALKING("foot-walking");

        private final String code;

        Profile(String code) {
            this.code = code;
        }

        /**
         * Create a valid endpoint of the API where a request may be send to.
         *
         * @return the API URL.
         * @throws MalformedURLException if generation of URL failed to obscure reasons.
         */
        public URL getEndpoint() throws MalformedURLException {
            return new URL(String.format("https://api.openrouteservice.org/v2/directions/%s/json", this.code));
        }
    }

    /**
     * An enum with supported languages for routing directions.
     */
    public enum Language {
        ENGLISH("en"),
        GERMAN("de"),
        FRENCH("fr"),
        SPANISH("es");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        /**
         * Returns the currently used language on the system.
         *
         * @return the currently used language on the system or English otherwise.
         */
        public static Language currentLanguage() {
            // Get current language
            String currentLanguage = Locale.getDefault().getLanguage();

            // Search for appropriate language and return English otherwise.
            for (Language language : Language.class.getEnumConstants()) {
                if (language.code.equals(currentLanguage)) {
                    return language;
                }
            }
            return Language.ENGLISH;
        }

        @NonNull
        @Override
        public String toString() {
            return code;
        }
    }
}
