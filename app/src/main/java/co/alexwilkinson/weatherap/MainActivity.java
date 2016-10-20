package co.alexwilkinson.weatherap;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText etQuery;
    TextView tvTitle, tvTemp,tvConditions;
    ImageView ivCondition;
    String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etQuery = (EditText)findViewById(R.id.etQuery);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvConditions = (TextView)findViewById(R.id.tvConditions);
        ivCondition = (ImageView)findViewById(R.id.ivCondition);
    }

    public void buSearch(View view) {
        String query = etQuery.getText().toString();
        title = query.trim().toUpperCase();
        query = query.replaceAll("\\s", "");
        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from" +
                "%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20" +
                "geo.places(1)%20where%20text%3D%22"+ query+"%22)" +
                "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        new MyAsyncTask().execute(url);

    }

    public class MyAsyncTask extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){

        }

        //work to do in background cant access UI
        @Override
        protected String doInBackground(String... params) {

            try{
                String weatherData;

                //define the url to connect with
                URL url = new URL(params[0]);

                //make a connection to the requested URL
                HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

                //set the time it will wait until cancelling the connection
                urlConnect.setConnectTimeout(5000);

                try{
                    //method to pull the data
                    InputStream in = new BufferedInputStream(urlConnect.getInputStream());

                    //convert the stream to a string using method below
                    weatherData = convertInputToString(in);

                    //send to display data
                    publishProgress(weatherData);
                }
                finally{
                    urlConnect.disconnect();
                }

            }
            catch(Exception ex){ex.getStackTrace();}

            return null;
        }

        //can access the UI from here
        protected void onProgressUpdate(String...progress){
            try{
                //display response data from JSON broken down
                JSONObject json = new JSONObject(progress[0]);
                JSONObject query = json.getJSONObject("query");
                JSONObject results = query.getJSONObject("results");
                JSONObject channel = results.getJSONObject("channel");
                JSONObject astronomy = channel.getJSONObject("astronomy");

                JSONObject condition = ((channel.getJSONObject("item")).getJSONObject("condition"));

                String sunset = astronomy.getString("sunset");
                String sunrise = astronomy.getString("sunrise");

                int temp = Integer.parseInt(condition.getString("temp"));
                temp = (temp-32) * 5/9;
                System.out.println(channel.getJSONObject("location"));


                String weatherCondition =(condition.getString("text")).toLowerCase();



                tvTitle.setText(title + ", " +((channel.getJSONObject("location")).getString("region")));
                tvTemp.setText(temp+"°C");
//                ivCondition.setImageResource(R.drawable.sun);

                if(weatherCondition.contains("cloud")
                        || weatherCondition.contains("rain")
                        || weatherCondition.contains("shower")){
                    ivCondition.setImageResource(R.drawable.rain);
                    System.out.println("true");
                }
                else{
                    ivCondition.setImageResource(R.drawable.sun);
                    System.out.print("false");
                }

                tvConditions.setText(condition.getString("text") + "\n" +
                        "Sunrise: " +sunrise + "\n" +
                        "Sunset: " +sunset + "\n"
                );
                etQuery.setText("");

//                Toast.makeText(getApplicationContext(), "Sunrise: "+sunrise +"\n Sunset: " +sunset,Toast.LENGTH_LONG).show();
            }
            catch(Exception ex){ ex.getStackTrace();}
        }

        protected void onPostExecute(String result2){

        }

    }

    //takes the stream and converts it to a string
    public static String convertInputToString(InputStream inputStream){

        BufferedReader buReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String total = "";

        try{
            while((line = buReader.readLine())!=null){

                total += line;
            }
            inputStream.close();
        }
        catch(Exception ex){
            ex.getStackTrace();
        }
        return total;
    }

}
