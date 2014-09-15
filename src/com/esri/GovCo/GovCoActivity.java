package com.esri.GovCo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.geotrigger.GeotriggerBroadcastReceiver;
import com.esri.android.geotrigger.GeotriggerService;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

public class GovCoActivity extends Activity{
	
	private PopupContainer popupContainer;
    private PopupDialog popupDialog;
    private ProgressDialog progressDialog;
    private AtomicInteger count;
	
	MapView mMapView;
	GraphicsLayer locationLayer;
	
	Locator locator;
	// create UI components
	static ProgressDialog dialog;
	static Handler handler;
	LocationService ls;
	
	Point mLocation = null;
	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);
	
	// Label instructing input for EditText
	TextView geocodeLabel;
	// Text box for entering address
	EditText addressText;
	LinearLayout entidades;
	ImageView mostrarEntidades;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ArcGISRuntime.setClientId("Y9y7uBLGOsvfIfLN");
        
        locationLayer = new GraphicsLayer();
        // create handler to update the UI
        handler = new Handler();
        
        // Set the geocodeLabel with instructions
     	geocodeLabel = (TextView) findViewById(R.id.geocodeLabel);
     	geocodeLabel.setText(getString(R.string.geocode_label));
     	// Get the addressText component
     	addressText = (EditText) findViewById(R.id.addressText);
        
	    // Retrieve the map and initial extent from XML layout
	    mMapView = (MapView)findViewById(R.id.map);
	    // attribute ESRI logo to map
		mMapView.setEsriLogoVisible(true);
	    // enable map to wrap around date line
		mMapView.enableWrapAround(true);
	    // Add dynamic layer to MapView
		//mMapView.setMapOptions(new MapOptions(MapType.OSM));
	   /* mMapView.addLayer(new ArcGISTiledMapServiceLayer("" +
	    		getString(R.string.mapserver_url)));*/
	    //Para cargar un feature layer
	    //(new ArcGISFeatureLayer(getString(R.string.basemap_url), MODE.ONDEMAND));
	    ls = mMapView.getLocationService();
	    entidades = (LinearLayout) findViewById(R.id.listaEntidades);
	    entidades.setVisibility(View.INVISIBLE);
	    
	    mostrarEntidades = (ImageView) findViewById(R.id.imageView8);
	 // Tap on the map and show popups for selected features.
	    mMapView.setOnSingleTapListener(new OnSingleTapListener() {
	      private static final long serialVersionUID = 1L;

	      public void onSingleTap(float x, float y) {       
	        if (mMapView.isLoaded()) {
	            // Instantiate a PopupContainer
	            popupContainer = new PopupContainer(mMapView);
	            int id = popupContainer.hashCode();
	            popupDialog = null;
	            // Display spinner.
	            if (progressDialog == null || !progressDialog.isShowing())
	                progressDialog = ProgressDialog.show(mMapView.getContext(), "", "Querying...");

	            // Loop through each layer in the webmap
	            int tolerance = 20;
	                    Envelope env = new Envelope(mMapView.toMapPoint(x, y), 20 * mMapView.getResolution(), 20 * mMapView.getResolution());
	            Layer[] layers = mMapView.getLayers();
	            count = new AtomicInteger();
	            for (Layer layer : layers) {
	                // If the layer has not been initialized or is invisible, do nothing.
	                if (!layer.isInitialized() || !layer.isVisible())
	                    continue;

	                if (layer instanceof ArcGISFeatureLayer) { 
	                    // Query feature layer and display popups
	                    ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) layer;                       
	                    if (featureLayer.getPopupInfo() != null) {
	                        // Query feature layer which is associated with a popup definition.
	                        count.incrementAndGet();
	                        new RunQueryFeatureLayerTask(x, y, tolerance, id).execute(featureLayer);
	                    }
	                }
	                else if (layer instanceof ArcGISDynamicMapServiceLayer) { 
	                    // Query dynamic map service layer and display popups.
	                    ArcGISDynamicMapServiceLayer dynamicLayer = (ArcGISDynamicMapServiceLayer) layer;
	                    // Retrieve layer info for each sub-layer of the dynamic map service layer.
	                    ArcGISLayerInfo[] layerinfos = dynamicLayer.getAllLayers();
	                    if (layerinfos == null)
	                        continue;

	                    // Loop through each sub-layer
	                    for (ArcGISLayerInfo layerInfo : layerinfos) {
	                        // Obtain PopupInfo for sub-layer.
	                        PopupInfo popupInfo = dynamicLayer.getPopupInfo(layerInfo.getId());
	                        // Skip sub-layer which is without a popup definition.
	                        if (popupInfo == null) {
	                            continue;
	                        }
	                        // Check if a sub-layer is visible.
	                        ArcGISLayerInfo info = layerInfo;
	                        while ( info != null && info.isVisible() ) {
	                            info = info.getParentLayer();
	                        }
	                        // Skip invisible sub-layer
	                        if ( info != null && ! info.isVisible() ) {
	                            continue;
	                        };

	                        // Check if the sub-layer is within the scale range
	                        double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo.getMaxScale():popupInfo.getMaxScale();
	                        double minScale = (layerInfo.getMinScale() != 0) ? layerInfo.getMinScale():popupInfo.getMinScale();

	                        if ((maxScale == 0 || mMapView.getScale() > maxScale) && (minScale == 0 || mMapView.getScale() < minScale)) {
	                            // Query sub-layer which is associated with a popup definition and is visible and in scale range.
	                            count.incrementAndGet();
	                            new RunQueryDynamicLayerTask(env, layer, layerInfo.getId(), dynamicLayer.getSpatialReference(), id).execute(dynamicLayer.getUrl() + "/" + layerInfo.getId());
	                        }
	                    }
	                }               
	            }
	        }
	      }
	    });

	    if(getIntent().getExtras()!=null){
	    	// the right way to get map resolution
	        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
	          private static final long serialVersionUID = 1L;
	    
	          public void onStatusChanged(Object source, STATUS status) {
	            if (OnStatusChangedListener.STATUS.INITIALIZED == status && source == mMapView) {
	            	Bundle bundle = getIntent().getExtras();
	            	IrDireccion(bundle.getString("lugar"));
	            }
	          }
	        });
        }
    }
    
    /*
	 * Dismiss dialog when geocode task completes
	 */
    static public class MyRunnable implements Runnable {
		public void run() {
			dialog.dismiss();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
 }

	
	/*
	 * AsyncTask to geocode an address to a point location Draw resulting point
	 * location on the map with matching address
	 */
	private class Geocoder extends
			AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

		ProgressDialog pd;
		@Override
		protected List<LocatorGeocodeResult> doInBackground(
				LocatorFindParameters... params) {
			// create results object and set to null
			List<LocatorGeocodeResult> results = null;
			// set the geocode service

			locator = Locator.createOnlineLocator(getResources()
								.getString(R.string.geocode_url));
			try {
				// pass address to find method to return point representing
				// address
				results = locator.find(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// return the resulting point(s)
			return results;
		}
		
		@Override
	    protected void onPreExecute() {
	            pd = new ProgressDialog(GovCoActivity.this);
	            pd.setTitle("Buscando dirección...");
	            pd.setMessage("Un momento por favor.");
	            pd.setCancelable(false);
	            pd.setIndeterminate(true);
	            pd.show();
	    }
		
		
		// The result of geocode task is passed as a parameter to map the
		// results
		protected void onPostExecute(List<LocatorGeocodeResult> result) {
			mMapView.addLayer(locationLayer);
			if (result == null || result.size() == 0) {
				// update UI with notice that no results were found
				Toast toast = Toast.makeText(GovCoActivity.this,
						"No result found.", Toast.LENGTH_LONG);
				toast.show();
			} else{
				dialog = ProgressDialog.show(mMapView.getContext(), "Geocoder",
						"Searching for address ...");
				// get return geometry from geocode result
				Geometry resultLocGeom = result.get(0).getLocation();
				// create marker symbol to represent location
				SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(
						Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
				// create graphic object for resulting location
				Graphic resultLocation = new Graphic(resultLocGeom,
						resultSymbol);
				// add graphic to location layer
				locationLayer.addGraphic(resultLocation);
				// create text symbol for return address
				TextSymbol resultAddress = new TextSymbol(12, result.get(0)
						.getAddress(), Color.BLACK);
				// create offset for text
				resultAddress.setOffsetX(10);
				resultAddress.setOffsetY(50);
				// create a graphic object for address text
				Graphic resultText = new Graphic(resultLocGeom, resultAddress);
				// add address text graphic to location graphics layer
				locationLayer.addGraphic(resultText);
				// zoom to geocode result
				mMapView.zoomToResolution(result.get(0).getLocation(), 2);
				// create a runnable to be added to message queue
				handler.post(new MyRunnable());
				if (pd!=null) {
                    pd.dismiss();
				}
			}
		}
		
	}
	
	
	private void setSearchParams(String address) {	
		try {
			// create Locator parameters from single line address string
			LocatorFindParameters findParams = new LocatorFindParameters(address);
			// set the search country to USA
			findParams.setSourceCountry("USA");
			// limit the results to 2
			findParams.setMaxLocations(2);
			// set address spatial reference to match map
			findParams.setOutSR(mMapView.getSpatialReference());
			// execute async task to geocode address
			new Geocoder().execute(findParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * Submit address for my location
	 */
	public void mylocate(View view) {
		//String address = addressText.getText().toString();
		ls.stop();
		ls.start();
		mMapView.zoomToResolution(ls.getPoint(), 2);
		ls.pause();
	}
	
	/*
	 * Submit address for place search
	 */
	public void locate(View view) {
		// remove any previous graphics
		locationLayer.removeAll();
		// obtain address from text box
		String address = addressText.getText().toString();
		// set parameters to support the find operation for a geocoding service
		setSearchParams(address);
	}

	
	public void menuEntidades(View view){
		
		entidades.setVisibility(View.VISIBLE);
		mostrarEntidades.setVisibility(View.INVISIBLE);
		
		
	}
	
	public void cerrarMenuEntidades(View view){
		
		entidades.setVisibility(View.INVISIBLE);
		mostrarEntidades.setVisibility(View.VISIBLE);
		
	}
	
	public void IrDireccion(String direccion) {
	     if(direccion.equals("ICDE")){
	    	  mLocation = new Point(-74.080008,4.637781);
	     }else if(direccion.equals("IGAC")){
	    	 mLocation = new Point(-74.079865,4.639632);
	     }else if(direccion.equals("Federacion")){
	    	 mLocation = new Point(-74.055703,4.656809);
	     }else if(direccion.equals("Min_Salud")){
	    	 mLocation = new Point(-74.068333,4.619361);
	     }else if(direccion.equals("Geologico")){
	    	 mLocation = new Point(-74.080167,4.641249);
	     }else if(direccion.equals("DANE")){
	    	 mLocation = new Point(-74.096536,4.647066);
	     }
	     cerrarMenuEntidades(null);
	     Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
	     mMapView.zoomToResolution(p, 0.5);
	}
	
	public void irIcde(View view){
		
		String direccion;
		direccion = "ICDE";
		IrDireccion(direccion);
	}
	
	public void irIgac(View view){
		
		String direccion;
		direccion = "IGAC";
		IrDireccion(direccion);
	}
	
	public void irDane(View view){
		
		String direccion;
		direccion = "DANE";
		IrDireccion(direccion);
	}
	
	public void irFederacion(View view){
		
		String direccion;
		direccion = "Federacion";
		IrDireccion(direccion);
	}
	
	public void irMinSalud(View view){
		
		String direccion;
		direccion = "Min_Salud";
		IrDireccion(direccion);
	}
	
	public void irGeologico(View view){
		
		String direccion;
		direccion = "Geologico";
		IrDireccion(direccion);
	}

	private void createPopupViews(Graphic[] graphics, final int id) {
		if ( id != popupContainer.hashCode() ) {
			if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
				progressDialog.dismiss();

			return;
		}

		if (popupDialog == null) {
			if (progressDialog != null && progressDialog.isShowing()) 
				progressDialog.dismiss();
			// Create a dialog for the popups and display it.
			popupDialog = new PopupDialog(mMapView.getContext(), popupContainer);
			//popupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			popupDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			popupDialog.setCanceledOnTouchOutside(true);
			popupDialog.show();
		}
  }
  
  // Query feature layer by hit test
  private class RunQueryFeatureLayerTask extends AsyncTask<ArcGISFeatureLayer, Void, Graphic[]> {

		private int tolerance;
		private float x;
		private float y;
		private ArcGISFeatureLayer featureLayer;
		private int id;

		public RunQueryFeatureLayerTask(float x, float y, int tolerance, int id) {
			super();
			this.x = x;
			this.y = y;
			this.tolerance = tolerance;
			this.id = id;
		}

		@Override
		protected Graphic[] doInBackground(ArcGISFeatureLayer... params) {
			for (ArcGISFeatureLayer featureLayer : params) {
				this.featureLayer = featureLayer;
				// Retrieve graphic ids near the point.
				int[] ids = featureLayer.getGraphicIDs(x, y, tolerance);
				if (ids != null && ids.length > 0) {
					ArrayList<Graphic> graphics = new ArrayList<Graphic>();
					for (int id : ids) {
						// Obtain graphic based on the id.
						Graphic g = featureLayer.getGraphic(id);
						if (g == null)
							continue;
						graphics.add(g);
					}
					// Return an array of graphics near the point.
					return graphics.toArray(new Graphic[0]);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Graphic[] graphics) {
			count.decrementAndGet();
			if (graphics == null || graphics.length == 0) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();
				
				return;
			}
			// Check if the requested PopupContainer id is the same as the current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if ( id != popupContainer.hashCode() ) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();
				
				return;
			}

			for (Graphic gr : graphics) {
				Popup popup = featureLayer.createPopup(mMapView, 0, gr);
				popupContainer.addPopup(popup);
			}
			createPopupViews(graphics, id);
		}

	}
// Query dynamic map service layer by QueryTask
  private class RunQueryDynamicLayerTask extends AsyncTask<String, Void, FeatureSet> {
		private Envelope env;
		private SpatialReference sr;
		private int id;
		private Layer layer;
		private int subLayerId;

		public RunQueryDynamicLayerTask(Envelope env, Layer layer, int subLayerId, SpatialReference sr, int id) {
			super();
			this.env = env;
			this.sr = sr;
			this.id = id;
			this.layer = layer;
			this.subLayerId = subLayerId;
		}

		@Override
		protected FeatureSet doInBackground(String... urls) {
			for (String url : urls) {
				// Retrieve graphics within the envelope.
				Query query = new Query();
				query.setInSpatialReference(sr);
				query.setOutSpatialReference(sr);
				query.setGeometry(env);
				query.setMaxFeatures(10);
				query.setOutFields(new String[] { "*" });

				QueryTask queryTask = new QueryTask(url);
				try {
					FeatureSet results = queryTask.execute(query);
					return results;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final FeatureSet result) {
			count.decrementAndGet();
			if (result == null) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();

				return;
			}
			Graphic[] graphics = result.getGraphics();
			if (graphics == null || graphics.length == 0) {
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();

				return;
			}
			// Check if the requested PopupContainer id is the same as the current PopupContainer.
			// Otherwise, abandon the obsoleted query result.
			if (id != popupContainer.hashCode()) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0) 
					progressDialog.dismiss();

				return;
			}
			PopupInfo popupInfo = layer.getPopupInfo(subLayerId);
			if (popupInfo == null) {
				// Dismiss spinner
				if (progressDialog != null && progressDialog.isShowing() && count.intValue() == 0)  
					progressDialog.dismiss();

				return;
			}
			
			for (Graphic gr : graphics) {
				Popup popup = layer.createPopup(mMapView, subLayerId, gr);
				popupContainer.addPopup(popup);
			}
			createPopupViews(graphics, id);
			
		}
	}


// A customize full screen dialog.
	  private class PopupDialog extends AlertDialog{
		  private PopupContainer popupContainer;
		  
		  public PopupDialog(Context context, PopupContainer popupContainer) {
			  super(context, android.R.style.Theme);
			  this.popupContainer = popupContainer;
			  
		  }
		  
		  @Override
		  protected void onCreate(Bundle savedInstanceState) {
			  super.onCreate(savedInstanceState);

			  LayoutParams params = new LayoutParams(600,600);
				LinearLayout layout = new LinearLayout(getContext());
				layout.setBackgroundColor(Color.WHITE);
				layout.setOrientation(LinearLayout.VERTICAL);
				Button bt = new Button(layout.getContext());
				bt.setText("Cerrar");
				bt.setLayoutParams(new LayoutParams(70,30));
				bt.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						popupDialog.dismiss();
					}
				});
				layout.addView(popupContainer.getPopupContainerView(), android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 500);
				layout.addView(bt,600,100);
				setContentView(layout, params);
			}
		  
	  }
	
}