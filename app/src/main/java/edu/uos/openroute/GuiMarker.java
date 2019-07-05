package edu.uos.openroute;

import android.widget.TextView;
import edu.uos.openroute.util.Position;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class GuiMarker extends InfoWindow {
    private Position coordinate;

    public GuiMarker(Position coordinate, int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        this.coordinate = coordinate;
    }

    public void onOpen(Object item) {
        TextView contentView = mView.findViewById(R.id.bubble_content);
        String content = this.mView.getContext().getString(
                R.string.bubble_content,
                Position.toDegreeMinutesSeconds(this.coordinate.getLatitude()),
                Position.toDegreeMinutesSeconds(this.coordinate.getLongitude())
        );
        contentView.setText(content);
    }

    public void onClose() {
    }
}
