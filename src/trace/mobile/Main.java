package trace.mobile;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.os.Bundle;

//public class Main extends MapActivity {
public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //MapView mapView = (MapView) findViewById(R.id.mapview);
        //mapView.setBuiltInZoomControls(true);
        
        new LocationService(this);
        
        
    }

    /*
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
    */
    
}