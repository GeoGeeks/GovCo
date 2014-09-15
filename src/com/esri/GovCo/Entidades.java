package com.esri.GovCo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Entidades extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entidades);
	}
	
	public void irIgac(View view){
		Intent i = new Intent(this, GovCoActivity.class);
        i.putExtra("lugar", "IGAC");
        startActivity(i);
	}
	
	public void irDane(View view){
		Intent i = new Intent(this, GovCoActivity.class);
        i.putExtra("lugar", "DANE");
        startActivity(i);
	}
	
	public void irIcde(View view){
		Intent i = new Intent(this, GovCoActivity.class);
        i.putExtra("lugar", "ICDE");
        startActivity(i);
	}
	
	public void irFederacion(View view){
		Intent i = new Intent(this, GovCoActivity.class);
        i.putExtra("lugar", "Federacion");
        startActivity(i);
	}
	
	public void irSalud(View view){
		Intent i = new Intent(this, GovCoActivity.class);
        i.putExtra("lugar", "Min_Salud");
        startActivity(i);
	}
	
	public void irGeologico(View view){
		Intent i = new Intent(this, GovCoActivity.class);
        i.putExtra("lugar", "Geologico");
        startActivity(i);
	}
	
	public void VerMapa(View view){
		Intent cambio =new Intent(this, GovCoActivity.class);
        startActivity(cambio);
	}
}
