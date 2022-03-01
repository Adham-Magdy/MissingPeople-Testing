package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class ChangePasswordActivity extends AppCompatActivity {

    // Define global Objects
    FirebaseAuth authProfile;
    private EditText editTextPwdCurr , editTextPwdNew , editTextPwdConfirm;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd , buttonReAuthenticate;
    private ProgressBar progressBar;
    private  String userPwdCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");

        // Define editTexts
        editTextPwdCurr = findViewById(R.id.editText_change_pwd);
        editTextPwdNew = findViewById(R.id.editText_change_pwd_new);
        editTextPwdConfirm = findViewById(R.id.editText_change_pwd_confirm);
        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);
        progressBar = findViewById(R.id.profile_change_password_progress_bar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_update_change_password_new);

        // Disable editText for new Password, Confirm New Password and make change pwd button un-clickable till user is authenticated
        editTextPwdNew.setEnabled(false);
        editTextPwdConfirm.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){

            Toast.makeText(ChangePasswordActivity.this , "Something went wrong! User's details not available",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePasswordActivity.this,UserProfileActivity.class);
            startActivity(intent);
            finish();

        } // end if
        else{
            reAuthenticateUser(firebaseUser);
        }
    }

    // ReAuthenticate User Before Change Password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwdCurr = editTextPwdCurr.getText().toString();
                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this,"Password is needed",Toast.LENGTH_LONG).show();
                    editTextPwdCurr.setError("Please enter your current password to authenticate");
                    editTextPwdCurr.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);

                    // ReAuthenticate User
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.VISIBLE);

                                // Disable editText for current password. Enable EditText for new password and confirm new password
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirm.setEnabled(true);

                                // Enable Change Pwd Button. Disable Authenticate Button
                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);

                                // Set TextView to show user is Authenticated / Verified
                                textViewAuthenticated.setText("You are authenticated/verified. You can change password now!");
                                Toast.makeText(ChangePasswordActivity.this,"Password has been verified."+"Change password now",Toast.LENGTH_LONG).show();

                                // Update color of change password button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this,R.color.dark_green));
                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else{
                                try{
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });


                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextPwdConfirm.getText().toString();
        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this,"New password is needed",Toast.LENGTH_LONG).show();
            editTextPwdNew.setError("Enter your new password");
            editTextPwdNew.requestFocus();
        }
        else if(TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this,"Confirm your new password",Toast.LENGTH_LONG).show();
            editTextPwdConfirm.setError("Re-enter your password ");
            editTextPwdConfirm.requestFocus();
        }
        else if(!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this, "Password did not match",Toast.LENGTH_LONG).show();
            editTextPwdNew.setError("Re-enter same password");
            editTextPwdNew.requestFocus();
        }
        else if(userPwdCurr.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "New password cannot be same as old password",Toast.LENGTH_LONG).show();
            editTextPwdNew.setError("Please enter a new password");
            editTextPwdNew.requestFocus();
        }else{
            progressBar.setVisibility(View.GONE);
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()){
                      Toast.makeText(ChangePasswordActivity.this,"Password has been changed",Toast.LENGTH_LONG).show();
                      Intent intent = new Intent(ChangePasswordActivity.this,UserProfileActivity.class);
                      startActivity(intent);
                      finish();
                  }else{
                      try{
                          throw task.getException();
                      }catch(Exception e){
                          Toast.makeText(ChangePasswordActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                      }
                  }
                  progressBar.setVisibility(View.GONE);
                }
            });
        }

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
            Intent intent = new Intent(ChangePasswordActivity.this , UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.menu_update_email){
            Intent intent = new Intent(ChangePasswordActivity.this , UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }

        //else if(id == R.id.menu_settings){
        //  Toast.makeText(UserProfileActivity.this,"Menu Settings!",Toast.LENGTH_LONG).show();
        //}
        else if(id == R.id.menu_change_password){
            Intent intent = new Intent(ChangePasswordActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(ChangePasswordActivity.this , DeleteProfileActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.menu_delete_logout){
            authProfile.signOut(); // Make user log-out
            Toast.makeText(ChangePasswordActivity.this , "Logged Out",Toast.LENGTH_LONG).show();
            // Go-to MainActivity
            Intent intent = new Intent(ChangePasswordActivity.this , MainActivity.class);

            // Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(ChangePasswordActivity.this , "Something went wrong",Toast.LENGTH_LONG).show();

        }


        return  super.onOptionsItemSelected(item);
    }
}
