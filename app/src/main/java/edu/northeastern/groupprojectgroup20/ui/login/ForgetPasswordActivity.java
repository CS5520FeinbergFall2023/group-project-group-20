package edu.northeastern.groupprojectgroup20.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import edu.northeastern.groupprojectgroup20.MainActivity;
import edu.northeastern.groupprojectgroup20.R;

public class ForgetPasswordActivity extends AppCompatActivity {


    Button btn_pwd_reset;
    EditText editTextPwdRestEmail;

    FirebaseAuth authProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        getSupportActionBar().setTitle("forget password");
        editTextPwdRestEmail = findViewById(R.id.edit_text_for_rest_email);
        btn_pwd_reset = findViewById(R.id.button_password_reset);

        btn_pwd_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextPwdRestEmail.getText().toString();

                if(TextUtils.isEmpty(email)){
                    editTextPwdRestEmail.setError("Email is required");
                    editTextPwdRestEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextPwdRestEmail.setError("Email is not valid");
                    editTextPwdRestEmail.requestFocus();
                } else {
                    restPassword(email);
                }
            }
        });

    }

    private void restPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgetPasswordActivity.this, "Please check your inbox for password rest link", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgetPasswordActivity.this, MainActivity.class);

                    startActivity(intent);

                    finish();
                } else {
                    try {
                        throw  task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextPwdRestEmail.setError("user do not match our record!");
                    } catch (Exception e){
                        editTextPwdRestEmail.setError("Something went wrong!");
                    }
                }
            }
        });
    }
}