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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.northeastern.groupprojectgroup20.R;
import edu.northeastern.groupprojectgroup20.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    View root;
    private Double lat;
    private Double lon;
    String weather;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_home, container, false);
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // apply permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }


//        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
//
//        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//        // 2. Use Geocoder api, to fetch the address list based on your current lat, lng.
//
//        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
//        List<Address> addresses = null;
//        try {
//            addresses = gcd.getFromLocation(location.getLatitude(),
//                    location.getLongitude(), 1);
//            if()
//          lat =  location.getLatitude();
//           lon = location.getLongitude();
//           // get weather here
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        if (addresses.size() > 0) {
//            for (Address adr : addresses) {
//                if (adr != null && adr.getLocality().length() > 0) {
//                    String cityName = adr.getLocality();
//                    Log.e(TAG, "current city is :"+ cityName);
//                    break;
//                }
//            }
//        }
        return root;
    }

        @Override
        public void onDestroyView () {
            super.onDestroyView();
            binding = null;
        }
    }
