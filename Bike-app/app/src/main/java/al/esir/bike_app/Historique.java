package al.esir.bike_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Historique extends ActionBarActivity implements OnClickListener,  AdapterView.OnItemSelectedListener {

    private Button btnConfirmHistorique;
    private Button btnClearHistorique;
    private Button btnRemoveHistorique;
    private Spinner spinnerHistorique;
    private Map<Integer, String> rowSelected;   // Map des lignes sélectionnées dans le tableau des historiques
    private TableLayout tableLayoutHistorique;  // Tableau des historique
    private Map<String, String> map;            // Map contenant les entrées du tableau des historiques
    private int rowCount = 1;                   // Compte le nombre de ligne dans le tableau des historiques
    private String file = "dataHistorique";     // Nom du fichier de sauvegarde
    private String fileContents = "";           // Contenu du fichier de sauvegarde

    public final static String DESTINATION_HISTORIQUE = "al.esir.bike_app.historique.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        btnConfirmHistorique = (Button) findViewById(R.id.btnConfirmHistorique);
        btnConfirmHistorique.setOnClickListener(this);

        btnClearHistorique = (Button) findViewById(R.id.btnClearHistorique);
        btnClearHistorique.setOnClickListener(this);

        btnRemoveHistorique = (Button) findViewById(R.id.btnRemoveHistorique);
        btnRemoveHistorique.setOnClickListener(this);

        spinnerHistorique = (Spinner) findViewById(R.id.spinnerHistorique);
        spinnerHistorique.setOnItemSelectedListener(this);
        spinnerHistorique.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        rowSelected = new LinkedHashMap<Integer, String>();
        tableLayoutHistorique = (TableLayout) findViewById(R.id.tableLayoutHistorique);
        map = new HashMap<String, String>();

        // On lit le contenu du fichier et on l'enregistre
        fileContents = readFile();
        // On initialise la structure du tableau des historiques
        initTable();
        // On analyse le contenu du fichier et on ajoute des entrées dans le tableau si besoin
        parseToAddToArray(fileContents);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_historique, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // On retourne un résultat différent en fonction du bouton cliqué par l'utilisateur
        switch(v.getId()){
            case R.id.btnConfirmHistorique :
                Intent intentNavigation = new Intent(this, NavigationActivity.class);
                if(rowSelected.size() > 0){
                    // On récupère la destination sélectionnée
                    String destination = getEntry(0).getValue();
                    // On envoie la destination à l'activité Navigation
                    intentNavigation.putExtra(DESTINATION_HISTORIQUE, destination);
                    // On lance l'activité Navigation
                    startActivity(intentNavigation);
                }
                else{
                    Toast.makeText(getBaseContext(), "Choose a destination", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnClearHistorique :
                clearTableHistorique();
                clearFile();
                initTable();
                break;
            case R.id.btnRemoveHistorique :
                for(Integer ent : rowSelected.keySet()){
                    removeByIndex(ent);
                    removeInFile(rowSelected.get(ent));
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On récupère la valeur sélectionnée dans le spinner
        String str = (String) spinnerHistorique.getSelectedItem().toString();
        // Si on doit afficher la totalité, alors on ne trie aucune colonne
        if(str.equals("Ce mois-ci")){showRows();}
        // Sinon on trie les colonnes en fonction de la valeur du spinner
        else{hideRows(str);}
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    /**
     * Permet de récupérer une entrée à la position absolu de la map rowSelected
     * @param id
     * @return Une entrée correspondant à la position demandée dans la map
     */
    private Map.Entry<Integer, String> getEntry(int id){
        Iterator<Map.Entry<Integer, String>> iterator = rowSelected.entrySet().iterator();
        int n = 0;
        while(iterator.hasNext()){
            Map.Entry entry = iterator.next();
            if(n == id){
                return entry;
            }
            n ++;
        }
        return null;
    }

    /**
     * Permet de cacher certaines colonnes de la liste des historiques en fonction du critère de recherche
     * @param search - Le critère de recherche basé sur les dates des dernières fois des historiques
     */
    public void hideRows(String search){
        // Pour toutes les colonnes du tableau, on applique le traitement
        for(int i = 1, j = tableLayoutHistorique.getChildCount(); i < j; i++){
            // On récupère une colonne en particulier
            View viewRows = tableLayoutHistorique.getChildAt(i);

            if(viewRows instanceof TableRow){
                TableRow row = (TableRow) viewRows;
                TextView firstTextView = (TextView) row.getChildAt(0);
                String firstText = firstTextView.getText().toString();

                if(!(firstText.equals(search))){
                    row.setVisibility(View.GONE);
                }
                else{
                    if(row.getVisibility() == View.GONE){
                        row.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    /**
     * Permet de rendre visible toutes les colonnes du tableau
     */
    public void showRows(){
        // Pour toutes les colonnes du tableau, on applique le traitement
        for(int i = 0, j = tableLayoutHistorique.getChildCount(); i < j; i++){
            // On récupère une colonne en particulier
            View viewRows = tableLayoutHistorique.getChildAt(i);
            // Si la colonne n'est pas visible
            if(viewRows.getVisibility() == View.GONE){
                if(viewRows instanceof TableRow){
                    TableRow row = (TableRow) viewRows;
                    // On rend visible la colonne
                    row.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Permet d'initialiser la structure du tableau des historiques
     */
    public void initTable(){
        // On initialise le TableLayout
        TableRow tbrow0 = new TableRow(this);
        tbrow0.setBackgroundColor(Color.parseColor("#92C94A"));

        // On crée la première colonne
        TextView tv0 = new TextView(this);
        tv0.setText("Dernière fois");
        tv0.setTextColor(Color.WHITE);
        tv0.setPadding(5,5,5,5);
        tbrow0.addView(tv0);
        // On crée la deuxième colonne
        TextView tv1 = new TextView(this);
        tv1.setText("Lieu");
        tv1.setTextColor(Color.WHITE);
        tv1.setPadding(5,5,5,5);
        tbrow0.addView(tv1);

        // On ajoute les modifications apportées
        tableLayoutHistorique.addView(tbrow0);
    }

    /**
     * Permet d'ajouter une nouvelle entrée dans le tableau des historiques
     * @param date - La date à laquelle on a réalisé un parcours vers ce lieu
     * @param lieu - Le lieu en question
     */
    public void addTableRow(String date, String lieu){
        // Si le lieu n'est pas déjà contenu dans la map, on l'ajoute
        if(!(map.containsKey(lieu))){
            TableRow tbrow = new TableRow(this);
            // On indique un id pour la ligne ajoutée
            tbrow.setId(0+rowCount);
            // On incrémente le nombre de ligne dans le tableau
            rowCount++;
            tbrow.setBackgroundColor(Color.parseColor("#6F9C33"));

            final String LieuIntoLine = lieu;   // Le lieu attendue pour la nouvelle ligne
            tbrow.setClickable(true);
            tbrow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // On récupère la couleur de la ligne
                    ColorDrawable rowColor = (ColorDrawable) v.getBackground();
                    int rowColorId = rowColor.getColor();
                    // Si la ligne a déjà été sélectionnée, on applique la couleur initial
                    if(rowColorId == Color.DKGRAY){
                        rowSelected.remove(v.getId()); // On le retire de la map des lignes sélectionnées
                        v.setBackgroundColor(Color.parseColor("#6F9C33"));
                    }else{
                        rowSelected.put(v.getId(), LieuIntoLine); // On l'ajoute à la liste des lignes sélectionnées
                        v.setBackgroundColor(Color.DKGRAY);
                    }
                }
            });

            TextView t1v = new TextView(this);
            t1v.setText(date);
            t1v.setTextColor(Color.WHITE);
            t1v.setPadding(5,5,5,5);
            tbrow.addView(t1v);

            TextView t2v = new TextView(this);
            t2v.setText(lieu);
            t2v.setTextColor(Color.WHITE);
            t2v.setPadding(5,5,5,5);
            tbrow.addView(t2v);

            // On ajoute l'entrée à la map
            map.put(lieu, date);

            // On vérifie que le lieu n'est pas déjà contenu dans le fichier
            if(!(fileContents.contains(lieu))){
                // On écrit dans le fichier la nouvelle donnée
                appendWriteFile(date, lieu);
            }

            // On ajoute une ligne dans le tableau
            tableLayoutHistorique.addView(tbrow);
        }
    }

    /**
     * Permet d'écrire dans le fichier de sauvegarde le contenu envoyé en paramètre
     * @param contents - Le contenu à écrire dans le fichier
     * @throws Exception - Propage une exception en cas d'erreur sur l'écriture dans le fichier
     */
    public void writeFile(String contents) throws Exception{
        FileOutputStream out = openFileOutput(file, Context.MODE_PRIVATE);
        // On réécrit le fichier avec les données en paramètre
        out.write(contents.getBytes());
        out.close();
    }

    /**
     * On ajoute la nouvelle entrée dans le fichier de sauvegarde en écrivant dedans
     * @param date - La date à laquelle on a réalisé un parcours vers ce lieu
     * @param lieu - Le lieu en question
     */
    public void appendWriteFile(String date, String lieu){
        try {
            // On sépare chaque entrée du tableau des historiques par \\.
            String elt = date+";"+lieu+"\\.";
            // On ajoute la nouvelle entrée au contenu du fichier
            fileContents += elt;
            // On écrit dans le fichier de sauvegarde
            writeFile(fileContents);

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
    public String readFile(){
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
     * Permet d'effacer le contenu du fichier de sauvegarde
     */
    public void clearFile(){
        try {
            // On réinitialise le contenu du fichier, puis on écrit dedans avec un contenu vide
            fileContents = "";
            writeFile(fileContents);
            Toast.makeText(getBaseContext(), "Data cleared", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e) {
            Toast.makeText(getBaseContext(), "Data don't cleared", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Permet de supprimer une donnée dans le fichier de sauvegarde
     * @param content - La donnée à supprimer du contenu du fichier de sauvegarde
     */
    public void removeInFile(String content){
        try{
            parseToReplaceToFileContents(content);  // On supprime du contenu du fichier
            writeFile(fileContents);                // On réécrit le fichier de sauvegarde
            Toast.makeText(getBaseContext(), "Data removed", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(getBaseContext(), "Data don't removed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    /**
     * Permet de parser un string (contenu du fichier de sauvegarde attendu) et
     * d'ajouter chaque élément distinct dans le tableau des historiques
     * @param result - Une chaine de caractère correspondant au contenu du fichier de sauvegarde
     */
    public void parseToAddToArray(String result){
        // On split à partir du séparateur $ pour chaque historique
        for(String elt : result.split("\\.")){
            String date; String lieu;
            int tmp = elt.indexOf(";");
            if(tmp != -1){
                date = elt.substring(0, tmp);
                lieu = elt.substring(tmp+1, elt.length()-1);
                // On ajoute l'élément au tableau des historiques
                addTableRow(date, lieu);
            }
        }
    }

    /**
     * Permet de parser un string (contenu du fichier de sauvegarde attendu) et
     * de remplacer la séquence de string recherché par rien (donc la supprimer du contenu du fichier)
     * @param content - La séquence de string à supprimer du contenu du fichier
     */
    public void parseToReplaceToFileContents(String content){
        for(String elt : fileContents.split("\\.")){
            if(elt.contains(content)){
                fileContents = fileContents.replace(elt, "");
            }
        }
    }

    /**
     * Permet de supprimer une ligne du tableau des historiques par rapport à l'index de cette dernière,
     * obtenu après sélection par l'utilisateur de la ligne concernée
     * @param index - L'index de la ligne à supprimer dans le tableau des historiques
     */
    public void removeByIndex(int index){
        View tmp = tableLayoutHistorique.findViewById(index);  // On récupère la vue
        tableLayoutHistorique.removeView(tmp);                 // On supprime la vue du tableau
    }

    // Permet de vider la table des historiques entièrement
    public void clearTableHistorique(){
        tableLayoutHistorique.removeAllViews();
    }
}
