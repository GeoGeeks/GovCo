package com.esri.GovCo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;


public class Ayuda extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ayuda);
		TextView texto=(TextView)findViewById(R.id.textView2);
		texto.setMovementMethod(new ScrollingMovementMethod());
		texto.setTextColor(Color.WHITE);

	}
	
	public void VolverPantallaInicio(View view){
		Intent cambio =new Intent(this, PantallaInicial.class);
        startActivity(cambio);
	}

}
