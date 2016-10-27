package co.alexwilkinson.weatherap;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public EditText etQuery;
    public ImageView ivCondition;
    public String title = "";
    public ListView lvWeather;
    public ArrayList<WeatherItem> listNewData = new ArrayList<>();
    public Typeface iconFont;
    ListAdapter myadapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //selected the font awesome file
        iconFont = FontManager.getTypeface(getApplicationContext(),FontManager.FONTAWESOME);

        //create the query object and list view which will hold the weather
        lvWeather = (ListView)findViewById(R.id.lvWeather);
        etQuery = (EditText)findViewById(R.id.etQuery);
    }

    //The search button for the weather API that will call Yahoo!
    public void buSearch(View view) {
        String query = etQuery.getText().toString(); //pulls what the user has input
        title = query.trim().toUpperCase(); //remove the whitespace and make uppercase
        query = query.replaceAll("\\s", ""); //remove the spaces for the Yahoo HTTP request

        //the URL that will be executed
        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from" +
                "%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20" +
                "geo.places(1)%20where%20text%3D%22"+ query+"%22)" +
                "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        //the async task that will process the URL
        new MyAsyncTask().execute(url);

    }




    //Async class that does all the work
    public class MyAsyncTask extends AsyncTask<String, String, String> {

        //what happens first
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

                //find the condition data
                JSONObject condition = ((channel.getJSONObject("item")).getJSONObject("condition"));

                //get the sunset and sunrise data
                String sunset = astronomy.getString("sunset");
                String sunrise = astronomy.getString("sunrise");

                //convert the temperature to celsius
                int temp = Integer.parseInt(condition.getString("temp"));
                temp = (temp-32) * 5/9;

                //DEBUG check the output
                System.out.println(channel.getJSONObject("location"));

                //add details to the list to be displayed TITLE, TEMP, CONDITION
                listNewData.add(new WeatherItem(
                        title + ", " +((channel.getJSONObject("location")).getString("region")),
                        temp + "°C",
                        condition.getString("text") + "\n" +
                                "Sunrise: " +sunrise + "\n" +
                                "Sunset: " +sunset + "\n"));

                //create the custom adapter and add the arraylist to it
                myadapter = new ListAdapter(listNewData);
                lvWeather.setAdapter(myadapter);
                etQuery.setText("");

            }
            catch(Exception ex){ ex.getStackTrace();}
        }

        protected void onPostExecute(String result2){

        }

        //takes the stream and converts it to a string
        public String convertInputToString(InputStream inputStream){

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

    //list class used to display the information in a list format
    private class ListAdapter extends BaseAdapter{
        public ArrayList<WeatherItem> listNewData;

        public ListAdapter(ArrayList<WeatherItem> listNewData){
            this.listNewData = listNewData;
        }

        //returns size of the list
        @Override
        public int getCount() {
            return listNewData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //generates the list view
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            //pulls the weatherlist_layout to make it into a view to be used
            LayoutInflater myInflater = getLayoutInflater();
            View myView = myInflater.inflate(R.layout.weatherlist_layout,null);

            //get the item from the arraylist
            final WeatherItem myItem = listNewData.get(position);

            //create buttons
            //UPDATE BUTTON
            Button buUpdate = (Button)myView.findViewById(R.id.buUpdate);
            buUpdate.setTypeface(iconFont);
            buUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //update task
                }
            });

            //DELETE BUTTON
            Button buDelete = (Button)myView.findViewById(R.id.buDelete);
            buDelete.setTypeface(iconFont);
            buDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listNewData.remove(position);
                    myadapter.notifyDataSetChanged();

                }
            });

            //TEXTVIEW TITLE
            TextView tvTitle = (TextView)myView.findViewById(R.id.tvTitle);
            tvTitle.setText(myItem.title + " " +myItem.temp);

            //TEXTVIEW CONDITIONS
            TextView tvConditions = (TextView)myView.findViewById(R.id.tvConditions);
            tvConditions.setText(myItem.weatherCond);


            //IMAGEVIEW PROCESSING
            ivCondition = (ImageView)myView.findViewById(R.id.ivCondition);

            if(myItem.weatherCond.contains("Cloud")
                    || myItem.weatherCond.contains("rain")
                    || myItem.weatherCond.contains("shower")){
                ivCondition.setImageResource(R.drawable.rain);
            }
            else{
                ivCondition.setImageResource(R.drawable.sun);
            }



            return myView;
        }


    }

}
