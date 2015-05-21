package al.esir.bike_app;

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
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Favoris extends ActionBarActivity implements OnClickListener, AdapterView.OnItemSelectedListener {

    private Button btnConfirmFavoris;       // Bouton valider favoris
    private Button btnAddFavoris;           // Bouton ajouter favoris
    private Button btnClearFavoris;         // Bouton clear favoris
    private Button btnRemoveFavoris;        // Bouton remove favoris
    private Spinner spinnerFavoris;         // Spinner favoris
    private TableLayout tableLayoutFavoris; // Tableau des favoris
    private String file = "dataFavoris";    // Nom du fichier de sauvegarde
    private String fileContents = "";       // Contenu du fichier de sauvegarde
    private Map<String, String> map;        // Map contenant les entrées du tableau des favoris
    private int rowCount = 1;               // Compte le nombre de ligne dans le tableau des favoris
    private Map<Integer, String> rowSelected; // Map des lignes sélectionnées dans le tableau des favoris

    public final static String DESTINATION_FAVORIS = "al.esir.bike_app.favoris.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoris);

        // On récupère le bouton ajouter favoris puis on applique la fonction onClick
        btnAddFavoris = (Button) findViewById(R.id.btnAddFavoris);
        btnAddFavoris.setOnClickListener(this);
        // Même chose pour le bouton clear
        btnClearFavoris = (Button) findViewById(R.id.btnClearFavoris);
        btnClearFavoris.setOnClickListener(this);
        // Même chose pour le bouton remove
        btnRemoveFavoris = (Button) findViewById(R.id.btnRemoveFavoris);
        btnRemoveFavoris.setOnClickListener(this);
        // Même chose pour le bouton valider
        btnConfirmFavoris = (Button) findViewById(R.id.btnConfirmFavoris);
        btnConfirmFavoris.setOnClickListener(this);
        // On récupère le spinner favoris puis on applique la fonction onItemSelected
        spinnerFavoris = (Spinner) findViewById(R.id.spinnerFavoris);
        spinnerFavoris.setOnItemSelectedListener(this);
        spinnerFavoris.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        // On récupère le tableau des favoris
        tableLayoutFavoris = (TableLayout) findViewById(R.id.tableLayoutFavoris);
        // Initialisation de la map
        map = new HashMap<String, String>();
        // Initialisation de la liste
        rowSelected = new LinkedHashMap<Integer, String>();

        // On lit le contenu du fichier et on l'enregistre
        fileContents = readFile();
        // On initialise la structure du tableau des favoris
        initTable();
        // On analyse le contenu du fichier et on ajoute des entrées dans le tableau si besoin
        parseToAddToArray(fileContents);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favoris, menu);
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
            case R.id.btnAddFavoris :
                Intent intent = new Intent(this, AddFavoris.class);
                // On lance une nouvelle activité et on récupère un résultat de cette activité
                startActivityForResult(intent, 1);
                break;
            case R.id.btnClearFavoris :
                clearTableFavoris();
                clearFile();
                initTable();
                break;
            case R.id.btnRemoveFavoris :
                for(Integer ent : rowSelected.keySet()){
                    removeByIndex(ent);
                    removeInFile(rowSelected.get(ent));
                }
                break;
            case R.id.btnConfirmFavoris :
                Intent intentNavigation = new Intent(this, NavigationActivity.class);
                if(rowSelected.size() > 0){
                    String destination = getEntry(0).getValue();
                    intentNavigation.putExtra(DESTINATION_FAVORIS, destination);
                    startActivity(intentNavigation);
                }
                else{
                    Toast.makeText(getBaseContext(), "Choose a destination", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Permet de récupérer une entrée à la position absolu de la map rowSelected
     * @param id
     * @return
     */
    private Entry<Integer, String> getEntry(int id){
        Iterator<Entry<Integer, String>> iterator = rowSelected.entrySet().iterator();
        int n = 0;
        while(iterator.hasNext()){
            Entry entry = iterator.next();
            if(n == id){
                return entry;
            }
            n ++;
        }
        return null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On récupère la valeur sélectionnée dans le spinner
        String str = (String) spinnerFavoris.getSelectedItem().toString();
        // Si on doit afficher la totalité, alors on ne trie aucune colonne
        if(str.equals("Tout")){showRows();}
        // Sinon on trie les colonnes en fonction de la valeur du spinner
        else{hideRows(str);}
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    /**
     * Permet de cacher certaines colonnes de la liste des favoris en fonction du critère de recherche
     * @param search - Le critère de recherche basé sur les catégories des favoris
     */
    public void hideRows(String search){
        // Pour toutes les colonnes du tableau, on applique le traitement
        for(int i = 1, j = tableLayoutFavoris.getChildCount(); i < j; i++){
            // On récupère une colonne en particulier
            View viewRows = tableLayoutFavoris.getChildAt(i);

            if(viewRows instanceof TableRow){
                TableRow row = (TableRow) viewRows;
                TextView firstTextView = (TextView) row.getChildAt(0);
                String firstText = firstTextView.getText().toString();
                // On rend la colonne invisible si sa catégorie ne fait pas partie du critère
                if(!(firstText.equals(search))){
                    row.setVisibility(View.GONE);
                }
                else{
                    // Si une colonne est correct mais invisible, on l'affiche
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
        for(int i = 0, j = tableLayoutFavoris.getChildCount(); i < j; i++){
            // On récupère une colonne en particulier
            View viewRows = tableLayoutFavoris.getChildAt(i);
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
        tv0.setPadding(5,5,5,5);
        tbrow0.addView(tv0);
        // On crée la deuxième colonne
        TextView tv1 = new TextView(this);
        tv1.setText("Lieu");
        tv1.setTextColor(Color.WHITE);
        tv1.setPadding(5,5,5,5);
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
                    //Toast.makeText(getApplicationContext(), "Row selected "+rowSelected, Toast.LENGTH_LONG).show();

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
            t1v.setText(activite);
            t1v.setTextColor(Color.WHITE);
            t1v.setPadding(5,5,5,5);
            tbrow.addView(t1v);

            TextView t2v = new TextView(this);
            t2v.setText(lieu);
            t2v.setTextColor(Color.WHITE);
            t2v.setPadding(5,5,5,5);
            tbrow.addView(t2v);

            // On ajoute l'entrée à la map
            map.put(lieu, activite);

            // On vérifie que le lieu n'est pas déjà contenu dans le fichier
            if(!(fileContents.contains(lieu))){
                // On écrit dans le fichier la nouvelle donnée
                appendWriteFile(activite, lieu);
            }

            // On ajoute une ligne dans le tableau
            tableLayoutFavoris.addView(tbrow);
        }
    }

    /**
     * Permet d'écrire dans le fichier de sauvegarde le contenu envoyé en paramètre
     * @param contents - Le contenu à écrire dans le fichier
     * @throws Exception - Propage une exception en cas d'erreur sur l'écriture dans le fichier
     */
    public void writeFile(String contents) throws Exception{
            //FileOutputStream out = openFileOutput(file, Context.MODE_APPEND);
            FileOutputStream out = openFileOutput(file, Context.MODE_PRIVATE);
            // On réécrit le fichier avec les données en paramètre
            out.write(contents.getBytes());
            out.close();
    }

    /**
     * On ajoute la nouvelle entrée dans le fichier de sauvegarde en écrivant dedans
     * @param activite - L'activité liée au lieu
     * @param lieu - Le lieu en question
     */
    public void appendWriteFile(String activite, String lieu){
        try {
            // On sépare chaque entrée du tableau des favoris par \\.
            String elt = activite+";"+lieu+"\\.";
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
     * Permet de supprimer une ligne du tableau des favoris par rapport à l'index de cette dernière,
     * obtenu après sélection par l'utilisateur de la ligne concernée
     * @param index - L'index de la ligne à supprimer dans le tableau des favoris
     */
    public void removeByIndex(int index){
        View tmp = tableLayoutFavoris.findViewById(index);  // On récupère la vue
        tableLayoutFavoris.removeView(tmp);                 // On supprime la vue du tableau

        // Alternative :
        // TableRow row = (TableRow) tableLayoutFavoris.getChildAt(index);
        // tableLayoutFavoris.removeView(row);
    }

    // Permet de vider la table des favoris entièrement
    public void clearTableFavoris(){
        tableLayoutFavoris.removeAllViews();
    }
}
