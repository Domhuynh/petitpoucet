package com.dominique.petitpoucet;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.dominique.petitpoucet.MainActivity.DEFAUT_NOM_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_NUMERO_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_PERIODE;
import static com.dominique.petitpoucet.MainActivity.NomFichier;
import static java.lang.String.valueOf;

public class MonitoringFusedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,View.OnClickListener{

    // Declaration des elements affiches
    private TextView Titre;

    // Declaration des variables

    private String Nom_aidant, Numero_aidant;
    private int Periode;
    // 120 secondes pour MAP - valeur par défaut

    private String msg_bat, msg_heure;
    private String coordonnees_bis= null; // msg latitude - longitude
    private String strAdresse;

    private Toast msgT = null;

    private String tmp = null;

    private Handler myHandler;

    //Initializing the GoogleApiClient object
    private GoogleApiClient googleApiClient;

    public static final int LOCATION_REQUEST = 101;

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            // Code à éxécuter de façon périodique
            // Version à base de handler

            //MAP
            //tmp = "Runnable";
            //msgT = Toast.makeText(getApplicationContext(),tmp,Toast.LENGTH_SHORT);
            //msgT.show();

            // Collecte des données
            Monitoring();

            if (! strAdresse.equals(""))

            {
                // Envoi de SMS
                tmp = msg_heure + "\n" + "\n" + msg_bat + "\n" + coordonnees_bis + "\n" + "Adresse approximative :\n" + strAdresse;
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(Numero_aidant, null, tmp, null, null);

                // Affichage sur l'écran
                tmp = "Message envoyé à " + msg_heure + "\n" + "\n" + msg_bat + "\n" + coordonnees_bis + "\n" + "Adresse approximative :\n" + strAdresse;
                //msgT = Toast.makeText(getApplicationContext(),tmp,Toast.LENGTH_SHORT);
                //msgT.show();
                Titre.setText(tmp);
            }
            else
            {
                // Envoi de SMS
                tmp = msg_heure + "\n" + "\n" + msg_bat + "\n" + coordonnees_bis + "\n" + "Pas d'adresse disponible :\n";
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(Numero_aidant, null, tmp, null, null);

                // Affichage sur l'écran
                tmp = "Message envoyé à " + msg_heure + "\n" + "\n" + msg_bat + "\n" + coordonnees_bis + "\n" + "Pas d'adresse disponible :\n";
                //msgT = Toast.makeText(getApplicationContext(),tmp,Toast.LENGTH_SHORT);
                //msgT.show();
                Titre.setText(tmp);
            }

            // MAP :
            int tempo1 = Periode * 1000 * 20; // en millisecondes - Periode en minutes - MAP 15 min-> 5 min
            myHandler.postDelayed(this, tempo1);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cabler les rubriques utilisées

        TextView NomValeur =  findViewById(R.id.NomValeur);
        TextView MobileValeur = findViewById(R.id.MobileValeur);
        TextView PeriodeValeur = findViewById(R.id.PeriodeValeur);
        Button g_bouton = findViewById(R.id.g_bouton);
        Button d_bouton = findViewById(R.id.d_bouton);

        //Building a instance of Google Api Client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();


        // relire le fichier

        try {
            // ouverture du fichier
            FileInputStream input = openFileInput(NomFichier);
            int value;

            // lecture
            StringBuilder lu = new StringBuilder();

            while ((value =input.read()) != -1) {
                lu.append((char)value);
            }

 //           if (input != null ) {

                //tmp = "buffer : " + lu.toString();
                //msgT = Toast.makeText(this, tmp, Toast.LENGTH_LONG);
                //msgT.show();

                // Decomposition du buffer
                String svgParametres = lu.toString();

                int nbVirgules = svgParametres.length() - svgParametres.replace(",", "").length();

                if (nbVirgules == 2) {

                    String[] separated = svgParametres.split(",");
                    Nom_aidant = separated[0];
                    Numero_aidant = separated[1];
                    Periode = Integer.parseInt(separated[2]);
                    //tmp = "retour lu fichier numero" + Numero_aidant;
                    //msgT = Toast.makeText(this,tmp, Toast.LENGTH_SHORT);
                    //msgT.show();
                } else {
                    // le fichier existe mais pb sur le fichier
                    tmp = "pb sur le buffer";
                    msgT = Toast.makeText(this, tmp, Toast.LENGTH_LONG);
                    msgT.show();

                    // MAP si ni Bundle ni fichier, on arrive chez moi
                    Nom_aidant =DEFAUT_NOM_AIDANT;
                    Numero_aidant = DEFAUT_NUMERO_AIDANT;
                    Periode = DEFAUT_PERIODE ;
                }
 //           }

            // fermeture du fichier
            input.close();
        }

