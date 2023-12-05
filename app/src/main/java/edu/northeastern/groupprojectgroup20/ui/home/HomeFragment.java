package edu.northeastern.groupprojectgroup20.ui.home;

import static android.content.ContentValues.TAG;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.northeastern.groupprojectgroup20.R;
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

    TextView alias_name;

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

    public void callWebServiceWeatherHandler(){
        // open thread call api
        new Thread(new Runnable() {
            @Override
            public void run() {

//                // avoid app crashing with empty string
//                if (!input.getText().toString().equals("")) {
//                    // here call api
//                    String result =  NetUtil.getExchangeRate(selectedBaseCurrency,selectedTargetCurrency);
//                    // trans to main thread
//                    Message message = Message.obtain();
//                    message.what = 0;
//                    message.obj = result;
//                    mHandler.sendMessage(message);
//                }

            }
        }).start();
    }
    }
