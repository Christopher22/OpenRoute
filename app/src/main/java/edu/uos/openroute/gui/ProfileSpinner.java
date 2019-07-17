package edu.uos.openroute.gui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Spinner;
import edu.uos.openroute.routing.OpenRouteServiceManager;
import edu.uos.openroute.util.EnumAdapter;

/**
 * A tiny wrapper for a given spinner extending it with the opportunity of selecting profiles of interest.
 */
public class ProfileSpinner extends EnumAdapter<OpenRouteServiceManager.Profile> {
    private final Spinner view;
    private final Activity parent;

    /**
     * Apply the wrapper on an existing view.
     *
     * @param parent    The activity of interest.
     * @param spinnerId The id of the Spinner widget.
     */
    public ProfileSpinner(Activity parent, int spinnerId) {
        // Create the data adapter -> Model, View, Controller!
        super(parent.getApplicationContext(), android.R.layout.simple_spinner_item, OpenRouteServiceManager.Profile.class);

        // Set the view
        this.view = parent.findViewById(spinnerId);
        this.view.setAdapter(this);

        this.parent = parent;
    }

    /**
     * Return the currently selected profile.
     *
     * @return currently selected profile.
     */
    public OpenRouteServiceManager.Profile getProfile() {
        return ((EnumAdapter.ValueWrapper<OpenRouteServiceManager.Profile>) view.getSelectedItem()).Value;
    }

    @NonNull
    @Override
    protected String getName(OpenRouteServiceManager.Profile enumValue) {
        // Get the default name
        String valueName = super.getName(enumValue);

        // Check if their is a translation available and use it if possible
        int resourceId = view.getResources().getIdentifier(valueName.toLowerCase(), "string", parent.getPackageName());
        return resourceId != 0 ? parent.getString(resourceId) : valueName;
    }
}