        catch (FileNotFoundException e) {
            msgT = Toast.makeText(this,"File Not Found", Toast.LENGTH_SHORT);
            msgT.show();

            // MAP si ni Bundle ni fichier, on arrive chez moi
            Nom_aidant =DEFAUT_NOM_AIDANT;
            Numero_aidant = DEFAUT_NUMERO_AIDANT;
            Periode = DEFAUT_PERIODE ;

            e.printStackTrace();
        }
        catch (IOException e) {
            msgT = Toast.makeText(this,"IOException", Toast.LENGTH_SHORT);
            msgT.show();
            Nom_aidant =DEFAUT_NOM_AIDANT;
            Numero_aidant = DEFAUT_NUMERO_AIDANT;
            Periode = DEFAUT_PERIODE ;

        }

        // Affecter les valeurs aux champs affichés

        NomValeur.setText(Nom_aidant);
        MobileValeur.setText(Numero_aidant);
        PeriodeValeur.setText(valueOf(Periode));

        //tmp = "Arrivée dans monitoring";
        // msgT = Toast.makeText(getApplicationContext(),tmp,Toast.LENGTH_SHORT);
        //msgT.show();

        myHandler = new Handler();
        myHandler.postDelayed(myRunnable,500); // on lance après 500ms

        // On surveille le boutons
        // On lance la surveillance du click
        // OnClick ci-après précise le traitement à réaliser

        g_bouton.setTag(0);
        d_bouton.setTag(1);
        g_bouton.setOnClickListener(this);
        d_bouton.setOnClickListener(this);

    }

    // Fin de On Create

    // Google API fused GPS

    public void onStart() {
        super.onStart();
        // Initiating the GoogleApiClient Connection when the activity is visible
        googleApiClient.connect();
    }
    public void onStop() {
        super.onStop();
        //Disconnecting the GoogleApiClient when the activity goes invisible
        googleApiClient.disconnect();
    }

    /*
This callback is invoked when the GoogleApiClient is successfully connected
*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //We set a listener to our button only when the ApiClient is connected successfully
        //locationButton.setOnClickListener(this);

    }

    //Callback invoked if the GoogleApiClient connection is suspended
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection was suspended", Toast.LENGTH_SHORT).show();
    }
    //Callback invoked if the GoogleApiClient connection fails
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    //This callback is invoked when the user grants or rejects the location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void getCurrentLocation() {
        //Checking if the location permission is granted
        String msg;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST);
            return;
        }
        //Fetching location using FusedLOcationProviderAPI
        FusedLocationProviderApi fusedLocationApi = LocationServices.FusedLocationApi;
        Location location = fusedLocationApi.getLastLocation(googleApiClient);

        //In some rare cases Location obtained can be null
        if (location == null)
            Toast.makeText(this, "Not able to fetch location", Toast.LENGTH_SHORT).show();

        else {
            msg = String.format("Location co-ord are " + location.getLatitude() + "," + location.getLongitude());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

        coordonnees_bis = String.format("Latitude : %f - Longitude : %f\n", location.getLatitude(), location.getLongitude());

        // Recherche de l'adresse
        //

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> adresses = null;

        try {
            adresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        }
        catch (IOException ioException) {
            tmp = "IO Exception Geocoder";
            msgT = Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
            msgT.show();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            tmp = "IO IllegalArgument Geocoder";
            msgT = Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
            msgT.show();
        }
        catch (NullPointerException  nullPointerExction) {
            Toast.makeText(getApplicationContext(), "NullPointerException", Toast.LENGTH_SHORT).show();
        }

        if (adresses == null || adresses.size() == 0) {
            // trace MAP
            msgT = Toast.makeText(this, "Pas d adresse touvee ", Toast.LENGTH_LONG);
            msgT.show();
        }
        else {
            Address adresse = adresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            strAdresse = adresse.getAddressLine(0) + ", " + adresse.getLocality();

            // trace MAP

            //msgT = Toast.makeText(this, "PositionAdresse " + strAdresse, Toast.LENGTH_SHORT);
            //msgT.show();

            // SmsManager sms = SmsManager.getDefault();
            // sms.sendTextMessage("0684975391", null,  "je suis : " + strAdresse, null, null);

            for (int i = 0; i <= adresse.getMaxAddressLineIndex(); i++) {
                addressFragments.add(adresse.getAddressLine(i));
            }
        }   // fin du else du if adresse == null...



    }


    private void Monitoring() {

        // Validation de la fonction de lecture de la batterie
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        assert bm != null;
        int batlevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        msg_bat = "Batterie : "+ batlevel +"%";

        // recuperation de l'heure courante
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        msg_heure = hour +"h "+ minute +"mn";

        // recuperation de la position GPS
        getCurrentLocation();

    }

    // arret du handler en fin d'activite
    public void onDestroy() {
        super.onDestroy();
        if(myHandler != null)
            myHandler.removeCallbacks(myRunnable); // On arrete le callback

        //tmp = "Handler arrêté";
        //msgT = Toast.makeText(getApplicationContext(),tmp,Toast.LENGTH_LONG);
        //msgT.show();
    }

/*
    public class MyLocationListener implements LocationListener {

        private Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        // Utilisation d'un listener pour gérer les changements

        private Toast msgT = null;
        private int i = 0;
        private String tmp = null;



        public void onLocationChanged(Location localisation) {

            List<Address> adresses = null;

            try {
                adresses = geocoder.getFromLocation(localisation.getLatitude(), localisation.getLongitude(), 1);
            } catch (IOException ioException) {
                // Log.e("GPS", "erreur", ioException);
                tmp = "GPS KO";
                msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
                msgT.show();
            } catch (IllegalArgumentException illegalArgumentException) {
                //Log.e("GPS", "erreur " + coordonnees, illegalArgumentException);
                tmp = "GPS KO";
                msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
                msgT.show();
            }

            if (adresses == null || adresses.size() == 0) {

                //Log.e("GPS", "erreur aucune adresse !");
                tmp = "GPS OK mais aucune adresse";
                msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
                msgT.show();
                strAdresse="";


            } else {
                Address adresse = adresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                String strAdresse = adresse.getAddressLine(0) + ", " + adresse.getLocality();
                // Log.d("GPS", "adresse : " + strAdresse);

                // trace MAP

                //tmp = "OnLocationChanged " + strAdresse;
                //msgT = Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
                //msgT.show();

                // Validation de la fonction envoi de SMS

                //SmsManager sms = SmsManager.getDefault();
                //sms.sendTextMessage("0684975391", null,  "je suis : " + strAdresse, null, null);


                for (int i = 0; i <= adresse.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(adresse.getAddressLine(i));
                }
                // Log.d("GPS", TextUtils.join(System.getProperty("line.separator"), addressFragments));
                // Adresse.setText(TextUtils.join(System.getProperty("line.separator"), addressFragments));
            }
        }
*/


