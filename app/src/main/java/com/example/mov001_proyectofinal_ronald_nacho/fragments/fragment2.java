package com.example.mov001_proyectofinal_ronald_nacho.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.example.mov001_proyectofinal_ronald_nacho.Models.Propiedad;
import com.example.mov001_proyectofinal_ronald_nacho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.Activity.RESULT_OK;


public class fragment2 extends Fragment {

    Button elegirImg, subirImg;
    ImageView imagenView;
    private final int PICK_IMAGE_REQUEST = 111;
    Uri filePath;
    ProgressDialog pd;

    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://proyectoronaldnacho-7eb34.appspot.com");

    public String TAG="FireBase";

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String respGarage;
    public String respCategoria;
    public String respProvincia;
    public String LatitudLongitud;

    public String vContenido="";
    public StringBuilder descarga = new StringBuilder();
    private ConnectivityManager cm;

    public String valorcol;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =getContext().getSharedPreferences("shared_login_data",   Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("coordenadas", "0/0");
        editor.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_fragment2, container, false);

        cm =  (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            SetupUI(view);


        return view;

    }


    public void SetupUI(final View view){

        //fragment2Args args= fragment2Args.fromBundle(getArguments());

        //LatitudLongitud = args.getArgLatLon();

        SharedPreferences  prefs =getContext().getSharedPreferences("shared_login_data",   Context.MODE_PRIVATE);
        String coorden = prefs.getString("coordenadas", ""); // prefs.getString("nombre del campo" , "valor por defecto")
        final EditText etPrecio=(EditText)view.findViewById(R.id.etPrecio);
        final TextView etPreciodol=(TextView)view.findViewById(R.id.etPreciodol);

        Button btnDolares = (Button)view.findViewById(R.id.btndolares);
        btnDolares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String url = "http://www.apilayer.net/api/live?access_key=bd179b8245761835672c1433a23e904b&currencies=USD,MXN,CRC,BRL,NGN,QAR&format=1";
                if (hasInternetAccess()) {
                    DescargaCodigo(url);
                }

                //vContenido = bundle.getString("Carga");
                JSONObject response = null;
                try {
                    response = new JSONObject(descarga.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String success = response.getString("success");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    final JSONObject currencies = response.getJSONObject("quotes");

                    double colon = currencies.getDouble("USDCRC");

                    if (colon!=0 ) {
                        if (etPrecio.getText().toString() != "0"){
                            double calcol = Double.parseDouble(etPrecio.getText().toString()) / colon;
                            valorcol = String.valueOf(calcol);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                etPreciodol.setText(valorcol);

            }
        });



        LatitudLongitud=coorden;
        //Se recibe Lat/Lon
        if (LatitudLongitud.equals("SinValor"))
        {
            LatitudLongitud="9/-84";
        }
        //Toast.makeText(getContext(),LatitudLongitud,Toast.LENGTH_SHORT).show();


        final EditText tit=(EditText)view.findViewById(R.id.etTitulo);
        tit.requestFocus();

        final Spinner spinnerCategoria=(Spinner)view.findViewById(R.id.categoria_spinner);


        ArrayAdapter<CharSequence> adapCategoria=ArrayAdapter.createFromResource(getContext(),R.array.categorias_spinner,R.layout.support_simple_spinner_dropdown_item);
        adapCategoria.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapCategoria);

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                respCategoria = spinnerCategoria.getSelectedItem().toString();
                //Toast.makeText(getContext(),respCategoria,Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final Spinner spinnerProvincia=(Spinner)view.findViewById(R.id.provincia_spinner);

        ArrayAdapter<CharSequence> adapProvincia=ArrayAdapter.createFromResource(getContext(),R.array.provincias_spinner,R.layout.support_simple_spinner_dropdown_item);
        adapProvincia.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerProvincia.setAdapter(adapProvincia);

        spinnerProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                respProvincia = spinnerProvincia.getSelectedItem().toString();
                //Toast.makeText(getContext(),respProvincia,Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final Spinner spinnerGarage=(Spinner)view.findViewById(R.id.garage_spinner);

        ArrayAdapter<CharSequence> adapGarage=ArrayAdapter.createFromResource(getContext(),R.array.garage_spinner,R.layout.support_simple_spinner_dropdown_item);
        adapGarage.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerGarage.setAdapter(adapGarage);

        spinnerGarage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                respGarage = spinnerGarage.getSelectedItem().toString();
                //numHab.requestFocus();
                //Toast.makeText(getContext(),respGarage,Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        Button btnSeleccionar = (Button)view.findViewById(R.id.btSeleccionar);
        btnSeleccionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(view).navigate(R.id.Accion2ToBuscar);

            }
        });

        Button mbutton=(Button)view.findViewById(R.id.btRegistrar);

        mbutton.setOnClickListener(new View.OnClickListener() {

                                       @Override
                                       public void onClick(View v) {

                   EditText etTitulo=(EditText)view.findViewById(R.id.etTitulo);
                   EditText etNumHabitaciones=(EditText)view.findViewById(R.id.etNumHabitaciones);
                   //EditText etPrecio=(EditText)view.findViewById(R.id.etPrecio);
                                           //EditText etUbic=(EditText)view.findViewById(R.id.etUbicacion);
                                           EditText etPropietario=(EditText)view.findViewById(R.id.etPropietario);

                   Propiedad obj=new Propiedad("",etTitulo.getText().toString(),respCategoria,respProvincia,respGarage,
                           Integer.parseInt(etNumHabitaciones.getText().toString()),Double.parseDouble(etPrecio.getText().toString()),
                           Double.parseDouble(etPreciodol.getText().toString()),LatitudLongitud,etPropietario.getText().toString());


                   // Add a new document with a generated ID
                   db.collection("Propiedades")
                           .add(obj)
                           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                               @Override
                               public void onSuccess(DocumentReference documentReference) {
                                   Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                                   Navigation.findNavController(view).navigate(R.id.AccionTofragment3);

                               }
                           })
                           .addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Log.w(TAG, "Error adding document", e);
                               }
                           });

                   Toast.makeText(getContext(), "Agregando....",Toast.LENGTH_SHORT).show();

                   etTitulo.setText("");
                   spinnerCategoria.setSelection(0);
                   spinnerProvincia.setSelection(0);
                   spinnerGarage.setSelection(0);
                   etNumHabitaciones.setText("");
                   etPrecio.setText("");
                   etPreciodol.setText("");
                   etPropietario.setText("");

           }
           }
        );

        elegirImg = (Button) view.findViewById(R.id.btbuscafoto);
        subirImg = (Button)  view.findViewById(R.id.btsubirfoto);
        imagenView = (ImageView) view.findViewById(R.id.imgView);

        pd = new ProgressDialog(getContext());
        pd.setMessage("Subiendo....");

        elegirImg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                //intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
            }

        });

        subirImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filePath != null) {
                    pd.show();

                    StorageReference childRef = storageRef.child("image.jpg");

                    //uploading the image
                    UploadTask uploadTask = childRef.putFile(filePath);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Subida exitosamente", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Problema al subir -> " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Seleccionar una imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap( getActivity().getContentResolver(), filePath);

                imagenView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public String DescargaCodigo (String url){
        //StringBuilder descarga = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try{
            URL urljson = new URL(url);
            urlConnection=(HttpURLConnection) urljson.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null){
                descarga.append(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            assert  urlConnection != null;
            urlConnection.disconnect();
        }
        return descarga.toString();

    }

    public boolean hasInternetAccess()
    {


        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }


}
