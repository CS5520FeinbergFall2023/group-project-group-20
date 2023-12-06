package edu.northeastern.groupprojectgroup20.ui.home;

import static android.content.ContentValues.TAG;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.northeastern.groupprojectgroup20.R;
import edu.northeastern.groupprojectgroup20.data.model.GameData;
import edu.northeastern.groupprojectgroup20.util.NetUtil;
import edu.northeastern.groupprojectgroup20.data.model.UserDetails;
import edu.northeastern.groupprojectgroup20.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    View root;
    private Double lat;
    private Double lon;
    String weather;

    String gender;
    FirebaseAuth firebaseProfile;
    ImageView gender_photo;
    ImageView weather_photo;
    TextView alias_name;
    TextView city_content;
    TextView HPValue;
    TextView ATKValue;
    TextView DEFValue;
    private Handler mHandler = new Handler(Looper.myLooper()){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                String result = (String) msg.obj;
                String [] results = result.split(",");
              String w= results[3];
               //  "main":"Clear"
                String weather = w.split(":")[1].replace("\"","");
               // "icon":"01d"}],
                String i = results[5];
                String id = i.split(":")[1]
                        .replace("\"","")
                        .replace("}","")
                        .replace("]","");
                String t = results[7];

              String te = t.split(":")[2]
                      .replace("\"","")
                      .replace(",","");
                String temp = (int)((Double.valueOf(te)-273.15) *1.8 +32) +"";
              String city = results[results.length-2].split(":")[1].replace("\"","");

              city_content.setText(city + ", " + weather + ", " + temp +"°F" );
                Picasso.get().load("https://openweathermap.org/img/wn/"+id+"@2x.png").into(weather_photo);
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_home, container, false);
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // apply permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        gender_photo = root.findViewById(R.id.home_page_gender);
        alias_name = root.findViewById(R.id.homepage_alias);
        HPValue = root.findViewById(R.id.textViewHP);
        ATKValue = root.findViewById(R.id.textViewATK);
        DEFValue = root.findViewById(R.id.textViewDEF);
        weather_photo = root.findViewById(R.id.photo_weather_city_tem);
        city_content = root.findViewById(R.id.weather_city_tem);

        // get user
        firebaseProfile = FirebaseAuth.getInstance();


        FirebaseUser firebaseUser = firebaseProfile.getCurrentUser();

        // get data
        if (firebaseUser == null) {
            Toast.makeText(getActivity(), "something went wrong!!!", Toast.LENGTH_LONG).show();
        }else {
            // get object
            String userUid = firebaseUser.getUid();

            // Extracting User from database
            alias_name.setText(firebaseUser.getDisplayName());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Register Users");
            reference.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);
                    if (userDetails != null) {
                        gender = userDetails.gender;
                        if(gender.equals("female")){
                            gender_photo.setBackgroundResource(R.drawable.female);
                        }else gender_photo.setBackgroundResource(R.drawable.male);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference referenceGame = FirebaseDatabase.getInstance().getReference("Game Data");
            referenceGame.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    GameData gameData = snapshot.getValue(GameData.class);
                    if (gameData != null) {
                        double HP = gameData.getHP();
                        HPValue.setText(String.valueOf((int) HP));
                        double ATK = gameData.getATK();
                        ATKValue.setText(String.valueOf((int) ATK));
                        double DEF = gameData.getDEF();
                        DEFValue.setText(String.valueOf((int) DEF));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }


        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // 2. Use Geocoder api, to fetch the address list based on your current lat, lng.

        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
          lat =  location.getLatitude();
           lon = location.getLongitude();
           // get weather here using Thread
            String [] newLats = lat.toString().split("\\.");
            String [] newLons = lon.toString().split("\\.");
            String newLat = newLats[0]+"."+newLats[1].substring(0,2);
            String newLon = newLons[0]+"."+newLons[1].substring(0,2);
            callWebServiceWeatherHandler(newLat,newLon);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (addresses.size() > 0) {
            for (Address adr : addresses) {
                if (adr != null && adr.getLocality().length() > 0) {
                    String cityName = adr.getLocality();
                  //  Log.e(TAG, "current city is :"+ cityName);
                    break;
                }
            }
        }


        return root;
    }

        @Override
        public void onDestroyView () {
            super.onDestroyView();
            binding = null;
        }

    public void callWebServiceWeatherHandler(String lat, String lon){
        // open thread call api
        new Thread(new Runnable() {
            @Override
            public void run() {

                    // here call api
                    String result =  NetUtil.getWeather(lat,lon);
                    // trans to main thread
                    Message message = Message.obtain();
                    message.what = 0;
                    message.obj = result;
                    mHandler.sendMessage(message);
                }
        }).start();
    }
    }
