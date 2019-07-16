package edu.uos.openroute.gui;

import android.app.Activity;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import edu.uos.openroute.routing.OpenRouteServiceManager;
import edu.uos.openroute.util.EnumAdapter;

/**
 * A tiny wrapper for a given spinner extending it with the opportunity of selecting profiles of interest.
 */
public class ProfileSpinner {
    private final Spinner spinner;

    /**
     * Apply the wrapper on an existing spinner.
     * @param parent The activity of interest.
     * @param spinnerId The id of the Spinner widget.
     */
    public ProfileSpinner(Activity parent, int spinnerId) {
        // Create the data adapter -> ModelViewController!
        SpinnerAdapter data = new EnumAdapter<>(parent.getApplicationContext(), android.R.layout.simple_spinner_item, OpenRouteServiceManager.Profile.class);

        // Set the view
        this.spinner = parent.findViewById(spinnerId);
        this.spinner.setAdapter(data);
    }

    /**
     * Return the currently selected profile.
     * @return currently selected profile.
     */
    public OpenRouteServiceManager.Profile getProfile() {
        return ((EnumAdapter.ValueWrapper<OpenRouteServiceManager.Profile>)spinner.getSelectedItem()).Value;
    }
}
