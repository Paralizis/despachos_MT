package cl.mundotour.intranet.mtdespachos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    ArrayList<HashMap<String, String>> detalleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_list);
        detalleList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        new getDetalles().execute();
    }


    public void showMap(View v) {
        View parentView = (View) v.getParent();
        Intent intent = new Intent(this, MapsActivity.class);
        TextView lattitude = (TextView) parentView.findViewById(R.id.latitude);
        TextView longitude = (TextView) parentView.findViewById(R.id.longitude);
        intent.putExtra("latitude", lattitude.getText().toString());
        intent.putExtra("longitude", longitude.getText().toString());
        startActivity(intent);
    }

    public void entregar(View v) {
        Intent intent = new Intent(this, EnviarActivity.class);
        startActivity(intent);
    }
    /**
     * Tarea asincrona para obtener el json de detalles en curso
     */
    private class getDetalles extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Muestra el modal de carga
            pDialog = new ProgressDialog(MainActivity.this, R.style.AppTheme_Dark_Dialog);
            pDialog.setMessage(getString(R.string.load_dialog));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Hace un request a la url y obtiene la respuesta
            String url = "https://api.androidhive.info/contacts/";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Obtiene el JSON :detalles
                    JSONArray detalles = jsonObj.getJSONArray("contacts");

                    // bucle por todos los detalles
                    for (int i = 0; i < detalles.length(); i++) {
                        JSONObject detalle = detalles.getJSONObject(i);

                        String id = detalle.getString("id");
                        String solicitante = detalle.getString("name");
                        String empresa = detalle.getString("email");
                        String direccion = detalle.getString("address");
                        String latitude = String.valueOf(-33.4092601);
                        String longitude = String.valueOf(-70.5722477);

                        // hash temporal de un detalle
                        HashMap<String, String> detalleHash = new HashMap<>();

                        // añade cada nodo del Json al hash
                        detalleHash.put("id", id);
                        detalleHash.put("solicitante", solicitante);
                        detalleHash.put("empresa", empresa);
                        detalleHash.put("direccion", direccion);
                        detalleHash.put("latitude", latitude);
                        detalleHash.put("longitude", longitude);


                        // se añade el detalle a la lista de detalles
                        detalleList.add(detalleHash);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Termina el modal de carga
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Coloca detalles en cada item de la lista de detalles
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, detalleList,
                    R.layout.listview_item, new String[]{"id","direccion", "solicitante",
                    "empresa","latitude","longitude"}, new int[]{R.id.detalle_id, R.id.direccion,
                    R.id.solicitante, R.id.empresa, R.id.latitude, R.id.longitude});
            lv.setAdapter(adapter);
        }

    }
}