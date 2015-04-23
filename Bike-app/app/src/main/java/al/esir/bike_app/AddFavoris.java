package al.esir.bike_app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddFavoris extends Activity implements OnClickListener {

    private EditText editTextAddFavoris1;   // EditText pour renseigner l'activité
    private EditText editTextAddFavoris2;   // EditText pour renseigner le lieu
    private Button btnOKAddFavoris;         // Bouton OK pour valider les données
    private Button btnAnnulerAddFavoris;    // Bouton Annuler pour annuler l'opération d'ajout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favoris);

        // On initialise les différentes éléments de la vue
        editTextAddFavoris1 = (EditText) findViewById(R.id.editTextAddFavoris1);
        editTextAddFavoris2 = (EditText) findViewById(R.id.editTextAddFavoris2);
        btnOKAddFavoris = (Button) findViewById(R.id.btnOKAddFavoris);
        btnAnnulerAddFavoris = (Button) findViewById(R.id.btnAnnulerAddFavoris);
        // On applique la méthode onClick au bouton OK et Annuler
        btnOKAddFavoris.setOnClickListener(this);
        btnAnnulerAddFavoris.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_favoris, menu);
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
            case R.id.btnOKAddFavoris :
                Intent intent = new Intent();
                // On crée la table de résultat qui comprendra l'activité et le lieu donné par l'utilisateur
                String[] tabResult = {editTextAddFavoris1.getText().toString(), editTextAddFavoris2.getText().toString()};
                intent.putExtra("result", tabResult);
                // On retourne le résultat avec le code OK
                setResult(RESULT_OK, intent);
                break;
            case R.id.btnAnnulerAddFavoris :
                // On retourne rien, seulement annuler
                setResult(RESULT_CANCELED);
                break;
        }
        // On met fin à l'activité
        finish();
    }

}
