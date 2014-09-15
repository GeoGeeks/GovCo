package com.esri.GovCo;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.os.Build;

public class Configuracion extends Activity{
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuracion);
		
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
		if(PantallaInicial.isActiveGeotrigger())
			checkBox.setChecked(true);
		else
			checkBox.setChecked(false);
		checkBox.setOnCheckedChangeListener(
				new CheckBox.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

						if (isChecked) {
							PantallaInicial.setActiveGeotrigger(true);
						}
						else {
							PantallaInicial.setActiveGeotrigger(false);
						}
					}
				});

	}
	
	public void VolverPantallaInicio(View view){
		Intent cambio =new Intent(this, PantallaInicial.class);
        startActivity(cambio);
	}
	
	public void VerAyuda(View view){
		Intent cambio =new Intent(this, Ayuda.class);
        startActivity(cambio);
	}
}
