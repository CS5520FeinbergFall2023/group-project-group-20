package edu.northeastern.groupprojectgroup20.ui.history;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import edu.northeastern.groupprojectgroup20.R;
import edu.northeastern.groupprojectgroup20.adapter.HistoryListAdapter;
import edu.northeastern.groupprojectgroup20.data.model.HealthData;
import edu.northeastern.groupprojectgroup20.data.model.HistoryData;
import edu.northeastern.groupprojectgroup20.databinding.FragmentHistoryShowBinding;

public class HistoryFragment extends Fragment {

    private FragmentHistoryShowBinding binding;


    private RecyclerView recyclerView;

    private HistoryListAdapter mHistoryListAdapter;

    View root;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(root == null) {
            root = inflater.inflate(R.layout.fragment_history_show, container, false);
        }

        recyclerView = root.findViewById(R.id.history_recycle_view);

        mHistoryListAdapter = new HistoryListAdapter(getActivity());

        recyclerView.setAdapter(mHistoryListAdapter);

        // get required data
        FirebaseAuth firebaseProfile = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseProfile.getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("History")
                .child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                collectAllDate((Map<String,HealthData>) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mHistoryListAdapter.setListData(new ArrayList<HistoryData>());

        return root;
    }

    private void collectAllDate(Map<String, HealthData> value) {
        ArrayList<HistoryData> data = new ArrayList<>();
        for (Map.Entry<String, HealthData> entry : value.entrySet()){
            //Get value map
            Map singleDate = (Map) entry.getValue();
           // singleDate.get("lastUpdateTime");
            String calories = String.valueOf (singleDate.get("calories")).split("\\.")[0];

            String exercise = String.valueOf ( singleDate.get("exercise"));
            String lastUpdateTime = (String) singleDate.get("lastUpdateTime");
            String updateTime = lastUpdateTime.substring(0,8);
            String sleep = String.valueOf ( singleDate.get("sleep"));
            String steps = String.valueOf (singleDate.get("steps"));
            //Log.e(TAG, calories + " " +exercise + " "+lastUpdateTime + " "+sleep + " "+steps );
            data.add(new HistoryData(updateTime, calories, exercise, sleep , steps));

        }
        // sort List
        Collections.sort(data, new Comparator<HistoryData>() {
            @Override
            public int compare(HistoryData o1, HistoryData o2) {
               return  Integer.valueOf(o2.getLastUpdateTime()) -Integer.valueOf( o1.getLastUpdateTime()) ;
            }
        });

        mHistoryListAdapter.setListData(data);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}