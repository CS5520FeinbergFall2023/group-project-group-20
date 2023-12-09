package edu.northeastern.groupprojectgroup20.ui.login;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.northeastern.groupprojectgroup20.MainActivity;
import edu.northeastern.groupprojectgroup20.R;
import edu.northeastern.groupprojectgroup20.data.model.UserDetails;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword,editConfirmTextPassword,
            editTextFullName, editTextDob, editTextWeight, editTextHeight;
    TextView backToLogin;
    Button buttonRegister;
    FirebaseAuth mAuth;

    RadioGroup radioGroupRegisterGender;
    RadioButton radioButtonRegisterGenderSelected;
    ProgressBar registerProgressBar;

    DatePickerDialog picker;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
  //      FirebaseUser currentUser = mAuth.getCurrentUser();
        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        editTextDob = findViewById(R.id.register_dob);

        editTextDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Date picker
                picker = new DatePickerDialog(Register.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDob.setText(dayOfMonth+"/"+(month)+"/"+year);
                    }
                }, year,month, day);
                picker.show();
            }
        });

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);
                // obtain data

                String email, password, confirmPassword, fullName,dob, weight, height,gender;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                confirmPassword = editConfirmTextPassword.getText().toString();
                fullName = editTextFullName.getText().toString();
                dob = editTextDob.getText().toString();
                weight = editTextWeight.getText().toString();
                height = editTextHeight.getText().toString();


                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Please enter Email", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Register.this, "Re-Enter Email", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Email is Invalid");
                    editTextEmail.requestFocus();
                } else if (TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                }else if (!password.equals(confirmPassword)){
                    Toast.makeText(Register.this, "Password is not the same", Toast.LENGTH_SHORT).show();
                    editConfirmTextPassword.setError("Password is the same");
                    editConfirmTextPassword.requestFocus();
                    editTextPassword.clearComposingText();
                    editConfirmTextPassword.clearComposingText();

                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(Register.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    radioButtonRegisterGenderSelected.setError("Gender is the required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if(TextUtils.isEmpty(fullName)){
                    Toast.makeText(Register.this, "Please enter your alias-name", Toast.LENGTH_SHORT).show();
                    editTextFullName.setError("alias is the required");
                    editTextFullName.requestFocus();
                }else if(TextUtils.isEmpty(dob)){
                    Toast.makeText(Register.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
                    editTextDob.setError("date of birth is the required");
                    editTextDob.requestFocus();
                }else if(TextUtils.isEmpty(weight)){
                    Toast.makeText(Register.this, "Please enter your weight", Toast.LENGTH_SHORT).show();
                    editTextWeight.setError("weight is the required");
                    editTextWeight.requestFocus();
                }else if(TextUtils.isEmpty(height)){
                    Toast.makeText(Register.this, "Please enter your height", Toast.LENGTH_SHORT).show();
                    editTextHeight.setError("height is the required");
                    editTextHeight.requestFocus();
                } else if (password.length() <6) {
                    Toast.makeText(Register.this, "Password too short", Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Password too short");
                    editTextPassword.requestFocus();
                } else {
                    gender = radioButtonRegisterGenderSelected.getText().toString();
                    registerProgressBar.setVisibility(View.VISIBLE);
                    registerUser(email, password, confirmPassword, fullName,dob, weight, height,gender);
                }
            }
        });
    }

    private void registerUser(String email, String password, String confirmPassword, String fullName, String dob, String weight, String height, String gender) {

        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(fullName).build();
                            firebaseUser.updateProfile(profileChangeRequest);

                            // create account date
                            String strDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            String timeZone = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").getTimeZone().toString();

                            UserDetails readWriteUserDetails = new UserDetails( dob, gender, weight , height, strDate,timeZone);
                            // Extracting User reference from database for "register User"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Register Users");
                            referenceProfile.child(firebaseUser.getUid())
                                    .setValue(readWriteUserDetails)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                registerProgressBar.setVisibility(View.GONE);

                                                // add a listener
                                                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(Register.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                                                startActivity(intent);
                                                            }else{
                                                                Toast.makeText(Register.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            }else{
                                                Toast.makeText(Register.this, "Authentication Faild", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });



                        } else {
                            // If sign in fails, display a message to the user.
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                editTextPassword.setError("your password is too week, Kindly using a mix of alphabet，number " );
                                editTextPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextEmail.setError("your email is already in use or invalid " );
                                editTextEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                editTextEmail.setError("user is already registered with this email. Use other email " );
                                editTextEmail.requestFocus();
                            } catch ( Exception e) {
                                Log.e(TAG, e.getMessage() );
                            }
                            registerProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void init(){
        editTextEmail = findViewById(R.id.register_email);
        editTextPassword =findViewById(R.id.register_password);
        buttonRegister = findViewById(R.id.button_register_submit);
        registerProgressBar = findViewById(R.id.register_progress_bar);
        backToLogin = findViewById(R.id.register_back_login);
        editConfirmTextPassword =findViewById(R.id.register_password_2);
        editTextFullName = findViewById(R.id.register_full_name);
        editTextWeight = findViewById(R.id.register_weight);
        editTextHeight = findViewById(R.id.register_height);
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

    }
}