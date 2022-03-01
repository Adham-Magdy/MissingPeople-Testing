package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class UserProfileActivity extends AppCompatActivity {

    // Define global values for textView
    private TextView textViewWelcome , textViewFullName , textViewEmail , textViewGender , textViewDOB , textViewMobile;
    private ProgressBar progressBar;
    private String fullName , email , doB , gender , mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set Title to action bar
        getSupportActionBar().setTitle("Home");

        // Assign id's for textView's
        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewDOB = findViewById(R.id.textView_show_dob);
        textViewMobile = findViewById(R.id.textView_show_mobile_number);
        progressBar = findViewById(R.id.progressBar);

        // Set onClickListener on ImageView to open UploadProfilePicActivity
        imageView = findViewById(R.id.imageView_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this,UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });

        // Get user instance
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // Check User details
        if(firebaseUser == null){
            Toast.makeText(UserProfileActivity.this , "Something went wrong! User's details are not available at the moment",Toast.LENGTH_LONG).show();


        } // end if
        else{

            // Check if user email is verified
            checkIfEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser); // method that display User Data
        } // end else

    } // end onCreate method

    // Users coming to UserProfileActivity after successful registration
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();

        }// end if


    } // end method

    private void showAlertDialog() {
        // Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. You can login without email verification");

        // Open email app if User clicks Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To open email app in new window and not within our app
                startActivity(intent);
            }
        });

        // Create Alert Dialog
        AlertDialog alertDialog= builder.create();

        // Show Alert Dialog
        alertDialog.show();

    }// end method


    private void showUserProfile(FirebaseUser firebaseUser) {
        // get user's id
        String userID = firebaseUser.getUid();

        // Extracting User Reference from Database for "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetials readUserDetails = snapshot.getValue(ReadWriteUserDetials.class);
                if(readUserDetails != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readUserDetails.doB;
                    gender = readUserDetails.gender;
                    mobile = readUserDetails.mobile;

                    // set user full name to welcome text
                    textViewWelcome.setText("Welcome, "+fullName+"!");
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewDOB.setText(doB);
                    textViewGender.setText(gender);
                    textViewMobile.setText(mobile);

                    //Set user DP after user has uploaded
                    Uri uri = firebaseUser.getPhotoUrl();
                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);


                } // end if
                else{
                    Toast.makeText(UserProfileActivity.this , "Something went wrong!",Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this , "Something went wrong!",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);




            }
        });

    } // end method

    // Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        // Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }// end method

    // When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();

        if(id == R.id.menu_refresh){
            // Refresh Activity
            startActivity(getIntent());
            finish();
            // No Animation when refreshing
            overridePendingTransition(0,0);
        }

        else if(id == R.id.menu_update_profile){
            Intent intent = new Intent(UserProfileActivity.this , UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.menu_update_email){
            Intent intent = new Intent(UserProfileActivity.this , UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_settings){
            Toast.makeText(UserProfileActivity.this,"Menu Settings!",Toast.LENGTH_LONG).show();
        }
        else if(id == R.id.menu_change_password){
            Intent intent = new Intent(UserProfileActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfileActivity.this , DeleteProfileActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_delete_logout){
            authProfile.signOut(); // Make user log-out
            Toast.makeText(UserProfileActivity.this , "Logged Out",Toast.LENGTH_LONG).show();
            // Go-to MainActivity
            Intent intent = new Intent(UserProfileActivity.this , MainActivity.class);

            // Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(UserProfileActivity.this , "Something went wrong",Toast.LENGTH_LONG).show();

        }


        return  super.onOptionsItemSelected(item);
    }
}