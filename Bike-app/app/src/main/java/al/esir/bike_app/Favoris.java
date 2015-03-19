package al.esir.bike_app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Favoris extends Activity implements OnClickListener {

    private Button btnAddFavoris;           // Bouton ajouter favoris
    private TableLayout tableLayoutFavoris; // Tableau des favoris
    private String file = "dataFavoris";    // Nom du fichier de sauvegarde
    private String fileContents = "";       // Contenu du fichier de sauvegarde
    private Map<String, String> map;        // Map contenant les entrées du tableau des favoris

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoris);

        // On récupère le bouton ajouter favoris puis on applique la fonction onClick
        btnAddFavoris = (Button) findViewById(R.id.btnAddFavoris);
        btnAddFavoris.setOnClickListener(this);
        // On récupère le tableau des favoris
        tableLayoutFavoris = (TableLayout) findViewById(R.id.tableLayoutFavoris);
        // Initialisation de la map
        map = new HashMap<String, String>();

        // On lit le contenu du fichier et on l'enregistre
        fileContents = read();
        // On initialise la structure du tableau des favoris
        initTable();
        // On analyse le contenu du fichier et on ajoute des entrées dans le tableau si besoin
        parseToAddToArray(fileContents);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddFavoris.class);
        // On lance une nouvelle activité et on récupère un résultat de cette activité
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Méthode qui recevra le résultat de l'activité lancé à partir du onClick
        // Si l'activité s'est exécuté sans accrochage
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // On récupère le résultat de l'activité
            String[] tabResult = data.getStringArrayExtra("result");
            // On ajoute ce résultat au tableau des favoris
            addTableRow(tabResult[0], tabResult[1]);
        }
    }

    /**
     * Permet d'initialiser la structure du tableau des favoris
     */
    public void initTable(){
        // On initialise le TableLayout
        TableRow tbrow0 = new TableRow(this);
        tbrow0.setBackgroundColor(Color.parseColor("#92C94A"));

        // On crée la première colonne
        TextView tv0 = new TextView(this);
        tv0.setText("Activité");
        tv0.setTextColor(Color.WHITE);
        tbrow0.addView(tv0);
        // On crée la deuxième colonne
        TextView tv1 = new TextView(this);
        tv1.setText("Lieu");
        tv1.setTextColor(Color.WHITE);
        tbrow0.addView(tv1);

        // On ajoute les modifications apportées
        tableLayoutFavoris.addView(tbrow0);
    }

    /**
     * Permet d'ajouter une nouvelle entrée dans le tableau des favoris
     * @param activite - L'activité liée au lieu
     * @param lieu - Le lieu en question
     */
    public void addTableRow(String activite, String lieu){
        // Si le lieu n'est pas déjà contenu dans la map, on l'ajoute
        if(!(map.containsKey(lieu))){
            TableRow tbrow = new TableRow(this);
            tbrow.setBackgroundColor(Color.parseColor("#6F9C33"));

            TextView t1v = new TextView(this);
            t1v.setText(activite);
            t1v.setTextColor(Color.WHITE);
            tbrow.addView(t1v);

            TextView t2v = new TextView(this);
            t2v.setText(lieu);
            t2v.setTextColor(Color.WHITE);
            tbrow.addView(t2v);

            // On ajoute l'entrée à la map
            map.put(lieu, activite);

            // On vérifie que le lieu n'est pas déjà contenu dans le fichier
            if(!(fileContents.contains(lieu))){
                // On écrit dans le fichier la nouvelle donnée
                appendWrite(activite, lieu);
            }

            // On ajoute une ligne dans le tableau
            tableLayoutFavoris.addView(tbrow);
        }
    }

    /**
     * On ajoute la nouvelle entrée dans le fichier de sauvegarde en écrivant dedans
     * @param activite - L'activité liée au lieu
     * @param lieu - Le lieu en question
     */
    public void appendWrite(String activite, String lieu){
        try {
            //FileOutputStream out = openFileOutput(file, Context.MODE_APPEND);
            FileOutputStream out = openFileOutput(file, Context.MODE_PRIVATE);

            // On sépare chaque entrée du tableau des favoris par \\.
            String elt = activite+";"+lieu+"\\.";
            // On ajoute la nouvelle entrée au contenu du fichier
            fileContents += elt;
            // On réécrit le fichier avec l'ensemble des données
            out.write(fileContents.getBytes());
            out.close();

            // On affiche un message via une popup
            Toast.makeText(getBaseContext(), "Data saved", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e) {
            Toast.makeText(getBaseContext(), "Data don't saved", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * On lit le fichier de sauvegarde et on retourne une chaine de caractère du contenu
     * @return Une chaine de caractère représentant le contenu du fichier de sauvegarde
     */
    public String read(){
        String ret = "";
        try {
            InputStream inputStream = openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Toast.makeText(getBaseContext(),"File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(),"Can not read file", Toast.LENGTH_SHORT).show();
        }

        return ret;
    }

    /**
     * Permet de parser un string (contenu du fichier de sauvegarde attendu) et
     * d'ajouter chaque élément distinct dans le tableau des favoris
     * @param result - Une chaine de caractère correspondant au contenu du fichier de sauvegarde
     */
    public void parseToAddToArray(String result){
        // On split à partir du séparateur $ pour chaque favoris
        for(String elt : result.split("\\.")){
            String act; String lieu;
            int tmp = elt.indexOf(";");
            if(tmp != -1){
                act = elt.substring(0, tmp);
                lieu = elt.substring(tmp+1, elt.length()-1);
                // On ajoute l'élément au tableau des favoris
                addTableRow(act, lieu);
            }
        }
    }
}
