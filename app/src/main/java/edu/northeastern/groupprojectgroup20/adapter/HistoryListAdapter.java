package edu.northeastern.groupprojectgroup20.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.groupprojectgroup20.R;
import edu.northeastern.groupprojectgroup20.data.model.HealthData;
import edu.northeastern.groupprojectgroup20.data.model.HistoryData;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.Myholder> {

    private  List<HistoryData> mdataList = new ArrayList<>();

    private Context mContext;

    public  void setListData(List<HistoryData> listData) {
        this.mdataList = listData;
        // notify data changed
        notifyDataSetChanged();
    }

    public HistoryListAdapter (Context context) {
        this.mContext = context;

    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // init layout
       View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item, null);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        // bind data
        HistoryData healthData = mdataList.get(position);
        String date = healthData.getLastUpdateTime();
        String year = date.substring(0,4) ;
        String month = date.substring(4,6);
        String day = date.substring(6,8);
        String newDate = day + "/" + month + "/" + year;
        holder.textView_history_date.setText(newDate);
        holder.textView_history_calories.setText(String.valueOf( healthData.getCalories()));
        holder.textView_history_exercise.setText(String.valueOf(healthData.getExercise()));
        holder.textView_history_sleep.setText(String.valueOf(healthData.getSleep()));
        holder.textView_history_steps.setText(String.valueOf(healthData.getSteps()));
    }

    @Override
    public int getItemCount() {
        return mdataList.size();
    }

    static class Myholder extends  RecyclerView.ViewHolder{

        TextView textView_history_date ;
        TextView textView_history_calories ;
        TextView textView_history_exercise ;
        TextView textView_history_sleep ;
        TextView textView_history_steps ;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            textView_history_date = itemView.findViewById(R.id.history_date);
            textView_history_calories = itemView.findViewById(R.id.history_calories);
            textView_history_exercise = itemView.findViewById(R.id.history_exercise);
            textView_history_sleep = itemView.findViewById(R.id.history_sleep);
            textView_history_steps = itemView.findViewById(R.id.history_steps);
        }
    }

}
