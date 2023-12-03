package edu.northeastern.groupprojectgroup20.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import edu.northeastern.groupprojectgroup20.R;
import edu.northeastern.groupprojectgroup20.data.model.UserDetails;
import edu.northeastern.groupprojectgroup20.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    View root;

    EditText textView_show_mail, textView_profile_alias_name, textView_profile_dob,
            textView_profile_gender, textView_profile_weight, textView_profile_height;

    ProgressBar progressBar;

    String alis_name , userEmail , userDob, userGender, userWeight, userHeight;

    ImageView ProfileImageView;

    FirebaseAuth firebaseProfile;

    Button editContent, submitChange, conceal;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        ProfileViewModel profileViewModel =
//                new ViewModelProvider(this).get(ProfileViewModel.class);
//
//        binding = FragmentProfileBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        final TextView textView = binding.textProfile;
//        profileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        if(root == null) {
            root = inflater.inflate(R.layout.fragment_profile, container, false);
        }
        // init text view
        textView_show_mail =root.findViewById(R.id.textView_show_mail);
        textView_profile_alias_name = root.findViewById(R.id.textView_show_alias);
        textView_profile_dob = root.findViewById(R.id.textView_profile_dob);
        textView_profile_gender =root.findViewById(R.id.textView_profile_gender);
        textView_profile_weight = root.findViewById(R.id.textView_profile_weight);
        textView_profile_height = root.findViewById(R.id.textView_profile_height);
        ProfileImageView = root.findViewById(R.id.profile_photo);

        progressBar = root.findViewById(R.id.profile_progress_bar);

        editContent= root.findViewById(R.id.change_weight_or_height);
        submitChange = root.findViewById(R.id.submit_change_weight_or_height);
        conceal = root.findViewById(R.id.conceal_change_weight_or_height);

        firebaseProfile = FirebaseAuth.getInstance();

        if (savedInstanceState != null ){

        }

        FirebaseUser firebaseUser = firebaseProfile.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getActivity(), "something went wrong!!!", Toast.LENGTH_LONG).show();
        }else {
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

        editContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editContent.setVisibility(View.INVISIBLE);
                textView_profile_weight.setEnabled(true);
                textView_profile_height.setEnabled(true);
                textView_profile_dob.setEnabled(true);
                submitChange.setVisibility(View.VISIBLE);
                conceal.setVisibility(View.VISIBLE);
            }
        });

        conceal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_profile_weight.setEnabled(false);
                textView_profile_height.setEnabled(false);
                textView_profile_dob.setEnabled(false);
                submitChange.setVisibility(View.INVISIBLE);
                conceal.setVisibility(View.INVISIBLE);
                showUserProfile(firebaseUser);
            }
        });

        submitChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_profile_weight.setEnabled(false);
                textView_profile_height.setEnabled(false);
                submitChange.setVisibility(View.INVISIBLE);
                conceal.setVisibility(View.INVISIBLE);
                String weight, height, name,dob;
                weight = textView_profile_weight.getText().toString();
                height = textView_profile_height.getText().toString();
                dob = textView_profile_dob.getText().toString();

                name = textView_profile_alias_name.getText().toString();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Register Users");

                reference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserDetails userDetails = snapshot.getValue(UserDetails.class);
                        if(userDetails!=null) {
                            userDetails.height = height;
                            userDetails.weight = weight;
                            userDetails.dob = dob;
                            reference.child(firebaseUser.getUid()).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                        firebaseUser.updateProfile(profileChangeRequest);
                                        setProfileText(firebaseUser, userDetails);
                                        Toast.makeText(getActivity(), "Update success!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        ProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadProfilePicture.class);
                startActivity(intent);
            }
        });


        return root;
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userUid = firebaseUser.getUid();

        // Extracting User from database

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Register Users");
        reference.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails userDetails = snapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    setProfileText(firebaseUser,userDetails);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "something went wrong!!!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setProfileText(FirebaseUser firebaseUser , UserDetails userDetails){
        alis_name = firebaseUser.getDisplayName();
        userEmail = firebaseUser.getEmail();
        userGender = userDetails.gender;
        userDob = userDetails.dob;
        userHeight = userDetails.height;
        userWeight = userDetails.weight;
        textView_show_mail.setText(userEmail);
        textView_profile_dob.setText(userDob);
        textView_profile_alias_name.setText(alis_name);
        textView_profile_gender.setText(userGender);
        textView_profile_weight.setText(userWeight);
        textView_profile_height.setText(userHeight);

        Uri uri = firebaseUser.getPhotoUrl();
        Picasso.get().load(uri).into(ProfileImageView);

    }
}