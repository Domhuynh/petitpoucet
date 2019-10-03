package com.dominique.petitpoucet;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.dominique.petitpoucet.MainActivity.DEFAUT_NOM_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_NUMERO_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_PERIODE;
import static com.dominique.petitpoucet.MainActivity.FichierMsg;
import static com.dominique.petitpoucet.MainActivity.NomFichier;
import static com.dominique.petitpoucet.MonitoringFusedActivity.LOCATION_REQUEST;


public class MonitoringService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private int result = Activity.RESULT_CANCELED;
    public static final String MESSAGE = "message";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.dominique.petitpoucet";


    public static final int ID_NOTIFICATION = 1964;

    //Initializing the GoogleApiClient object
    private GoogleApiClient googleApiClient;
    String msg_heure, msg_bat, message_statut;
    private String coordonnees_bis= null; // msg latitude - longitude
    private String strAdresse;

    private String Nom_aidant, Numero_aidant;
    private int Periode;
    private Handler myHandler;

    public MonitoringService(){
        super("MonitoringService");
    }

    @Override
    protected void onHandleIntent(Intent intent){

        //Building a instance of Google Api Client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        litFichierLog();

        myHandler = new Handler();
        myHandler.postDelayed(myRunnable,500); // on lance myRunnable après 500ms

    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            // Code à éxécuter de façon périodique
            // Version à base de handler

            // Collecte des données
            Monitoring();

            // Elaboration du message
            if (! strAdresse.equals(""))
            {
                message_statut = msg_heure + "\n" + "\n" + msg_bat + "\n" + coordonnees_bis + "\n" + "Adresse approximative :\n" + strAdresse;
            }
            else
            {
                message_statut = msg_heure + "\n" + "\n" + msg_bat + "\n" + coordonnees_bis + "\n" + "Pas d'adresse disponible :\n";
            }

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(Numero_aidant, null, message_statut, null, null);

            // envoi vers activité pour affichage
            result = Activity.RESULT_OK;
            publieResult(message_statut,result);


            // on reboucle sur le handler
            int tempo1 = Periode * 1000 * 20; // en millisecondes - Periode en minutes - MAP 15 min-> 5 min
            myHandler.postDelayed(this, tempo1);

        }
    };


    protected void publieResult(String message, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(MESSAGE, message);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    // Notification



    private void createNotify(){

        //On crée un "gestionnaire de notification"
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //Le PendingIntent c'est ce qui va nous permettre d'atteindre notre deuxième Activity.ActivityNotification sera donc le nom de notre seconde Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MonitoringFusedActivity.class), 0);

        // recuperation de l'heure courante
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        msg_heure = hour +"h "+ minute +"mn";

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Recherche position pour envoi SMS à ")
                .setContentText(msg_heure)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(ID_NOTIFICATION, notification);

    }


    //Méthode pour supprimer de la liste de notification la notification que l'on vient de créer
    private void cancelNotify(){
        //On crée notre gestionnaire de notification
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //on supprime la notification grâce à son ID
        notificationManager.cancel(ID_NOTIFICATION);
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

 /*   //This callback is invoked when the user grants or rejects the location permission
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

*/

    private void getCurrentLocation() {
        //Checking if the location permission is granted
        String msg;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[] {
            //        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            //}, LOCATION_REQUEST);
            //
            // Retiré selon ref : devdeeds.com/android-location-tracking-in-background-service
            //
            Log.d("getCurrentLocation", "== Error On onConnected() Permission not granted");
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
            Log.d("getCurrentLocation","IO Exception Geocoder");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            Log.d("getCurrentLocation","IO IllegalArgument Geocoder");
        }
        catch (NullPointerException  nullPointerExction) {
            Log.d("getCurrentLocation","NullPointerException");
        }

        if (adresses == null || adresses.size() == 0) {
            // trace MAP
            Log.d("getCurrentLocation","Pas d adresse touvee ");
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

    private void litFichierLog(){

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
                Log.d("litFichierLog","pb sur le buffer");

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
            Log.d("litFichierLog","FileNotFoundException");

            // MAP si ni Bundle ni fichier, on arrive chez moi
            Nom_aidant =DEFAUT_NOM_AIDANT;
            Numero_aidant = DEFAUT_NUMERO_AIDANT;
            Periode = DEFAUT_PERIODE ;

            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d("litFichierLog","IOException");

            Nom_aidant =DEFAUT_NOM_AIDANT;
            Numero_aidant = DEFAUT_NUMERO_AIDANT;
            Periode = DEFAUT_PERIODE ;

        }




    }


}


