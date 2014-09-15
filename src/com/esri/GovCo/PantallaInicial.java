package com.esri.GovCo;


import com.esri.android.geotrigger.GeotriggerBroadcastReceiver;
import com.esri.android.geotrigger.GeotriggerService;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class PantallaInicial extends Activity implements
GeotriggerBroadcastReceiver.LocationUpdateListener,
GeotriggerBroadcastReceiver.ReadyListener{
	
	static ProgressDialog dialog;
	
	static boolean isActiveGeotrigger = true;

	private static final String TAG = "GovCo";
    // Create a new application at https://developers.arcgis.com/en/applications
    private static final String AGO_CLIENT_ID = "Y9y7uBLGOsvfIfLN";

    // The project number from https://cloud.google.com/console
    private static final String GCM_SENDER_ID = "234748359227";

    // A list of initial tags to apply to the device.
    // Triggers created on the server for this application, with at least one of these same tags,
    // will be active for the device.
    private static final String[] TAGS = new String[] {"tag 1", "tag 2"};

    // The GeotriggerBroadcastReceiver receives intents from the
    // GeotriggerService, calling any listeners implemented in your class.
    private GeotriggerBroadcastReceiver mGeotriggerBroadcastReceiver;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pantalla_inicial);
		
		mGeotriggerBroadcastReceiver = new GeotriggerBroadcastReceiver();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//mMapView.unpause();
		registerReceiver(mGeotriggerBroadcastReceiver,
	                GeotriggerBroadcastReceiver.getDefaultIntentFilter()); 
	}
	
	 @Override
    public void onStart() {
        super.onStart();
        if(isActiveGeotrigger){
        	GeotriggerHelper.startGeotriggerService(this, AGO_CLIENT_ID, GCM_SENDER_ID, TAGS,
                GeotriggerService.TRACKING_PROFILE_ADAPTIVE);
	        GeotriggerService.setPushSound(this,"sound");
	        GeotriggerService.setPushIcon(this,"GovCo");
	        GeotriggerService.setVibrationEnabled(this,true);
	        Toast.makeText(this, "Servicio de Geotrigger Encendido", Toast.LENGTH_SHORT).show();
        }else{
        	GeotriggerService.stop(this);
        	Toast.makeText(this, "Servicio de Geotrigger Apagado", Toast.LENGTH_SHORT).show();
        }
    }
	
 	@Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver. Activity will no longer respond to
        // GeotriggerService intents. Tracking and push notification handling
        // will continue in the background.
        unregisterReceiver(mGeotriggerBroadcastReceiver);
    }
 	
 	
 	
 	  @Override
 	  public void onReady() {
 	        // Called when the device has registered with ArcGIS Online and is ready
 	        // to make requests to the Geotrigger Service API.
 	      if(isActiveGeotrigger)
 	    	  Toast.makeText(this, "Servicio de Geotrigger Listo!", Toast.LENGTH_SHORT).show();
 	      else
 	    	 Toast.makeText(this, "GeotriggerService Desactivado!", Toast.LENGTH_SHORT).show();
 	      Log.d(TAG, "GeotriggerService ready!");
 	  }

	@Override
	public void onLocationUpdate(Location loc, boolean arg1) {
		// Called with the GeotriggerService obtains a new location update from
        // Android's native location services. The isOnDemand parameter lets you
        // determine if this location update was a result of calling
        // GeotriggerService.requestOnDemandUpdate()
        Toast.makeText(this, "Se actualizó la localización!",
                Toast.LENGTH_SHORT).show(); 
        Log.d(TAG, String.format("Location update received: (%f, %f)",
                loc.getLatitude(), loc.getLongitude()));
        				
	}
	
	
	public void VerMapa(View view){
		registerReceiver(mGeotriggerBroadcastReceiver,
                GeotriggerBroadcastReceiver.getDefaultIntentFilter());
		Intent cambio =new Intent(this, GovCoActivity.class);
        startActivity(cambio);
	}
	
	public void VerEntidades(View view){
		Intent cambio =new Intent(this, Entidades.class);
        startActivity(cambio);
	}
	
	public void VerConfiguracion(View view){
		Intent cambio =new Intent(this, Configuracion.class);
        startActivity(cambio);
	}
	
	/*
	 * Dismiss dialog when geocode task completes
	 */
	static public class MyRunnable implements Runnable {
		public void run() {
			dialog.dismiss();
		}
	}

	public static boolean isActiveGeotrigger() {
		return isActiveGeotrigger;
	}
	
	public static void setActiveGeotrigger(boolean bool) {
		isActiveGeotrigger=bool;
	}
}
