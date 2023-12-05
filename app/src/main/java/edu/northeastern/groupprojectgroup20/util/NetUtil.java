package edu.northeastern.groupprojectgroup20.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is used to fetch exchange rate data from an external API.
 */
public class NetUtil {

    /**
     * This method performs an HTTP GET request to a given URL and returns the response data as a string.
     * @param urlStr
     * @return
     */
    public static String doGet(String urlStr) {
        String result = "";
        // connection
        HttpURLConnection connection = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            // read data
            InputStream inputStream = connection.getInputStream() ;
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line ="";
            StringBuilder sb = new StringBuilder();
            while ( (line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString().replace(",",",\n");


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connection!= null) {connection.disconnect();}
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader!= null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * The getHistory method constructs a URL for fetching historical exchange rate data
     * and calls doGet to retrieve and return the data.
     * @return
     */

    public static String getWeather(String lat, String lon){
        String exchangeRateUrl = EXCHANGE_URL + lat +"&lon="+lon +"&appid="+API_KEY;
        return doGet(exchangeRateUrl);
       // System.out.println(result);

    }
    static String API_KEY = "b25fc08da39d4f20fcf28d0b679c6561";
    static String EXCHANGE_URL = "https://api.openweathermap.org/data/2.5/weather?lat=";

}
