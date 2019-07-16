package edu.uos.openroute.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;


/**
 * A small helper class for making enums selectable in a List/Spinner with customizable labels.
 *
 * @param <T> The enum whose values are choosable.
 */
public class EnumAdapter<T extends Enum<T>> extends ArrayAdapter<EnumAdapter.ValueWrapper<T>> {

    /**
     * Create a new adapter for an enum of interest.
     *
     * @param context   The context of the adapter.
     * @param resource  The resource used to visualize.
     * @param enumClass The enum of interest.
     */
    public EnumAdapter(Context context, int resource, Class<T> enumClass) {
        super(context, resource);
        for (T value : enumClass.getEnumConstants()) {
            this.add(new ValueWrapper<T>(this, value));
        }
    }

    /**
     * Generates a label for a given enum value, probably independent from the enum itself. Might be overritten for cusomization.
     *
     * @param enumValue The enum whose name is of interest.
     * @return A name or label presented to the user.
     */
    @NonNull
    protected String getName(T enumValue) {
        String name = enumValue.name();
        return String.format("%s%s", name.substring(0, 1).toUpperCase(), name.substring(1).toLowerCase());
    }

    /**
     * A wrapper for the values, providing a customized "toString" depending on the adapter.
     *
     * @param <T>
     */
    public final static class ValueWrapper<T extends Enum<T>> {
        public final T Value;
        private final EnumAdapter<T> parent;

        /**
         * Internally used to create the wrapper.
         *
         * @param parent The parent adopter.
         * @param value  The enum value of interest
         */
        private ValueWrapper(EnumAdapter<T> parent, T value) {
            this.parent = parent;
            this.Value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return this.parent.getName(this.Value);
        }
    }
}