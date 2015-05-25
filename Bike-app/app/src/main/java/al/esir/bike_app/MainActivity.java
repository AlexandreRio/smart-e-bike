package al.esir.bike_app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements OnClickListener {
// main

    private EditText editTextMain;
    private Button btnOkMain;
    private String file = "dataHistorique";     // Nom du fichier de sauvegarde
    private String fileContents = "";           // Contenu du fichier de sauvegarde
    public final static String DESTINATION_MAIN = "al.esir.bike_app.main.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnOkMain = (Button) findViewById(R.id.btnOKMain);
        this.btnOkMain.setOnClickListener(this);
        this.editTextMain = (EditText) findViewById(R.id.textFieldHome);

        // On lit le contenu du fichier et on l'enregistre
        fileContents = readFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        switch(v.getId()){
            case R.id.btnOKMain :
                Intent intentNavigation = new Intent(this, NavigationActivity.class);
                String destination = this.editTextMain.getText().toString();
                if(destination.length() > 0){
                    // On ajoute au fichier de sauvegarde la destination courante
                    appendWriteFile(getFormattedDate(), destination);
                    // On envoie la destination à l'activité Navigation
                    intentNavigation.putExtra(DESTINATION_MAIN, destination);
                    // On lance l'activité Navigation
                    startActivity(intentNavigation);
                }
                else{
                    Toast.makeText(getBaseContext(), "Veuillez donner un lieu", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void launchFavorisActivity(View view){
        Intent intent = new Intent(this, Favoris.class);
        startActivity(intent);
    }

    public void launchNavigationActivity(View view){
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    public void launchHistoriqueActivity(View view){
        Intent intent = new Intent(this, Historique.class);
        startActivity(intent);
    }

    public void launchSettingsActivity(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    /**
     * @return La date courante sous la forme JJ/MM/AA HH:MM
     */
    public static String getFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(); //called without pattern
        return df.format(c.getTime());
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
            //Toast.makeText(getBaseContext(),"File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(),"Can not read file", Toast.LENGTH_SHORT).show();
        }

        return ret;
    }

    /**
     * On ajoute la nouvelle entrée dans le fichier de sauvegarde en écrivant dedans
     * @param date - La date à laquelle nous avons réalisé le parcours
     * @param lieu - Le lieu en question
     */
    public void appendWriteFile(String date, String lieu){
        try {
            // On sépare chaque entrée par \\.
            String elt = date+";"+lieu+"\\.";
            // On ajoute la nouvelle entrée au contenu du fichier
            fileContents += elt;
            // On écrit dans le fichier de sauvegarde
            writeFile(fileContents);
        }
        catch(Exception e) {
            //Toast.makeText(getBaseContext(), "Data don't saved", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
