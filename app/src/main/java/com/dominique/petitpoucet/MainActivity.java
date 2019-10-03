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

    public static String Nom_aidant, Numero_aidant;
    public static int Periode;

    public static final String BUNDLE_NOM_AIDANT = "Nom";
    public static final String BUNDLE_NUMERO_AIDANT = "Numero";
    public static final String BUNDLE_PERIODE = "60";

    public static final String DEFAUT_NOM_AIDANT = "NN";
    public static final String DEFAUT_NUMERO_AIDANT = "0x2x4x6x8x"; // MAP
    public static final int DEFAUT_PERIODE = 60;   // pas top les formats différents bundle et defaut...

    public static final String NomFichier = "data1.txt";
    public static final String FichierMsg = "msg.txt";

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

        // relire le fichier

        try {
            // ouverture du fichier puis lecture
            FileInputStream input = openFileInput(NomFichier);
            int value;
            StringBuilder lu = new StringBuilder();
            while ((value = input.read()) != -1) {
                lu.append((char) value);
            }

            // Decomposition du buffer
            SvgParametres = lu.toString();

            int nbVirgules = SvgParametres.length() - SvgParametres.replace(",", "").length();

            if (nbVirgules == 2) {

                String[] separated = SvgParametres.split(",");
                Nom_aidant = separated[0];
                Numero_aidant = separated[1];
                Periode = Integer.parseInt(separated[2]);
                }
            else {
                // le fichier existe mais pb sur le fichier
                Toast.makeText(this, "Pb sur le fichier", Toast.LENGTH_LONG).show();

                // valeurs par defaut si pb fichier
                Nom_aidant = DEFAUT_NOM_AIDANT;
                Numero_aidant = DEFAUT_NUMERO_AIDANT;
                Periode = DEFAUT_PERIODE;
                }

            // fermeture du fichier
            input.close();
            }

        catch (FileNotFoundException e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
            }
        catch (IOException e) {
            Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
            }


        // Affecter les valeurs aux champs affichés

        NomValeur.setText(Nom_aidant);
        MobileValeur.setText(Numero_aidant);
        PeriodeValeur.setText(valueOf(Periode));

        // Saisir les choix utilisateur

        // On tag les boutons. On lance la surveillance du click. OnClick ci-après précise le traitement à réaliser

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
        // Recuperation de l evenement - Tag fixe plus haut - gauche : 0 - droite : 1 - fin : 2
        int valeurbouton = (int) view.getTag();
        switch (valeurbouton) {
            case 0:

                ActiviteSuivante = new Intent(MainActivity.this, ModifieParametres.class);
                startActivity(ActiviteSuivante);
                break;

            case 1:

                //ActiviteSuivante = new Intent(MainActivity.this, MonitoringFusedActivity.class);
                //startActivity(ActiviteSuivante);

                // Alternative demarrage du service
                Intent intent = new Intent(MainActivity.this,MonitoringService.class);
                startService(intent);

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


    private void confirm() {

        // set title
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("...");


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