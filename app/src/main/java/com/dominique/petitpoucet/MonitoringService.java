package com.dominique.petitpoucet;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.IBinder;
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

import static com.dominique.petitpoucet.MainActivity.FichierMsg;
import static com.dominique.petitpoucet.MainActivity.NomFichier;
import static com.dominique.petitpoucet.MonitoringFusedActivity.LOCATION_REQUEST;


public class MonitoringService extends JobService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int ID_NOTIFICATION = 1964;
    //Initializing the GoogleApiClient object
    private GoogleApiClient googleApiClient;

    String msg_heure, msg_bat;
    private String coordonnees_bis= null; // msg latitude - longitude
    private String strAdresse;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize your service here
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //destroy your service here
    }

    // The binder that glue to my service

    private final Binder binder=new LocalBinder();

    public class LocalBinder extends Binder {
        MonitoringService getService() {
            return (MonitoringService.this);
        }
    }

  /* A priori pas possible / necessaire dans un Jobservice, ie au Jobscheduler

    @Override
    public IBinder onBind (Intent intent) {

        return binder;
    }*/

    // The key of the Intent to communicate with the service's users

    public static final String MY_SERVICE_INTENT = "stinfoservices.net.android.MyUniqueItentServiceKey";

    private Intent broadcast = new Intent(MY_SERVICE_INTENT);

    // This class aims to make a task in a separeted thread

    class MyTaskInAnOtherThread extends AsyncTask<Location, Void, Void> {
        @Override
        protected Void doInBackground(Location... locs) {
            // Do something to update your data ; here is the place where your treatment is done in another thread than in the GUI thread
            // Prevent your listener that something happens
            sendBroadcast(broadcast);
            return (null);
        }
    }



    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i("JobServiceSample", "MainJobService start" );
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("JobServiceSample", "MainJobService stop" );
        return true;
    }

    // Scheduler

    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, MonitoringService.class);
        JobInfo jobInbo = new JobInfo.Builder(0, serviceComponent)
                .setPeriodic( 30000, JobInfo.getMinFlexMillis() )
                .build();

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(jobInbo);
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


}


