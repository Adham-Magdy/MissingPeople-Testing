package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

import java.util.Calendar;

public class UpdateProfileActivity extends AppCompatActivity {

    // Define values
    private EditText editTextUpdateName , editTextUpdateDoB , editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName , textDoB , textGender , textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        getSupportActionBar().setTitle("Profile Settings");

        progressBar = findViewById(R.id.progress_bar_update_profile);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);
        radioGroupUpdateGender = findViewById(R.id.radioGroup_update_profile_gender);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // Show Profile Data
        showProfile(firebaseUser);

        // Upload Profile Picture
        Button buttonUploadProfilePic = findViewById(R.id.button_update_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateProfileActivity.this,UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Update Email Activity
        Button buttonUpdateEmail = findViewById(R.id.button_update_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Update BirthOfDate
        // Setting-up DatePicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Extracting saved dd,m,yyyy into different variables by creating an array delimited by "/"
                String textSABoB[] = textDoB.split("/");

                int day = Integer.parseInt(textSABoB[0]); // convert integer data into string type and store in an array
                int month = Integer.parseInt(textSABoB[1])-1;
                int year = Integer.parseInt(textSABoB[2]);

                DatePickerDialog picker;
                // Date picker dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth){
                        // setValues to editText birthOfDate
                        editTextUpdateDoB.setText(dayOfMonth+"/"+(month+1)+"/"+year);

                    }
                },year,month, day);
                picker.show();
            }
        });

        // Update Profile
        Button buttonUpdateProfile = findViewById(R.id.button_update_profile_data);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateProfile(firebaseUser);
            }
        });


    }

    private void updateProfile(FirebaseUser firebaseUser) {
            int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
            radioButtonUpdateGenderSelected = findViewById(selectedGenderID);
        if(TextUtils.isEmpty(textFullName)){
            // Check if user full name is empty or not
            Toast.makeText(UpdateProfileActivity.this , "Please enter your full name", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full Name is required ");
            editTextUpdateName.requestFocus();
        } // end if



        else if(TextUtils.isEmpty(textDoB)){
            // Check if user full name is empty or not
            Toast.makeText(UpdateProfileActivity.this , "Please enter your date of birth", Toast.LENGTH_LONG).show();
            editTextUpdateDoB.setError(" is required ");
            editTextUpdateDoB.requestFocus();
        } // end else if

        else if(TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())){
            Toast.makeText(UpdateProfileActivity.this , "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } // end else if
        else if(TextUtils.isEmpty(textMobile)){
            Toast.makeText(UpdateProfileActivity.this , "Please enter your mobile no.", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile No. is required");
            editTextUpdateMobile.requestFocus();
        } // end else if
        /*
         * Check if mobile number that user entered is correct or not by check his length must equal 11
         * */

        //else if(textMobile.length() != 11){
          //  Toast.makeText(UpdateProfileActivity.this , "Please re-enter your mobile no.", Toast.LENGTH_LONG).show();
            //editTextUpdateMobile.setError("Mobile No. should be 11 digits");
            //editTextUpdateMobile.requestFocus();
        //} // end else if
        else{
            // Obtain the data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            // Enter User Data into firebase realtime database. SetUp dependencies
            ReadWriteUserDetials writeUserDetails = new ReadWriteUserDetials(textDoB,textGender,textMobile);

            // Extract user reference from database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        // Setting new display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);
                        Toast.makeText(UpdateProfileActivity.this, "Update Successful!", Toast.LENGTH_SHORT).show();
                        // Stop users for returning to updateProfileActivity on pressing back button and close activity
                        Intent intent = new Intent(UpdateProfileActivity.this , UserProfileActivity.class);

                        // Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        try{
                            throw task.getException();
                        }
                        catch(Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        //progressBar.setVisibility(View.VISIBLE);



    }// end method


    // fetch data from firebase and display
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        // Extracting User Reference from Database for "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetials readUserDetails = snapshot.getValue(ReadWriteUserDetials.class);
                if(readUserDetails != null){
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.doB;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;

                    // Set data to edit texts
                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    // Show gender through Radio Button
                    if(textGender.equals("Male")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radioButton_update_profile_gender_male);

                    }else{
                        radioButtonUpdateGenderSelected = findViewById(R.id.radioButton_update_profile_gender_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                }else{
                    Toast.makeText(UpdateProfileActivity.this , "Something went wrong!",Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this , "Something went wrong!",Toast.LENGTH_LONG).show();

            }
        });
    }
}