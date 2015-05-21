package al.esir.bike_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class NavigationActivity extends Activity implements OnMapReadyCallback, LocationListener {

    private LocationManager locationManager;
    private String provider;
    private String destinationFavoris;      // Destination reçue par la vue Favoris
    private String destinationHistorique;   // Destination reçue par la vue Historique

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // On récupère l'intent qui nous a envoyé une donnée
        Intent intent = getIntent();
        // La destination reçue par la vue Favoris lorsqu'on valide une destination
        destinationFavoris = intent.getStringExtra(Favoris.DESTINATION_FAVORIS);
        // La destination reçue par la vue Historique lorsqu'on valide une destination
        destinationHistorique = intent.getStringExtra(Historique.DESTINATION_HISTORIQUE);
        /* !!! Il y a trois cas de figure possible : !!!
        * 1. Seule la destination des favoris est renseignée
        * 2. Seule la destination des historiques est renseignée
        * 3. Aucune des deux destinations n'est renseignée (elles sont toutes les deux à null)
        * */

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        locationManager.requestLocationUpdates(provider, 400, 1, this);
        Location location = locationManager.getLastKnownLocation(provider);



        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            LatLng here = new LatLng(location.getLatitude(), location.getLongitude());

            map.setMyLocationEnabled(true);
            CameraUpdate up = CameraUpdateFactory.newLatLngZoom(here, 17);
            map.moveCamera(up);
            CameraPosition.Builder pos = CameraPosition.builder(map.getCameraPosition()).tilt(45);
            CameraUpdate tilt = CameraUpdateFactory.newCameraPosition(pos.build());


            map.moveCamera(tilt);

            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            map.addMarker(new MarkerOptions()
                    .title("Here")
                    .snippet("This is where I am.")
                    .position(here));
        } else {
           System.out.println("location not available");
        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
