package edu.uos.openroute.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import org.osmdroid.util.GeoPoint;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A position on the earth, specified by latitude and longitude.
 */
public class Position implements Parcelable {

    private final double latitude, longitude;

    /**
     * Create a new position from two positions in degree.
     *
     * @param latitude  The latitude in degrees.
     * @param longitude The longitude in degrees.
     * @throws IllegalArgumentException if the latitude or longitude is out of the valid range.
     */
    public Position(double latitude, double longitude) throws IllegalArgumentException {
        if (Math.abs(latitude) > 90.0) {
            throw new IllegalArgumentException("Latitude is out of range");
        } else if (Math.abs(longitude) > 180.0) {
            throw new IllegalArgumentException("Longitude is out of range");
        }

        this.latitude = Math.toRadians(latitude);
        this.longitude = Math.toRadians(longitude);
    }

    /**
     * Create a new position from two strings, supporting different decimal notation (i.e. ',' instead of '.' in German)
     *
     * @param latitude  The latitude in degrees.
     * @param longitude The longitude in degrees.
     * @return The position or null, if the strings do not represent a valid latitude or longitude.
     */
    public static Position fromString(String latitude, String longitude) {
        NumberFormat formatter = NumberFormat.getInstance();
        try {
            double latitude_dbl = formatter.parse(latitude).doubleValue();
            double longitude_dbl = formatter.parse(longitude).doubleValue();
            return new Position(latitude_dbl, longitude_dbl);
        } catch (ParseException | IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Convert a double into the "degree, minutes, seconds" format.
     *
     * @param degrees The degrees which are to be formatted.
     * @return The formatted result.
     */
    public static String toDegreeMinutesSeconds(double degrees) {
        int full_degrees = (int) Math.floor(degrees);
        double minutesTmp = 60.0 * (degrees - full_degrees);
        int minutes = (int) Math.floor(minutesTmp);
        int seconds = (int) Math.round(60.0 * (minutesTmp - minutes));
        return String.format(Locale.ENGLISH, "%d° %d' %d''", full_degrees, minutes, seconds);
    }

    /**
     * Return the latitude in degrees.
     *
     * @return Latitude in degrees.
     */
    public double getLatitude() {
        return Math.toDegrees(latitude);
    }

    /**
     * Return the longitude in degrees.
     *
     * @return Longitude in degrees.
     */
    public double getLongitude() {
        return Math.toDegrees(longitude);
    }

    /**
     * Create a GeoPoint usable in OSMDroid from this position.
     *
     * @return The GeoPoint.
     */
    public GeoPoint toGeoPoint() {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }

    /**
     * Calculate a rough approximation between two point on the earth.
     *
     * @param otherPosition The position which is used as other point.
     * @return the distance.
     */
    public double distance(Position otherPosition) {
        final double EARTH_RADIUS = 6371;
        double[] pos1 = this.toSpace(EARTH_RADIUS), pos2 = otherPosition.toSpace(EARTH_RADIUS);

        double alpha = 0.0;
        for (int i = 0; i < pos1.length; ++i) {
            alpha += pos1[i] * pos2[i];
        }
        alpha /= EARTH_RADIUS * EARTH_RADIUS;
        alpha = Math.acos(alpha);
        return alpha * EARTH_RADIUS;
    }

    /**
     * Transfer the radians into another coordinate system.
     *
     * @param r The radius.
     * @return The mapped coordinates.
     */
    private double[] toSpace(double r) {
        return new double[]{
                r * Math.cos(latitude) * Math.cos(longitude),
                r * Math.cos(latitude) * Math.sin(longitude),
                r * Math.sin(latitude)
        };
    }

    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.ENGLISH, "%f, %f", this.getLongitude(), this.getLatitude());
    }


    /*
     * ---------------- The following methods make the object sendable to other activities. ----------------
     */

    private Position(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Parcelable.Creator<Position> CREATOR = new Parcelable.Creator<Position>() {
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }

        public Position[] newArray(int size) {
            return new Position[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
    }
}
