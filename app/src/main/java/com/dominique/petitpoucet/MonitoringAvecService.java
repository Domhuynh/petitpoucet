package com.dominique.petitpoucet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.dominique.petitpoucet.MainActivity.DEFAUT_NOM_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_NUMERO_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_PERIODE;
import static com.dominique.petitpoucet.MainActivity.NomFichier;

import static java.lang.String.valueOf;

public class MonitoringAvecService extends AppCompatActivity implements View.OnClickListener {

    // Declaration des elements affiches
    private TextView Titre;

    // Declaration des variables

    private String Nom_aidant, Numero_aidant;
    private int Periode;
    // 120 secondes pour MAP - valeur par défaut


    private BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String message = bundle.getString(MonitoringService.MESSAGE);
                    int resultCode = bundle.getInt(MonitoringService.RESULT);
                    if (resultCode == RESULT_OK) {
                        // Afficher le message;
                        Titre.setText(message);
                    } else {
                        // Afficher qu'il y a un problème

                    }
                }
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_avec_service);

        // Cabler les rubriques utilisées

        TextView NomValeur =  findViewById(R.id.NomValeur);
        TextView MobileValeur = findViewById(R.id.MobileValeur);
        TextView PeriodeValeur = findViewById(R.id.PeriodeValeur);
        Button g_bouton = findViewById(R.id.g_bouton);
        Button d_bouton = findViewById(R.id.d_bouton);

        litFichier();
        // Affecter les valeurs aux champs affichés

        NomValeur.setText(Nom_aidant);
        MobileValeur.setText(Numero_aidant);
        PeriodeValeur.setText(valueOf(Periode));

        // On tag les boutons de changement d'activite
        // On lance la surveillance du click
        // OnClick ci-après précise le traitement à réaliser

        g_bouton.setTag(0);
        d_bouton.setTag(1);
        g_bouton.setOnClickListener(this);
        d_bouton.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                MonitoringService.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View view) {
        // user just clicked
        Intent ActiviteSuivante;
        // Recuperation de l evenement - Tag fixe plus haut - gauche : 0 - droite : 1 - fin : 2
        int valeurbouton = (int) view.getTag();

        if (valeurbouton == 0 ) {

            // arrêter

            confirm();

            // arrêter le service

            Intent intent = new Intent(MonitoringAvecService.this, MonitoringService.class);
            stopService(intent);

            // arrêter l'activité
            finish();
        }

    }

    private void confirm() {

        // set title
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MonitoringAvecService.this);
        alertDialogBuilder.setTitle("...");


        // set dialog message
        alertDialogBuilder
                .setMessage("Voulez-vous quitter ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        MonitoringAvecService.this.finish();
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

    private void litFichier() {

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


            // Decomposition du buffer
            String SvgParametres = lu.toString();

            int nbVirgules = SvgParametres.length() - SvgParametres.replace(",", "").length();

            if (nbVirgules == 2) {

                String[] separated = SvgParametres.split(",");
                Nom_aidant = separated[0];
                Numero_aidant = separated[1];
                Periode = Integer.parseInt(separated[2]);

            } else {
                // le fichier existe mais pb sur le fichier
                Toast.makeText(this, "Pb sur le buffer", Toast.LENGTH_LONG).show();

                Nom_aidant=DEFAUT_NOM_AIDANT;
                Numero_aidant = DEFAUT_NUMERO_AIDANT;
                Periode = DEFAUT_PERIODE ;
            }
            //}

            // fermeture du fichier
            input.close();
        }


        catch (FileNotFoundException e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();

            // MAP si ni Bundle ni fichier, on arrive chez moi
            Nom_aidant=DEFAUT_NOM_AIDANT;
            Numero_aidant = DEFAUT_NUMERO_AIDANT;
            Periode = DEFAUT_PERIODE ;

            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }
}
