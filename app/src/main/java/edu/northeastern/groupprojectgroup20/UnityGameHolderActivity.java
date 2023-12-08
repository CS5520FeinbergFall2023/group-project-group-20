package edu.northeastern.groupprojectgroup20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.player.UnityPlayerActivity;

import edu.northeastern.groupprojectgroup20.data.model.GameData;

public class UnityGameHolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unity_game_holder);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String UserID = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Game Data");
        Intent intent = new Intent(UnityGameHolderActivity.this, UnityPlayerActivity.class);
        reference.child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double atk = snapshot.child("atk").getValue(Double.class);
                Double def = snapshot.child("def").getValue(Double.class);
                Double hp = snapshot.child("hp").getValue(Double.class);
                if (atk != null && def != null && hp != null) {
                    intent.putExtra("atk", String.format("%.2f", atk));
                    intent.putExtra("def", String.format("%.2f", def));
                    intent.putExtra("hp", String.format("%.2f", hp));
                    intent.putExtra("userKey", UserID);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("tao", error.getMessage());
                finish();
            }
        });



    }
}