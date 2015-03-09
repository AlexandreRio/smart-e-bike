package al.esir.bike_app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class AddFavoris extends Activity implements OnClickListener {

    EditText editTextAddFavoris1;
    EditText editTextAddFavoris2;
    Button btnOKAddFavoris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favoris);

        editTextAddFavoris1 = (EditText) findViewById(R.id.editTextAddFavoris1);
        editTextAddFavoris2 = (EditText) findViewById(R.id.editTextAddFavoris2);
        btnOKAddFavoris = (Button) findViewById(R.id.btnOKAddFavoris);
        btnOKAddFavoris.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        String[] tabResult = {editTextAddFavoris1.getText().toString(), editTextAddFavoris2.getText().toString()};
        intent.putExtra("result", tabResult);
        setResult(RESULT_OK, intent);
        // On met fin à l'activité
        finish();
    }

}
