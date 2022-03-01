package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class DeleteProfileActivity extends AppCompatActivity {
    FirebaseAuth authProfile;
    FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate , buttonDeleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        getSupportActionBar().setTitle("Delete Your Profile");

        progressBar = findViewById(R.id.profile_delete_user_progress_bar);
        editTextUserPwd = findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticated = findViewById(R.id.textView_delete_user_authenticated);
        buttonDeleteUser = findViewById(R.id.button_delete_user);
        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);

        // Disable Delete User Button until user is authenticated
        buttonDeleteUser.setEnabled(false);
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this,"Something went wrong! User's details not available",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this,UserProfileActivity.class);
            startActivity(intent);
            finish();
        }else{
            reAuthenticateUser(firebaseUser);
        }
    }

    // ReAuthenticate User Before Change Password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd = editTextUserPwd.getText().toString();
                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this,"Password is needed",Toast.LENGTH_LONG).show();
                    editTextUserPwd.setError("Please enter your current password to authenticate");
                    editTextUserPwd.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);

                    // ReAuthenticate User
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.VISIBLE);

                                // Disable editText for Password.
                                editTextUserPwd.setEnabled(false);


                                // Enable Delete User Button. Disable Authenticate Button
                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                // Set TextView to show user is Authenticated / Verified
                                textViewAuthenticated.setText("You are authenticated/verified. You can delete your Profile and related data now!");
                                Toast.makeText(DeleteProfileActivity.this,"Password has been verified."+"You can delete your Profile now!",Toast.LENGTH_LONG).show();

                                // Update color of change password button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this,R.color.dark_green));
                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showAlertDialog();
                                    }
                                });
                            }else{
                                try{
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });


                }
            }
        });
    }

    private void showAlertDialog() {
        // Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete User and Related Data?");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible");

        // Open email app if User clicks Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteUser(firebaseUser);
            }
        });

        // Return to user profile activity is user press cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(DeleteProfileActivity.this,UserProfileActivity.class);
                startActivity(intent);
                finish();
            }

        });

        // Create Alert Dialog
        AlertDialog alertDialog= builder.create();

        // Change the button color of Continue 
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });
        // Show Alert Dialog
        alertDialog.show();
    }

    private void deleteUser(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this,"User has been deleted!",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(DeleteProfileActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    try{
                        throw task.getException();
                    }catch(Exception e){
                        Toast.makeText(DeleteProfileActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });


    }// end method

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        // Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }// end method

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
            Intent intent = new Intent(DeleteProfileActivity.this , UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_update_email){
            Intent intent = new Intent(DeleteProfileActivity.this , UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_settings){
            Toast.makeText(DeleteProfileActivity.this,"Menu Settings!",Toast.LENGTH_LONG).show();
        }
        else if(id == R.id.menu_change_password){
            Intent intent = new Intent(DeleteProfileActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(DeleteProfileActivity.this , DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.menu_delete_logout){
            authProfile.signOut(); // Make user log-out
            Toast.makeText(DeleteProfileActivity.this , "Logged Out",Toast.LENGTH_LONG).show();
            // Go-to MainActivity
            Intent intent = new Intent(DeleteProfileActivity.this , MainActivity.class);

            // Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(DeleteProfileActivity.this , "Something went wrong",Toast.LENGTH_LONG).show();

        }


        return  super.onOptionsItemSelected(item);
    }
}