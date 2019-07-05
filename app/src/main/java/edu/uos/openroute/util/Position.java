package edu.uos.openroute.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Position implements Parcelable {
    public static final Parcelable.Creator<Position> CREATOR = new Parcelable.Creator<Position>() {
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }

        public Position[] newArray(int size) {
            return new Position[size];
        }
    };

    private final double latitude, longitude;

    public Position(double latitude, double longitude) throws IllegalArgumentException {
        if (Math.abs(latitude) > 90.0) {
            throw new IllegalArgumentException("Latitude is out of range");
        } else if (Math.abs(longitude) > 180.0) {
            throw new IllegalArgumentException("Longitude is out of range");
        }

        this.latitude = Math.toRadians(latitude);
        this.longitude = Math.toRadians(longitude);
    }

    private Position(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

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

    public static String toDegreeMinutesSeconds(double degrees) {
        int full_degrees = (int) Math.floor(degrees);
        double minutesTmp = 60.0 * (degrees - degrees);
        int minutes = (int) Math.floor(minutesTmp);
        int seconds = (int) Math.round(60.0 * (minutesTmp - minutes));
        return String.format(Locale.ENGLISH, "%d° %d' %d''", full_degrees, minutes, seconds);
    }

    public double getLatitude() {
        return Math.toDegrees(latitude);
    }

    public double getLongitude() {
        return Math.toDegrees(longitude);
    }

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
        return String.format(Locale.ENGLISH, "%f,%f", this.getLongitude(), this.getLatitude());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
    }
}
