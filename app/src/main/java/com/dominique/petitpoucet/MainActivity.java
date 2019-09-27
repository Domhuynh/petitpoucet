package com.dominique.petitpoucet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // On annonce ci-dessus qu'on va utiliser une interface InClickListener

    // Declaration des variables


    public static String Nom_aidant, Numero_aidant;
    public static int Periode;

    // Declaration du Bundle

    public static final String BUNDLE_NOM_AIDANT = "Nom";
    public static final String BUNDLE_NUMERO_AIDANT = "Numero";
    public static final String BUNDLE_PERIODE = "60";

    // valeurs par défaut

    public static final String DEFAUT_NOM_AIDANT = "NN";
    public static final String DEFAUT_NUMERO_AIDANT = "0x2x4x6x8x"; // MAP
    public static final int DEFAUT_PERIODE = 60;   // pas top les formats différents bundle et defaut...

    // Declaration du fichier

    public static final String NomFichier = "data1.txt";

    public String SvgParametres = null;
    // tampon pour la sauvegarde / lecture des parametres





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Declaration des elements affiches
        // TextView Titre, Texte, NomRubrique, MobileRubrique, PeriodeRubrique;
        TextView NomValeur, MobileValeur,PeriodeValeur;
        Button g_bouton, d_bouton, fin_bouton;

        setContentView(R.layout.activity_main);

        // MAJ et Afficher écran d'accueil

        // Cabler les rubriques

        //Titre = findViewById(R.id.Titre);
        //Texte = findViewById(R.id.Texte);
        //NomRubrique = findViewById(R.id.NomRubrique);
        NomValeur = findViewById(R.id.NomValeur);
        //MobileRubrique = findViewById(R.id.MobileRubrique);
        MobileValeur = findViewById(R.id.MobileValeur);
        //PeriodeRubrique = findViewById(R.id.PeriodeRubrique);
        PeriodeValeur = findViewById(R.id.PeriodeValeur);
        g_bouton = findViewById(R.id.g_bouton);
        d_bouton = findViewById(R.id.d_bouton);
        fin_bouton = findViewById(R.id.fin_bouton);

        // Récupérer les paramètres de fonctionnement

        // Initialisation
        Nom_aidant = DEFAUT_NOM_AIDANT;
        Numero_aidant = DEFAUT_NUMERO_AIDANT;
        Periode = DEFAUT_PERIODE;

        // tampons de travail
        Toast msgT;
        String tmp;

        // Relire le Bundle au cas ou interruption prealable à On Create
        // Sinon on relit le fichier
        // Pourquoi ne pas relire le fichier directement : si interruption avant sauvegarde ?

        // if (savedInstanceState != null) {
        // relire le bundle

        //   Nom_aidant = savedInstanceState.getString(BUNDLE_NOM_AIDANT);
        //   Numero_aidant = savedInstanceState.getString(BUNDLE_NUMERO_AIDANT);
        //   Periode = Integer.parseInt((savedInstanceState.getString(BUNDLE_PERIODE)));

        // trace MAP
        //   msgT = Toast.makeText(this, "Bundle relu "+ Nom_aidant, Toast.LENGTH_SHORT);
        //   msgT.show();

        //} else {
        // relire le fichier

        try {
            // ouverture du fichier
            FileInputStream input = openFileInput(NomFichier);
            int value;

            // lecture
            StringBuilder lu = new StringBuilder();

            while ((value = input.read()) != -1) {
                lu.append((char) value);
            }


            if (input != null) {

                //tmp = "buffer : " + lu.toString();
                // msgT = Toast.makeText(this, tmp, Toast.LENGTH_LONG);
                //msgT.show();

                // Decomposition du buffer
                SvgParametres = lu.toString();

                int nbVirgules = SvgParametres.length() - SvgParametres.replace(",", "").length();

                if (nbVirgules == 2) {

                    String[] separated = SvgParametres.split(",");
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

                    // valeurs par defaut si pb fichier
                    Nom_aidant = DEFAUT_NOM_AIDANT;
                    Numero_aidant = DEFAUT_NUMERO_AIDANT;
                    Periode = DEFAUT_PERIODE;
                }
            }

            // fermeture du fichier
            input.close();
        } catch (FileNotFoundException e) {
            msgT = Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT);
            msgT.show();

            // MAP si ni Bundle ni fichier, on arrive chez moi
            //       Nom_aidant="Nnnnnnnn";
            //       Numero_aidant ="0684975391";
            //       Periode = 120;

            // e.printStackTrace();
        } catch (IOException e) {
            msgT = Toast.makeText(this, "IOException", Toast.LENGTH_SHORT);
            msgT.show();
            // e.printStackTrace();
        }


        // Affecter les valeurs aux champs affichés

        NomValeur.setText(Nom_aidant);
        MobileValeur.setText(Numero_aidant);
        PeriodeValeur.setText(valueOf(Periode));

        // Saisir les choix utilisateur

        // On tag les boutons
        // On lance la surveillance du click
        // OnClick ci-après précise le traitement à réaliser

        g_bouton.setTag(0);
        d_bouton.setTag(1);
        fin_bouton.setTag(2);
        g_bouton.setOnClickListener(this);
        d_bouton.setOnClickListener(this);
        fin_bouton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        // user just clicked
        Intent ActiviteSuivante;
        // Recuperation de l evenement - Tag fixe plus haut - gauche : 0 - droite : 1
        int valeurbouton = (int) view.getTag();
        switch (valeurbouton) {
            case 0:
                // MAP
                //    String msg = "Bouton gauche";
                // Toast msgT = Toast.makeText(this,"bouton gauche",Toast.LENGTH_SHORT);
                //msgT.show();
                // Lancer l'activité ModifieParametres
                ActiviteSuivante = new Intent(MainActivity.this, ModifieParametres.class);
                startActivity(ActiviteSuivante);
                break;

            case 1:
                // MAP

                //msgT = Toast.makeText(this,"bouton droit", Toast.LENGTH_SHORT);
                //msgT.show();
                // Lancer le monitoring
                ActiviteSuivante = new Intent(MainActivity.this, MonitoringFusedActivity.class);
                startActivity(ActiviteSuivante);

                break;

            case 2:
                finish();
                break;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Sauvegarde dans le bundle en cas de changement d'activite

        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_NOM_AIDANT, Nom_aidant);
        outState.putString(BUNDLE_NUMERO_AIDANT, Numero_aidant);
        outState.putString(BUNDLE_PERIODE, valueOf(Periode));
    }


    public void confirm() {

        // set title
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Touche retour actionnée");


        // set dialog message
        alertDialogBuilder
                .setMessage("Voulez-vous quitter ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        MainActivity.this.finish();
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