package edu.uos.openroute;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.uos.openroute.util.Position;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadNode;

/**
 * This activity shows the instructions in an linerar way.
 */
public class RouteInstructions extends AppCompatActivity {

    // Named constants for required data of the activity provided in the bundle on create.
    public final static String ROAD = "ROAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_instructions);

        // Load the required road of the activity and fail otherwise.
        Bundle data = this.getIntent().getExtras();
        if (data != null) {
            Road road = (Road) data.get(ROAD);
            if (road != null) {
                // Create a usable model for the ModelViewController pattern
                ListAdapter model = this.getModel(road);

                // Set the model and controller to the view
                ((ListView)findViewById(R.id.instruction_list)).setAdapter(model);
                return;
            }
        }

        throw new IllegalArgumentException("Required extras for the activity are missing!");
    }

    /**
     * Create the model and controller for a ListView.
     * @param road The route of interest.
     * @return a suitable adapter.
     */
    private ListAdapter getModel(Road road) {
        // Overwrite the ArrayAdapter for custom labels
        return new ArrayAdapter<RoadNode>(this, android.R.layout.simple_list_item_2, android.R.id.text1, road.mNodes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Let the superclass make the heavy load
                View view = super.getView(position, convertView, parent);

                // Find the widgets of the Android default layout "simple_list_item_2"
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                // Get the node of interest
                RoadNode node = (RoadNode)this.getItem(position);

                // Set the text and ensure the second text is smaller
                text1.setText(node.mInstructions);
                text2.setText(Road.getLengthDurationText(RouteInstructions.this, node.mLength, node.mDuration));
                text2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);

                return view;
            }
        };
    }
}
