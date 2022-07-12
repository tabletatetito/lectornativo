package com.yjulianhc.lectornativo2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tdocumento;
    private TextView tprimer_nombre;
    private TextView tsegundo_nombre;
    private TextView tprimer_apellido;
    private TextView tsegundo_apellido;
    private TextView tfecha_nacimiento;
    private TextView tsexo;
    private EditText direccion;
    private EditText telefono;
    private TextView tlongitud;
    private TextView tlatitud;
    //private TextView taforo;
    public String token = null;

    private Button escanear;
    private Button registrar;
    //private Button reiniciar;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    public static String URL = "https://www.gestionperfecta.com/covid/gimnasio/covidserver/public/api/";
    private GPSTracker gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.INTERNET,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                1);



        tlongitud=(TextView) findViewById(R.id.value_longitud);
        tlatitud=(TextView) findViewById(R.id.value_latitud);

        /*FragmentManager fragmentManager = getSupportFragmentManager();
        FullscreenDialogFragment newFragment = new FullscreenDialogFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();*/


        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if(sharedPref.getString("token",null)==null){
            final Dialog customDialog = new Dialog(this, R.style.AppTheme);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setCancelable(false);
            customDialog.setContentView(R.layout.dialog_signin);
            final EditText correo=(EditText) customDialog.findViewById(R.id.correo);
            final EditText contrasena=(EditText) customDialog.findViewById(R.id.contrasena);
            final RadioGroup grupo = (RadioGroup) customDialog.findViewById(R.id.radiogroup);

            final View.OnClickListener seleccionar = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   editor=sharedPref.edit();
                   editor.putString("token", token);
                   editor.putString("dispositivo", v.getTag().toString());
                   editor.commit();
                   customDialog.dismiss();
                }
            };

            final Button entrar=(Button) customDialog.findViewById(R.id.entrar);

            entrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    grupo.removeAllViews();
                                    entrar.setText("Cargando...");
                                    entrar.setEnabled(false);
                                }
                            });

                            RequestQueue requstQueue = Volley.newRequestQueue(MainActivity.this);
                            HashMap data=new HashMap();
                            data.put("email",correo.getText().toString());
                            data.put("password",contrasena.getText().toString());

                            JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, URL+"logindevice",new JSONObject(data),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                Log.e("RESPONSE",response.toString());
                                                token=response.getString("token");
                                                JSONArray dispositivos=response.getJSONArray("dispositivos");
                                                for(int i=0;i<dispositivos.length();i++){
                                                    RadioButton rdbtn = new RadioButton(MainActivity.this);
                                                    rdbtn.setId(View.generateViewId());
                                                    rdbtn.setTag(dispositivos.getJSONObject(i).getString("id"));
                                                    rdbtn.setText(dispositivos.getJSONObject(i).getString("nombre"));
                                                    rdbtn.setOnClickListener(seleccionar);
                                                    grupo.addView(rdbtn);
                                                }
                                            }catch (Exception e){
                                                Log.e("ERROR",e.toString());
                                            }

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    entrar.setText("INICIAR SESIÓN");
                                                    entrar.setEnabled(true);
                                                }
                                            });
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    entrar.setText("INICIAR SESIÓN");
                                                    entrar.setEnabled(true);
                                                    Toast.makeText(getApplicationContext(), "Usuario o contraseña invalidos", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                            ){
                                //here I want to post data to sever
                            };
                            requstQueue.add(jsonobj);

                        }
                    };

                    Thread h = new Thread(r);
                    h.start();
                }
            });
            customDialog.show();

        }

        /*editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
        editor.commit();*/

        tdocumento=(TextView) findViewById(R.id.value_documento);
        tprimer_nombre=(TextView) findViewById(R.id.value_primer_nombre);
        tsegundo_nombre=(TextView) findViewById(R.id.value_segundo_nombre);
        tprimer_apellido=(TextView) findViewById(R.id.value_primer_apellido);
        tsegundo_apellido=(TextView) findViewById(R.id.value_segundo_apellido);
        tfecha_nacimiento=(TextView) findViewById(R.id.value_fecha_nacimiento);
        tsexo=(TextView) findViewById(R.id.value_sexo);
        direccion=(EditText)findViewById(R.id.value_direccion);
        telefono=(EditText) findViewById(R.id.value_telefono);
        /*taforo=(TextView) findViewById(R.id.value_aforo);
        taforo.setText(sharedPref.getInt("aforo",0)+"");
        reiniciar=(Button) findViewById(R.id.reset);
        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editor = sharedPref.edit();
                        editor.putInt("aforo",0);
                        editor.commit();
                        taforo.setText("0");
                    }
                });

            }
        });*/

        escanear=(Button)findViewById(R.id.escanear);
        registrar=(Button) findViewById(R.id.registrar);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r=new Runnable() {
                    @Override
                    public void run() {
                        if(!tdocumento.getText().equals("")) {
                            RequestQueue requstQueue = Volley.newRequestQueue(MainActivity.this);
                            HashMap data = new HashMap();


                            data.put("documento", tdocumento.getText().toString());
                            data.put("primer_nombre", tprimer_nombre.getText().toString());
                            data.put("segundo_nombre", tsegundo_nombre.getText().toString());
                            data.put("primer_apellido", tprimer_apellido.getText().toString());
                            data.put("segundo_apellido", tsegundo_apellido.getText().toString());
                            data.put("fecha_nacimiento", tfecha_nacimiento.getText().toString());
                            data.put("sexo", tsexo.getText().toString());
                            data.put("direccion", direccion.getText().toString());
                            data.put("telefono", telefono.getText().toString());
                            data.put("longitud",tlongitud.getText().toString());
                            data.put("latitud",tlatitud.getText().toString());
                            data.put("dispositivo", sharedPref.getString("dispositivo", null));

                            JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, URL + "registraringreso", new JSONObject(data),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tdocumento.setText("");
                                                    tprimer_nombre.setText("");
                                                    tsegundo_nombre.setText("");
                                                    tprimer_apellido.setText("");
                                                    tsegundo_apellido.setText("");
                                                    tfecha_nacimiento.setText("");
                                                    tsexo.setText("");
                                                    direccion.setText("");
                                                    telefono.setText("");
                                                    tlatitud.setText("");
                                                    tlongitud.setText("");
                                                    Toast.makeText(getApplicationContext(), "Cliente registrado", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Usuario o contraseña invalidos", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                            ) {
                                //here I want to post data to sever

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> headers = new HashMap<>();
                                    // Basic Authentication
                                    //String auth = "Basic " + Base64.encodeToString(CONSUMER_KEY_AND_SECRET.getBytes(), Base64.NO_WRAP);
                                    headers.put("Authorization", "Bearer " + sharedPref.getString("token", ""));
                                    return headers;
                                }
                            };
                            requstQueue.add(jsonobj);
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Datos invalidos", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                };

                Thread h=new Thread(r);
                h.start();

            }
        });
        escanear.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null){
            if(result.getContents()!=null){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        gps = new GPSTracker(MainActivity.this);

                        // Check if GPS enabled
                        if(gps.canGetLocation()) {

                            final double latitude = gps.getLatitude();
                            final double longitude = gps.getLongitude();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tlongitud.setText(longitude+"");
                                    tlatitud.setText(latitude+"");
                                }
                            });
                            // \n is for new line


                        }
                        int valor=sharedPref.getInt("aforo",0)+1;
                        editor = sharedPref.edit();
                        editor.putInt("aforo",valor);
                        editor.commit();
                        //taforo.setText(valor+"");
                    }
                });

                String dato=result.toString();
                String dato1=dato.substring(dato.indexOf("Contents:")+9,dato.indexOf("Raw"));
                String salida=dato1.toString().replaceAll("\\W", ",");
                String resultado=salida.substring(49,170);
                String documento=resultado.substring(0,10);
                String primer_apellido="";
                String segundo_apellido="";
                String primer_nombre="";
                String segundo_nombre="";
                String sexo="";
                String fecha_nacimiento="";
                resultado=resultado.substring(10,resultado.length());
                int position=-1;
                int indice_genero=resultado.indexOf("0F");
                String resultado1=null;
                if(indice_genero!=-1){
                    resultado1=resultado.substring(0,indice_genero);
                }else{
                    indice_genero=resultado.indexOf("0M");
                    resultado1=resultado.substring(0,indice_genero);
                }



                String[] items=resultado1.split(",");

                for(int i=0;i<items.length;i++){
                  if(items[i].length()>0){
                      position++;

                      if(position==0){
                          primer_apellido=items[i];
                      }else if(position==1){
                          segundo_apellido=items[i];
                      }else if(position==2){
                          primer_nombre=items[i];
                      }else if(position==3){
                          segundo_nombre=items[i];
                      }
                  }
                }
                sexo=resultado.substring(indice_genero+1,indice_genero+2);
                fecha_nacimiento=resultado.substring(indice_genero+2,indice_genero+10);
                fecha_nacimiento=fecha_nacimiento.substring(0,4)+"-"+fecha_nacimiento.substring(4,6)+"-"+fecha_nacimiento.substring(6,8);

                tdocumento.setText(documento);
                tprimer_nombre.setText(primer_nombre);
                tsegundo_nombre.setText(segundo_nombre);
                tprimer_apellido.setText(primer_apellido);
                tsegundo_apellido.setText(segundo_apellido);
                tfecha_nacimiento.setText(fecha_nacimiento);
                tsexo.setText(sexo);

                RequestQueue requstQueue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.GET, URL + "getclientedocumento/"+documento, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if(!response.getString("direccion").equals("null")) {
                                                direccion.setText(response.getString("direccion"));
                                            }

                                            if(!response.getString("telefono").equals("null")) {
                                                telefono.setText(response.getString("telefono"));
                                            }
                                        } catch (JSONException e) {

                                        }
                                    }
                                });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            }
                        }
                ) {
                    //here I want to post data to sever

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        // Basic Authentication
                        //String auth = "Basic " + Base64.encodeToString(CONSUMER_KEY_AND_SECRET.getBytes(), Base64.NO_WRAP);
                        headers.put("Authorization", "Bearer " + sharedPref.getString("token", ""));
                        return headers;
                    }
                };
                requstQueue.add(jsonobj);


            }else{
                //texto.setText("ERROR");
            }
        }else{
            //texto.setText("ERROR");
        }
    }

    @Override
    public void onClick(View v) {
        //new IntentIntegrator(MainActivity.this).initiateScan();
        IntentIntegrator integrator = new IntentIntegrator(this);
        /*List lista=new ArrayList<>();
        lista.add("PDF_417");*/
        integrator.setDesiredBarcodeFormats(Collections.singleton("PDF_417"));
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
       String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
