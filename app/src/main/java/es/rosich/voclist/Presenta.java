package es.rosich.voclist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.ArrayList;

public class Presenta extends Activity {

    EditText txt_nombre;
    Spinner spinner1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presenta);
        addListenerOnButton();
    }

    public void empieza() {
        String nombre=txt_nombre.getText().toString();
        if (nombre.equals("")) return;

        String lista=String.valueOf(spinner1.getSelectedItem());
        Intent i = new Intent(getBaseContext(), pregunta.class);
        i.putExtra("nombre",nombre);
        i.putExtra("lista",lista);
        startActivity(i);
    }


    public void addListenerOnButton() {
        Button b=(Button)findViewById(R.id.start_boton);

        spinner1=(Spinner) findViewById(R.id.lista_listas);
        ArrayList lista = new ArrayList<>();
        lista.add("ortografia");
        lista.add("ingles");
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lista);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adaptador);

        txt_nombre=(EditText)findViewById(R.id.intro_nombre);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                empieza();
            }
        });
    }

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
