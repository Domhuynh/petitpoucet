package com.dominique.petitpoucet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.dominique.petitpoucet.MainActivity.BUNDLE_NOM_AIDANT;
import static com.dominique.petitpoucet.MainActivity.BUNDLE_NUMERO_AIDANT;
import static com.dominique.petitpoucet.MainActivity.BUNDLE_PERIODE;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_NOM_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_NUMERO_AIDANT;
import static com.dominique.petitpoucet.MainActivity.DEFAUT_PERIODE;
import static com.dominique.petitpoucet.MainActivity.NomFichier;
import static java.lang.String.valueOf;


public class ModifieParametres extends AppCompatActivity implements View.OnClickListener {

    // Procéder à la modification des parametres

    // Declaration des variables


    private String Nom_aidant, Numero_aidant;

    private int Periode;
    // 120 secondes pour MAP - valeur par défaut

    // public static final String NomFichier  dans MainActivity
    //private File mFile = null;

    // tampon pour la sauvegarde / lecture des parametres
    private String SvgParametres = null;



    // Declaration des elements affiches

    //private TextView NomRubrique, MobileRubrique, PeriodeRubrique, PeriodeValeur;
    private EditText MobileValeur, NomValeur ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modifie_parametres);

        // MAJ et Afficher écran d'accueil

        // Cabler les rubriques

        //TextView Titre = findViewById(R.id.Titre);
        //TextView Texte = findViewById(R.id.Texte);
        // NomRubrique = findViewById(R.id.NomRubrique);
        NomValeur =  findViewById(R.id.NomValeur);
        // MobileRubrique =  findViewById(R.id.MobileRubrique);
        MobileValeur = findViewById(R.id.MobileValeur);
        //PeriodeRubrique = findViewById(R.id.PeriodeRubrique);
        TextView PeriodeValeur = findViewById(R.id.PeriodeValeur);
        Button g_bouton = findViewById(R.id.g_bouton);
        Button d_bouton = findViewById(R.id.d_bouton);

        // tampons de travail
        Toast msgT;
        String tmp;


        // Ajout modification Periode

        RadioGroup radiogroup;
        //RadioButton radio1, radio2, radio3 = new RadioButton(this);

        radiogroup = findViewById(R.id.radiogroup);
        //radio1 = findViewById(R.id.radio1);
        //radio2 = findViewById(R.id.radio2);
        //radio3 = findViewById(R.id.radio3);

        // relire le fichier via un buffer ... si le fichier existe

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


            // if (input != null ) {

            //   tmp = "buffer : " + lu.toString();
            //   msgT = Toast.makeText(this, tmp, Toast.LENGTH_LONG);
            //   msgT.show();

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

                Nom_aidant=DEFAUT_NOM_AIDANT;
                Numero_aidant = DEFAUT_NUMERO_AIDANT;
                Periode = DEFAUT_PERIODE ;
            }
            //}

            // fermeture du fichier
            input.close();
        }


        catch (FileNotFoundException e) {
            msgT = Toast.makeText(this,"File Not Found", Toast.LENGTH_SHORT);
            msgT.show();

            // MAP si ni Bundle ni fichier, on arrive chez moi
            Nom_aidant=DEFAUT_NOM_AIDANT;
            Numero_aidant = DEFAUT_NUMERO_AIDANT;
            Periode = DEFAUT_PERIODE ;

            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        // Affecter les valeurs aux champs affichés

        NomValeur.setText(Nom_aidant);
        MobileValeur.setText(Numero_aidant);
        PeriodeValeur.setText(valueOf(Periode));

        switch (Periode) {
            case 15:
                radiogroup.check(R.id.radio1);
                break;
            case 30:
                radiogroup.check(R.id.radio2);
                break;
            case 60:
                radiogroup.check(R.id.radio3);
                break;
        }

        // Mettre en place la surveillance de la saisie
        NomValeur.addTextChangedListener(textWatcher);
        MobileValeur.addTextChangedListener(textWatcher);

        // Mettre en place la surveillance des radiobuttons
        // les radiobuttons font appel a onButtonClickListener via le fichier XML

        // On tag les boutons de changement d'activite
        // On lance la surveillance du click
        // OnClick ci-après précise le traitement à réaliser

        g_bouton.setTag(0);
        d_bouton.setTag(1);
        g_bouton.setOnClickListener(this);
        d_bouton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        // user just clicked

        // declaration de variable
        Intent ActiviteSuivante;
        // Recuperation de l evenement - Tag fixe plus haut - gauche : 0 - droite : 1
        int valeurbouton = (int) view.getTag();
        switch (valeurbouton) {
            case 0:
                // MAP
                // String msg = "Bouton gauche";

                try {
                    FileInputStream input = openFileInput(NomFichier);
                    int value;
                    StringBuilder lu = new StringBuilder();
                    while ((value =input.read()) != -1) {
                        lu.append((char)value);
                    }
                    //  Toast msgT = Toast.makeText(this,"lu fichier" + lu.toString(), Toast.LENGTH_SHORT);
                    //  msgT.show();
                    //if (input != null )
                    input.close();
                }

                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                // Revenir à l'accueil => passe par le bundle
                //ActiviteSuivante = new Intent(ModifieParametres.this, MainActivity.class);
                //startActivity(ActiviteSuivante);
                finish();
                break;

            case 1:

                // On récupère les deux saisies
                Nom_aidant = NomValeur.getText().toString();
                // On enleve les accents
                Nom_aidant = Nom_aidant.replaceAll("[éè]","e");
                Numero_aidant = MobileValeur.getText().toString();

                if (estUnEntier(Numero_aidant) && (Numero_aidant.length() == 10)) {

                    // On les écrit dans le fichier
                    SvgParametres = Nom_aidant + "," + Numero_aidant + "," + Periode;

                    try {

                        FileOutputStream output = openFileOutput(NomFichier, MODE_PRIVATE);
                        output.write(SvgParametres.getBytes());

                        //if (output != null)
                        output.close();
                        //Toast msgT = Toast.makeText(this, "ecriture fichier", Toast.LENGTH_SHORT);
                        //msgT.show();
                        // On retourne à l'accueil par un intent et non finish) pour bénéficier de la MAJ si nécessaire
                        ActiviteSuivante = new Intent(ModifieParametres.this, MainActivity.class);
                        startActivity(ActiviteSuivante);
                        //finish();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast msgT = Toast.makeText(this, "le numero saisi ne comporte pas 10 chiffres", Toast.LENGTH_LONG);
                    msgT.show();
                }

                break;
        }   // fin du switch
    }   // fin de onClick


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()){
            case R.id.radio1:
                if (checked)
                    Periode = 15;
                break;
            case R.id.radio2:
                if (checked)
                    Periode = 30;
                break;
            case R.id.radio3:
                if (checked)
                    Periode = 60;
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Sauvegarde dans le bundle en cas de changement d'activite

        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_NOM_AIDANT, Nom_aidant );

        //Toast msgT = Toast.makeText(this, "Bundle "+ Nom_aidant, Toast.LENGTH_SHORT);
        //msgT.show();

        outState.putString(BUNDLE_NUMERO_AIDANT, Numero_aidant);
        outState.putString(BUNDLE_PERIODE, valueOf(Periode));
    }

    public TextWatcher textWatcher = new TextWatcher() {
        // Sert à capturer les EditText via getText
        public void onTextChanged (CharSequence s, int start, int before, int count) {
            //
        }
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after) {
            //
        }
        @Override
        public void afterTextChanged (Editable s) {
            //
        }
    };

    public boolean estUnEntier(String chaine) {
        try {
            Integer.parseInt(chaine);
        } catch (NumberFormatException e){
            return false;
        }

        return true;
    }

}
