package es.rosich.voclist;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;


public class Presenta extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presenta);
        addListenerOnButton();
    }

    public void addListenerOnButton() {
        Button b=(Button)findViewById(R.id.start_boton);

        final Spinner spinner1 = (Spinner) findViewById(R.id.lista_listas);
        ArrayList lista = new ArrayList<String>();
        lista.add("ortografia");
        lista.add("ingles");
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lista);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adaptador);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TextView txt_nombre=(TextView)findViewById(R.id.intro_nombre);
                String nombre=txt_nombre.getText().toString();
                String lista=String.valueOf(spinner1.getSelectedItem());
                Intent i = new Intent(getBaseContext(), pregunta.class);
                i.putExtra("nombre",nombre);
                i.putExtra("lista",lista);
                startActivity(i);
            }
        });
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_presenta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) return true;
        return super.onOptionsItemSelected(item);
    }
}
