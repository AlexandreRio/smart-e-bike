package al.esir.bike_app;

import android.graphics.Color;
import android.view.MenuItem;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Favoris extends Activity implements OnClickListener {

    Button btnAddFavoris;
    TableLayout tableLayoutFavoris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoris);

        btnAddFavoris = (Button) findViewById(R.id.btnAddFavoris);
        btnAddFavoris.setOnClickListener(this);
        tableLayoutFavoris = (TableLayout) findViewById(R.id.tableLayoutFavoris);

        init();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddFavoris.class);
        // Starting an activity and getting a result from this activity
        startActivityForResult(intent, 1);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String[] tabResult = data.getStringArrayExtra("result");
            addTableRow(tabResult[0], tabResult[1]);
        }
    }

    public void init(){
        // On initialise le TableLayout
        TableRow tbrow0 = new TableRow(this);
        tbrow0.setBackgroundColor(Color.parseColor("#92C94A"));

        TextView tv0 = new TextView(this);
        tv0.setText("Activité");
        tv0.setTextColor(Color.WHITE);
        tbrow0.addView(tv0);

        TextView tv1 = new TextView(this);
        tv1.setText("Lieu");
        tv1.setTextColor(Color.WHITE);
        tbrow0.addView(tv1);

        tableLayoutFavoris.addView(tbrow0);
    }

    public void addTableRow(String activite, String lieu){
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

        tableLayoutFavoris.addView(tbrow);
    }
}
