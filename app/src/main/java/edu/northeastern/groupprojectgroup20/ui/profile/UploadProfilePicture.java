package edu.northeastern.groupprojectgroup20.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import edu.northeastern.groupprojectgroup20.MainActivity;
import edu.northeastern.groupprojectgroup20.R;

public class UploadProfilePicture extends AppCompatActivity {

    ImageView imageViewUploadPic;
    Button upload, submit;

    FirebaseAuth authProfile;

    StorageReference storageReference;

    FirebaseUser firebaseUser;

    Uri uriImage;
    static final int Request_CHOOSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_picture);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Upload profile photo");

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        upload = findViewById(R.id.upload_pic_btn);
        submit = findViewById(R.id.submit_pic_btn);
        imageViewUploadPic = findViewById(R.id.profile_photo_preview);

        storageReference = FirebaseStorage.getInstance().getReference("ProfilePhoto");
        Uri uri = firebaseUser.getPhotoUrl();

        //
        Picasso.get().load(uri).into(imageViewUploadPic);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChoose();
            }
        });

        //submit

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPic();
            }
        });

    }

    private void uploadPic() {
        if(uriImage != null) {
            // save here
            StorageReference fileReference = storageReference.child(
                    authProfile.getCurrentUser().getUid()+"."+getFileExtensions(uriImage));
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                                    .Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileChangeRequest);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadProfilePicture.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UploadProfilePicture.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    Toast.makeText(UploadProfilePicture.this, "Upload success!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadProfilePicture.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                }
            });
        } else {
            Toast.makeText(UploadProfilePicture.this, "No file selected!", Toast.LENGTH_SHORT).show();

        }
    }

    private String getFileExtensions(Uri uriImage) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uriImage));
    }

    private void openChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,Request_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Request_CHOOSE && resultCode == RESULT_OK && data != null) {
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }
    }
}