package es.rosich.voclist;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.KeyEvent;
import android.graphics.Color;
import android.util.Log;
import java.util.Locale;
import java.io.IOException;

public class pregunta extends Activity {

    private Voclist vl;
    TextToSpeech tts;
    TextView respuesta, info;
    ImageButton repite;
    Button ok, siguiente;
    String palabra;
    boolean empezando=true;
    boolean salir=false;
    String nombre;
    String lista;

    private void terminar() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        this.finish();
    }

    public void entradaNO() {
        ok.setEnabled(false);
        ok.setAlpha((float) 0.0);
        repite.setEnabled(false);
        repite.setAlpha((float) 0.0);
    }

    public void entradaSI() {
        siguiente.setText("Siguiente");
        siguiente.setEnabled(false);
        ok.setEnabled(true);
        ok.setAlpha((float) 1.0);
        repite.setEnabled(true);
        repite.setAlpha((float) 1.0);
        repite.setBackgroundColor(Color.GREEN);
        respuesta.setText("");
        respuesta.setEnabled(true);
        respuesta.requestFocus();
        info.setText("Escucha y escribe");
        tts.speak(vl.pronuncia(palabra), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void presenta(String saludo, String informe, String boton) {
        empezando = true;
        entradaNO();
        respuesta.setText(saludo);
        info.setText(informe);
        siguiente.setText(boton);
        siguiente.setEnabled(true);
        siguiente.setBackgroundColor(Color.YELLOW);
    }

    public void newVL() {
        try { vl=new Voclist(nombre,lista,getApplicationContext());
        } catch (Exception e) { terminar(); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregunta);
        respuesta =    (TextView) findViewById(R.id.txtRespuesta);
        info      =    (TextView) findViewById(R.id.txtInfo);
        repite    = (ImageButton) findViewById(R.id.botonRepite);
        ok        =      (Button) findViewById(R.id.botonOK);
        siguiente =      (Button) findViewById(R.id.botonSiguiente);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        ok.setBackgroundColor(Color.GREEN);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(new Locale("es", "ES"));
                }else{
                    Log.i("tts","Algo va mal");
                    terminar();
                }
            }
        });

        respuesta.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Log.i("INFO",""+"IME_ACTION_DONE="+EditorInfo.IME_ACTION_DONE);
                //Log.i("INFO",""+"ACTION_UP="+KeyEvent.ACTION_UP);
                //Log.i("INFO",""+"ACTION_DOWN="+KeyEvent.ACTION_DOWN);
                //Log.i("actionId",""+actionId);
                //Log.i("getAction",""+event.getAction());
                //Log.i("getKeyCode", "" + event.getKeyCode());

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    califica(v);
                }
                else if ((actionId == 0) && event.getAction() == KeyEvent.ACTION_UP){
                    califica(v);
                }

                return (true);
            }
        });

        Bundle bundle = getIntent().getExtras();
        nombre=bundle.getString("nombre");
        lista=bundle.getString("lista");

        newVL();
        presenta("Hola, " + nombre + "!",
                "Por ahora sabes " + vl.numsabidas() + " palabras.",
                "EMPEZAR");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pregunta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) { return true; }
        return super.onOptionsItemSelected(item);
    }

    public void califica(View v) {
        if (ok.isEnabled()) {
            // Calificar palabra introducida
            info.setText(vl.califica(palabra, respuesta.getText().toString()));
            entradaNO();
        }else{
            // Palabra ya calificada. Pasar a otra, salvo empezando
            // (previene doble pulsacion al final de la partida).
            if (! empezando) siguiente(v);
        }
    }

    public void siguiente(View v) {

        if (salir) this.finish();

        if (empezando) {
            empezando=false;
            //siguiente.setBackgroundColor(Color.CYAN);
            siguiente.setAlpha((float) 0.0);
            siguiente.setEnabled(false);
        }

        if ((palabra=vl.nuevaPal())!=null)
            entradaSI();
        else { // hemos acabado; preparar otra partida
            try {
                presenta("Hemos terminado!", vl.finmsg(), "VOLVER");
                //this.finish();
                // newVL();
                salir=true;
                siguiente.setAlpha((float)1.0);
            }
            catch (IOException e) {terminar();}
        }
    }

    public void repite(View v) {
        entradaSI();
    }

}
