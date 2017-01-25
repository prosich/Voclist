package es.rosich.voclist;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.KeyEvent;
import android.graphics.Color;
import android.util.Log;
import java.util.Locale;
import java.io.IOException;

import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;

public class pregunta extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private Voclist vl;
    TextToSpeech tts;
    TextView respuesta, info, ftxt1, ftxt2;
    ImageButton repite;
    Button siguiente;
    Button nosoy;
    ProgressBar pcbar;
    String palabra;
    boolean empezando=true;
    boolean salir=false;
    String nombre;
    String lista="ortografia";

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.d(TAG, "LOGEADO");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "NOOOOO LOGEADO");
                //signIn();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(pregunta.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signIn() {
        Log.d(TAG, "SIGNIN");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void volver(View v) {
        Log.d(TAG, "VOLVER");
        signOut();
        signIn();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //signIn();
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();
        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        signIn();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
    }

    private void terminar() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        this.finish();
    }

    public void entradaNO() {
        repite.setEnabled(false);
        repite.setBackgroundColor(Color.LTGRAY);
        siguiente.setEnabled(true);
        siguiente.setVisibility(View.VISIBLE);
        //respuesta.setEnabled(false);
    }

    public void entradaSI() {
        repite.setEnabled(true);
        repite.setBackgroundColor(Color.GREEN);
        siguiente.setText("Otra palabra");
        siguiente.setEnabled(false);
        siguiente.setVisibility(View.INVISIBLE);
        respuesta.setText("");
        respuesta.setEnabled(true);
        respuesta.setVisibility(View.VISIBLE);
        respuesta.requestFocus();
        info.setText("Escucha y escribe");
        tts.speak(vl.pronuncia(palabra), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void presenta(String saludo, String informe, String boton) {
        empezando = true;
        entradaNO();
        info.setText(saludo+"\n"+informe);
        siguiente.setText(boton);
        siguiente.setEnabled(true);
        nosoy.setText("No soy "+nombre);
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
        siguiente =      (Button) findViewById(R.id.botonSiguiente);
        nosoy     =      (Button) findViewById(R.id.botonNosoy);
        pcbar     = (ProgressBar) findViewById(R.id.pcPartida);
        ftxt1     =    (TextView) findViewById(R.id.ftxt1);
        ftxt2     =    (TextView) findViewById(R.id.ftxt2);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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

        newVL();
        pcbar.setProgress(vl.pcGlobal());
        ftxt1.setText(vl.fGlobal());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this )
                .addApi(Drive.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
        */

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    Log.d(TAG, "onAuthStateChanged()");
                    hideProgressDialog();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        nombre=user.getDisplayName();
                            presenta("Hola, " + nombre + "!",
                                    "Por ahora sabes " + vl.numsabidas() + " palabras.",
                                    "Vale, empecemos");
                    } else {
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                    }
                }
        };

        signIn();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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
        Log.i("CALIFICANDO","ando");
        if (repite.isEnabled()) {
            // Calificar palabra introducida
            info.setText(vl.califica(palabra, respuesta.getText().toString()));
            pcbar.setProgress(vl.pcPartida());
            ftxt1.setText(vl.f2Partida() + " " + vl.fPartida());
            //ftxt2.setText("hola"+vl.f2Partida());
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
            pcbar.setProgress(0);
            ftxt1.setText(vl.f2Partida()+" "+vl.fPartida());
            siguiente.setVisibility(View.INVISIBLE);
            siguiente.setEnabled(false);
            nosoy.setVisibility(View.INVISIBLE);
            nosoy.setEnabled(false);
        }

        if ((palabra=vl.nuevaPal())!=null)
            entradaSI();
        else { // hemos acabado; preparar otra partida
            try {
                presenta("Guardado", vl.finmsg(), "VOLVER");
                //this.finish();
                // newVL();
                salir=true;
                //siguiente.setAlpha((float)1.0);
                //siguiente.setVisibility(View.VISIBLE);
            }
            catch (IOException e) {terminar();}
        }
    }

    public void repite(View v) {
        entradaSI();
    }


}

