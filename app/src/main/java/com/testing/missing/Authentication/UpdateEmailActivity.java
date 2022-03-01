package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class UpdateEmailActivity extends AppCompatActivity {

    // Define global objects
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String userOldEMail , userNewEmail , userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail , editTextPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        getSupportActionBar().setTitle("Update Email");

        // Assign Values
        progressBar = findViewById(R.id.profile_update_email_progress_bar);
        editTextPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuthenticated = findViewById(R.id.textView_update_authenticate_email);
        buttonUpdateEmail = findViewById(R.id.button_update_email_new);

        buttonUpdateEmail.setEnabled(false); // Make button disabled in the beginning until the user is authenticated
        editTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Set old email ID in TextView
        userOldEMail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textView_update_email_old);
        textViewOldEmail.setText(userOldEMail);

        if(firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this,"Something went wrong! User's details not available",Toast.LENGTH_LONG).show();
        }else{
            reAuthenticate(firebaseUser);
        }


    } // end onCreate method

    // ReAuthenticate / Verify user before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.button_update_email_authenticate);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtain password for authenticate
                userPwd = editTextPwd.getText().toString();
                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this , "Password is need to continue",Toast.LENGTH_LONG).show();
                    editTextPwd.setError("Please enter your password for authentication");
                    editTextPwd.requestFocus();
                }else{
                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEMail,userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(UpdateEmailActivity.this,"Password has been verified. You can update email now.",Toast.LENGTH_LONG).show();

                                // Set TextView to show that user is authenticated

                                textViewAuthenticated.setText("You are authenticated. You can update your email now.");

                                // Disable edit text for password and enable edit text for new email and update email
                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                // Change color of update email button
                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,R.color.dark_green));

                                // Set click of Button update new email
                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        // Check if new email edit text is empty or not invalid
                                        if(TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this,"New email is required",Toast.LENGTH_LONG).show();
                                            editTextNewEmail.setError("Please enter new email");
                                            editTextNewEmail.requestFocus();
                                        }
                                        else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this , "Please enter valid email", Toast.LENGTH_LONG).show();
                                            editTextNewEmail.setError("Valid email is required ");
                                            editTextNewEmail.requestFocus();
                                        } // end else if

                                        // Check if old email is equal to old email
                                        else if(userOldEMail.matches(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this , "New Email can not be same as old Email", Toast.LENGTH_LONG).show();
                                            editTextNewEmail.setError("Please enter new email");
                                            editTextNewEmail.requestFocus();
                                        }
                                        else{
                                            updateEmail(firebaseUser);
                                        }
                                    } // end onclick method
                                });
                            } // end if
                            else{

                                try{
                                    throw task.getException();

                                }catch(Exception e){
                                    Toast.makeText(UpdateEmailActivity.this , e.getMessage(), Toast.LENGTH_LONG).show();

                                }

                            }// end else

                        }
                    });
                }
            }
        });


    }// end method

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){

                    // Verify Email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this,"Email has been updated. Please verify your new Email",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this , UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    try{
                        throw task.getException();

                    }
                    catch(Exception e){
                        Toast.makeText(UpdateEmailActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
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
            Intent intent = new Intent(UpdateEmailActivity.this , UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.menu_update_email){
            Intent intent = new Intent(UpdateEmailActivity.this , UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_settings){
          Toast.makeText(UpdateEmailActivity.this,"Menu Settings!",Toast.LENGTH_LONG).show();
        }
        else if(id == R.id.menu_change_password){
            Intent intent = new Intent(UpdateEmailActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_delete_logout){
            authProfile.signOut(); // Make user log-out
            Toast.makeText(UpdateEmailActivity.this , "Logged Out",Toast.LENGTH_LONG).show();
            // Go-to MainActivity
            Intent intent = new Intent(UpdateEmailActivity.this , MainActivity.class);

            // Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(UpdateEmailActivity.this , "Something went wrong",Toast.LENGTH_LONG).show();

        }


        return  super.onOptionsItemSelected(item);
    }
}