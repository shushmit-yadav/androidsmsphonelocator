package com.twolinessoftware.android.smslocator;

import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsLocatorService extends Service implements LocationListener {

	public static final String INTENT_DESTINATION_NUMBER = "intentdestinationnumber";

	private static final String LOGNAME = SmsLocatorService.class.getCanonicalName();
	
	private LocationManager lm;

	private String destination;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		destination = intent.getStringExtra(INTENT_DESTINATION_NUMBER);
		
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		if(lm.getProvider(LocationManager.GPS_PROVIDER) != null){
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
		}

	
	}

	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		lm.removeUpdates(this);
	}

	
	private String getStreetName(Location location){
		
		// Get the street address
		Geocoder geocoder = new Geocoder(this);
		
		String addressName = null;
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(addresses != null && addresses.size() > 0){
				Address address = addresses.get(0);
				StringBuffer sb = new StringBuffer();
				sb.append( (address.getAddressLine(0)!= null)?address.getAddressLine(0):"");
				sb.append( (address.getLocality()!= null)? " "+address.getLocality():"");
				sb.append( (address.getCountryCode()!= null)? " "+address.getCountryCode():"");
				addressName = sb.toString(); 
			}
			
		} catch (IOException e) {
			Log.e(LOGNAME,"Unable to geocode:"+e.getMessage());
		}
		
		return addressName; 
	}
	
	private String getShortMapsUrl(Location location){
		
		StringBuffer sb = new StringBuffer(); 
		sb.append("http://mapof.it/");
		sb.append(location.getLatitude());
		sb.append(",");
		sb.append(location.getLongitude());
		
		return sb.toString(); 
	}
	
	private void sendSms(String message){
		SmsManager sm = SmsManager.getDefault();
		sm.sendTextMessage(destination, null, message, null, null);
		
		Log.i(LOGNAME, "Sending '"+message+"' to "+destination);
	}
	
	
	public void onLocationChanged(Location location) {
		
		String address = getStreetName(location);
		
		String url = getShortMapsUrl(location);
		
		String message = "Your phone is located near:"+address+" "+url; 
		
		sendSms(message);
		
		stopSelf();
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}
}
