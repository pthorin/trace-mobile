package trace.mobile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.StreamHandler;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.widget.TextView;
import android.net.http.*;

/**
 * 
 * @author petert
 * 353230000000000;2011-03-24T17:17:00Z;109;01;80;00;0;0;0;0;13410;0;2;1;1300990620;62316018;17362360;0;0;198;4;000000000000;0;\\n
	17:52 [johanl(johanl@kartena)] där 
17:52 [johanl(johanl@kartena)] {IMEI};{create 
          time};109;01;80;00;0;0;0;0;13410;0;2;1;{position unix 
          time};{lat};{long};0;0;198;4;000000000000;0;\\n
17:57 [johanl(johanl@kartena)] jag har räknat ut några wgs84 värden som borde 
          motsvara ungefär 10m i respektive ledd
17:57 [johanl(johanl@kartena)] lat: 0,000093
17:57 [johanl(johanl@kartena)] long: 0,000160
17:58 [johanl(johanl@kartena)] borde vara giltigt här i gbg ungefär

 */


public class LocationService {
	private static Main activity = null;
	
	public LocationService(Main activity) {
		LocationService.activity = activity;
		LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    
        
        LocationListener locationListener = new LocationListener() {

			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
			    TextView textView = (TextView)LocationService.activity.findViewById(R.id.textview);
				try {
					makeUseOfNewLocation(location);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					textView.setText(e.getMessage());
				} catch (UnknownHostException e) {
					e.printStackTrace();
					textView.setText(e.getMessage());
				} catch (IOException e) {
					String host = LocationService.activity.getString(R.string.traceHost);
					String appRoot = LocationService.activity.getString(R.string.appRoot);
					String s = "http://"+host+"/"+appRoot+"/Login.aspx";
					
					e.printStackTrace();
					textView.setText(e.getMessage());
					
					DefaultHttpClient dhc = new DefaultHttpClient();
					try {
						HttpResponse response = dhc.execute(new HttpHost(host), new HttpGet(s));
						e.printStackTrace();
						if (response.getStatusLine().getStatusCode() == 200) {
							textView.setText("Started up PPF.");
							onLocationChanged(location);
						} else {
							textView.setText("PPF is not running, failed to start.");
						}
					} catch (ClientProtocolException e1) {
						e1.printStackTrace();
						textView.setText("Failed to start PPF. " + e1.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						textView.setText("Failed to start PPF. " + e1.getMessage());
					}			
				}
				
			}

			private void makeUseOfNewLocation(Location location) throws NumberFormatException, UnknownHostException, IOException {
				((TextView)LocationService.activity.findViewById(R.id.textview)).setText("Updating location...");
				String dstName = LocationService.activity.getString(R.string.traceHost);
				int dstPort = Integer.parseInt(LocationService.activity.getString(R.string.tracePort));
				Socket socket = new Socket(dstName, dstPort);
				//ObjectOutputStream out = (ObjectOutputStream) socket.getOutputStream();
				PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
				
				TelephonyManager telMgr = (TelephonyManager) LocationService.activity.getSystemService(Context.TELEPHONY_SERVICE);
				
				// {IMEI};{create time};109;01;80;00;0;0;0;0;13410;0;2;1;{position Unix time};{latitude};{longitude};0;0;{bearing/direction};4;000000000000;0;\\n
				
				CharSequence createTime = DateFormat.format("yyyy-MM-ddTkk:mm:ssZ", location.getTime()); // TODO set to UTC (currently local time)... 
				StringBuilder a1maxMsg =  new StringBuilder();
				a1maxMsg.append(telMgr.getDeviceId() + ";");
				a1maxMsg.append(createTime + ";");
				a1maxMsg.append("109;01;80;00;0;0;0;0;13410;0;2;1;");
				a1maxMsg.append((location.getTime() / 1000) + ";");
				a1maxMsg.append(((long)(location.getLatitude() * 1000000)) + ";");
				a1maxMsg.append(((long)(location.getLongitude() * 1000000))+ ";");
				//a1maxMsg.append("0;0;");
				a1maxMsg.append((int)location.getSpeed() + ";" + (int)location.getAltitude() + ";");
				a1maxMsg.append((int)location.getBearing() + ";");
				a1maxMsg.append("4;000000000000;0;");
				((TextView)LocationService.activity.findViewById(R.id.textview)).setText("Sending location to server...");
				try {	
					out.println(a1maxMsg.toString());
				} catch (Exception e) {
					((TextView)LocationService.activity.findViewById(R.id.textview)).setText(e.getMessage());
				}
				((TextView)LocationService.activity.findViewById(R.id.textview)).setText(a1maxMsg.toString());
				
				out.flush();
				out.close();
				socket.close();				
			}

			public void onProviderDisabled(String provider) {}
			public void onProviderEnabled(String provider) {}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
        	
        };
        
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0, locationListener);
	}
}
