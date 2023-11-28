package edu.northeastern.groupprojectgroup20.ui.login;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.groupprojectgroup20.MainActivity;
import edu.northeastern.groupprojectgroup20.R;

public class Login extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    Button loginSubmit, registerSubmit;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        loginEmail = findViewById(R.id.loginInputEmail);
        loginPassword = findViewById(R.id.loginInputPassword);
        loginSubmit = findViewById(R.id.button_login);
        registerSubmit= findViewById(R.id.button_create_account);
        mAuth = FirebaseAuth.getInstance();
        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = loginEmail.getText().toString();
                password = loginPassword.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(password)){
                    Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser!=null && firebaseUser.isEmailVerified()){
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mAuth.signOut();
                                                showAlertDialog();
                                            }
                                        });

                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                 try {
                                     throw task.getException();

                                 } catch ( FirebaseAuthInvalidUserException e){
                                     loginEmail.setError("User does not exists or is no longer valid. Please register again");
                                     loginEmail.requestFocus();
                                 } catch (FirebaseAuthInvalidCredentialsException e){
                                     loginEmail.setError("Invalid credentials. kindly, check and re-enter.");
                                     loginEmail.requestFocus();
                                 } catch (Exception e) {
                                     Log.e(TAG, e.getMessage());
                                 }
                                }
                            }
                        });
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle("Email is not verified");
        builder.setMessage("Please verify your email now. You can not login without email verification");
        // Open Email Apps if user clicks
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To email app in the new window
                startActivity(intent);
            }
        });

        // create AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}