/*        @Override
        public void onStatusChanged(String fournisseur, int status, Bundle extras) {
            // Toast toast = Toast.makeText(this, fournisseur + " état : " + status, Toast.LENGTH_SHORT);
            //toast.show();
        }

        @Override
        public void onProviderEnabled(String fournisseur) {
            // Toast toast = Toast.makeText(this, fournisseur + " activé !", Toast.LENGTH_SHORT);
            // toast.show();
        }

        @Override
        public void onProviderDisabled(String fournisseur) {
            Toast msgT = Toast.makeText(getApplicationContext(), "fournisseur désactivé !", Toast.LENGTH_SHORT);
            msgT.show();
        }*/


/*    public void PositionAdresse() {


        getCurrentLocation();



            // Recherche de l'adresse
            //
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address> adresses = null;

            try {
                adresses = geocoder.getFromLocation(localisation.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ioException) {
                tmp = "IO Exception Geocoder";
                msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
                msgT.show();

            } catch (IllegalArgumentException illegalArgumentException) {
                tmp = "IO IllegalArgument Geocoder";
                msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
                msgT.show();
            }

            if (adresses == null || adresses.size() == 0) {
                // trace MAP
                msgT = Toast.makeText(this, "Pas d adresse touvee ", Toast.LENGTH_LONG);
                msgT.show();
            } else {
                Address adresse = adresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                strAdresse = adresse.getAddressLine(0) + ", " + adresse.getLocality();

                // trace MAP

                //msgT = Toast.makeText(this, "PositionAdresse " + strAdresse, Toast.LENGTH_SHORT);
                //msgT.show();

                // SmsManager sms = SmsManager.getDefault();
                // sms.sendTextMessage("0684975391", null,  "je suis : " + strAdresse, null, null);


                for (int i = 0; i <= adresse.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(adresse.getAddressLine(i));
                }
            }   // fin du if adresse == null...

        }   // fin du localisation != null
        else
        {
            tmp = "pas de service localisation";
            msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
            msgT.show();
        }
    }
    else {
        tmp = "pas de fournisseur GPS";
        msgT = (Toast) Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT);
        msgT.show();
    };

    }*/

    @Override
    public void onClick(View view) {
        // user just clicked
        confirm();
        //finish();
    }


    public void confirm() {

        // set title
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MonitoringFusedActivity.this);
        alertDialogBuilder.setTitle("   ");


        // set dialog message
        alertDialogBuilder
                .setMessage("Voulez-vous quitter ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        MonitoringFusedActivity.this.finish();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

    }

    @Override
    public void onBackPressed() {

        confirm();

        // return;
    }

}


