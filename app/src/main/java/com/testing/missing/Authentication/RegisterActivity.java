package com.testing.missing.Authentication;

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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // Define EditText objects for all EditText's I need
    private EditText editTextRegisterFullName , editTextRegisterEmail , editTextRegisterDoB , editTextRegisterMobile , editTextRegisterPwd , editEditTextRegisterConfirmPwd;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;

    private DatePickerDialog picker;
    // define tag
    private  static final String TAG = "RegisterActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register");

        Toast.makeText(RegisterActivity.this , "You can register now ", Toast.LENGTH_LONG).show();
        //Get Firebase auth instance
        //auth = FirebaseAuth.getInstance();

        // Define All EditText Objects
        progressBar = findViewById(R.id.progress_bar_register);
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegisterPwd = findViewById(R.id.editText_register_password);
        editEditTextRegisterConfirmPwd = findViewById(R.id.editText_register_confirm_password);

        // RadioButton for Gender
        radioGroupRegisterGender = findViewById(R.id.radioGroup_register_gender);
        radioGroupRegisterGender.clearCheck();

        // Setting-up DatePicker on EditText
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth){
                        // setValues to editText birthOfDate
                        editTextRegisterDoB.setText(dayOfMonth+"/"+(month+1)+"/"+year);

                    }
                },year,month, day);
                picker.show();
            }
        });

        // Define Register Button
        Button buttonRegister = findViewById(R.id.button_register_);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                // Obtain the entered data by user

                String textFullName = editTextRegisterFullName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textMobile = editTextRegisterMobile.getText().toString();
                String textPwd = editTextRegisterPwd.getText().toString();
                String textConfirmPwd = editEditTextRegisterConfirmPwd.getText().toString();

                String textGender; // Can't obtain the value before verifying  if any button was selected or not

                /*
                * Validate mobile number using matcher and pattern ( Regular Expression)
                * First no, can be {6,8,9} and rest 9 nos. can be any no.
                * */
                /*String mobileRegex = "[0-5][6-9]{9}";

                Matcher mobileMatcher; // Create mobileMatcher Object from Class Matcher
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);*/


                /*
                 * Using TextUtils to check if user inserting his data or not
                 * TextUtils method always return boolean values
                 * */
                if(TextUtils.isEmpty(textFullName)){
                    // Check if user full name is empty or not
                    Toast.makeText(RegisterActivity.this , "Please enter your full name", Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Full Name is required ");
                    editTextRegisterFullName.requestFocus();
                } // end if

                else if(TextUtils.isEmpty(textEmail)){
                    // Check if user email is empty or not
                    Toast.makeText(RegisterActivity.this , "Please enter your email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email is required ");
                    editTextRegisterEmail.requestFocus();
                } // end else if
                /*
                 * Verifying if the email entered by user is valid or not
                 * Chek Using pattern matching
                 * */
                else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(RegisterActivity.this , "Please re-enter your email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid email is required ");
                    editTextRegisterEmail.requestFocus();
                } // end else if

                else if(TextUtils.isEmpty(textDoB)){
                    // Check if user full name is empty or not
                    Toast.makeText(RegisterActivity.this , "Please enter your date of birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDoB.setError(" is required ");
                    editTextRegisterDoB.requestFocus();
                } // end else if

                else if(radioGroupRegisterGender.getCheckedRadioButtonId() == -1){
                    Toast.makeText(RegisterActivity.this , "Please select your gender", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } // end else if

                else if(TextUtils.isEmpty(textMobile)){
                    Toast.makeText(RegisterActivity.this , "Please enter your mobile no.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile No. is required");
                    editTextRegisterMobile.requestFocus();
                } // end else if
                /*
                 * Check if mobile number that user entered is correct or not by check his length must equal 11
                 * */
                else if(textMobile.length() != 11){
                    Toast.makeText(RegisterActivity.this , "Please re-enter your mobile no.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile No. should be 11 digits");
                    editTextRegisterMobile.requestFocus();
                } // end else if

                // Check for mobile matcher
                /*else if(!mobileMatcher.find()){
                    Toast.makeText(RegisterActivity.this , "Please re-enter your mobile no.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile No. is not valid");
                    editTextRegisterMobile.requestFocus();
                }*/

                // Check for password
                else if(TextUtils.isEmpty(textPwd)){
                    Toast.makeText(RegisterActivity.this , "Please enter your password", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password is required");
                    editTextRegisterPwd.requestFocus();
                } // end else if
                /*
                 * Check if password that user entered is correct or not by check his length at-least should be 6 digits
                 *
                 * */
                else if(textPwd.length() < 6){
                    Toast.makeText(RegisterActivity.this , "Password at least should be 6 digits", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password too week");
                    editTextRegisterPwd.requestFocus();
                } // end else if

                // Check for Password Confirmation
                else if(TextUtils.isEmpty(textConfirmPwd)){
                    Toast.makeText(RegisterActivity.this , "Please confirm your password", Toast.LENGTH_LONG).show();
                    editEditTextRegisterConfirmPwd.setError("Confirmation password is required");
                    editEditTextRegisterConfirmPwd.requestFocus();
                } // ed else if
                else if(!textPwd.equals(textConfirmPwd)){
                    Toast.makeText(RegisterActivity.this , "Please enter same password", Toast.LENGTH_LONG).show();
                    editEditTextRegisterConfirmPwd.setError("Confirmation password is required");
                    editEditTextRegisterConfirmPwd.requestFocus();
                    //Clear entered password
                    editEditTextRegisterConfirmPwd.clearComposingText();

                } // end else if
                else{
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textFullName,textEmail,textDoB,textMobile,textPwd, textGender);
                }
                //progressBar.setVisibility(View.VISIBLE);


            }
        });

    }
    // Register user using the credentials given
    private void registerUser(String textFullName, String textEmail, String textDoB, String textMobile, String textPwd, String textGender) {
        FirebaseAuth auth = FirebaseAuth.getInstance(); // create auth object from firebase class
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email", Toast.LENGTH_LONG).show();
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // Update display the name of user
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    // Inserting User Data in Firebase RealTime Database
                    ReadWriteUserDetials writeUserDetails = new ReadWriteUserDetials(textDoB, textGender, textMobile);

                    // Extracting  User reference
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                // send verification email
                                firebaseUser.sendEmailVerification();
                                //Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email", Toast.LENGTH_LONG).show();

                                // Open user profile after successful registration
                                Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                //Use FLAG_ACTIVITY_CLEAR_TOP
                                //Prevent User from returning back to register activity or pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(RegisterActivity.this, "User registered failed. Please try again", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);


                        }
                    });

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        editTextRegisterPwd.setError("Your password is too weak. Use a mix of alphabets, numbers and special characters ");
                        editTextRegisterPwd.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextRegisterPwd.setError("Your email is invalid or already in use. Please re-enter");
                        editTextRegisterPwd.requestFocus();

                    } // end catch
                    catch (FirebaseAuthUserCollisionException e) {
                        editTextRegisterPwd.setError("User is already registered with this email. Use another email");
                        editTextRegisterPwd.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);

                } // end else
            }
        });
    }}