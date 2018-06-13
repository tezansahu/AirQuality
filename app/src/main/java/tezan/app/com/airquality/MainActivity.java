package tezan.app.com.airquality;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void searchClick(View view) {
        EditText enteredCity=(EditText)findViewById(R.id.city_name);
        String city=enteredCity.getText().toString();
        city=String.valueOf(city.charAt(0)).toUpperCase() + city.substring(1, city.length());
        city=city.replaceAll("\\s","%20");

        detailsofParam();

        String url="https://api.openaq.org/v1/latest?city="+city;


        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e,String data) {
                        processAirQualityData(data);
                    }
                });

    }
    /*
    {"meta":{"name":"openaq-api",
	 "license":"CC BY 4.0",
	 "website":"https://docs.openaq.org/",
	 "page":1,
	 "limit":100,
	 "found":4},

	 "results":[{"location":"Bandra, Mumbai - MPCB",
		     "city":"Mumbai",
		     "country":"IN",
		     "distance":11943289.80140025,
	             "measurements":[{"parameter":"so2",
				      "value":31,
				      "lastUpdated":"2018-03-31T05:30:00.000Z",
				      "unit":"µg/m³",
				      "sourceName":"data.gov.in",
				      "averagingPeriod":{"value":1,"unit":"hours"}},
				      {"parameter":....}
				    ]
		   {"location":...}
		  ]
}
     */

    private void processAirQualityData(String data){
        try{
            JSONObject json=new JSONObject(data);
            int count=json.getJSONObject("meta").getInt("found");
            if(count>0){
                JSONArray measurements=json.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONArray("measurements");

                LinearLayout results=(LinearLayout)findViewById(R.id.results_layout);
                results.removeAllViews();


                for(int i=0;i<measurements.length();i++){
                    //Extract parameters for the city
                    String parameter=measurements.getJSONObject(i).getString("parameter");
                    parameter=parameter.toUpperCase();
                    int value=measurements.getJSONObject(i).getInt("value");
                    String unit=measurements.getJSONObject(i).getString("unit");

                    //Display each parameter in a textview
                    TextView param_display=new TextView(this);
                    ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    param_display.setLayoutParams(params);
                    param_display.setTextSize(18);
                    param_display.setText(parameter+": "+value+unit);
                    param_display.setGravity(Gravity.CENTER);

                    results.addView(param_display);

                }

            }
            else{
                Toast.makeText(this,"Incorrect City Entered!! Please try again",Toast.LENGTH_SHORT).show();
            }

        }catch(JSONException e){
            Log.wtf("json",e);
        }
    }

    private void detailsofParam(){
        String url="https://api.openaq.org/v1/parameters";
        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e,String data) {
                        displayParamDetails(data);
                    }
                });
    }
    /*
    "results":
    [
         {
           "id": "pm25",
           "name": "PM2.5",
           "description": "Particulate matter less than 2.5 micrometers in diameter",
           "preferredUnit": "µg/m³"
         },
         {
           "id": "pm10",
           "name": "PM10",
           "description": "Particulate matter less than 10 micrometers in diameter",
           "preferredUnit": "µg/m³"
         },
         ...
        ]
     */

    private void displayParamDetails(String data){
        LinearLayout legend=(LinearLayout)findViewById(R.id.legend_display);
        legend.removeAllViews();

        try {
            JSONObject json = new JSONObject(data);
            JSONArray results = json.getJSONArray("results");
            for(int i=0;i<results.length();i++){

                String name=results.getJSONObject(i).getString("name");

                    String desc = results.getJSONObject(i).getString("description");

                    TextView param_display = new TextView(this);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    param_display.setLayoutParams(params);
                    param_display.setTextSize(10);
                    param_display.setText(name + ": " + desc);

                    legend.addView(param_display);



            }

        }catch(JSONException e){
            //do nothing
        }
    }
}
