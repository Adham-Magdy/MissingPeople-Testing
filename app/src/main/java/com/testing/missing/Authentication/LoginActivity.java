package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.testing.missing.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail , editTextPwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";
    boolean passwordVisible ;// boolean value for show and hide password


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login");
        editTextLoginEmail = findViewById(R.id.editText_email_login);
        editTextPwd = findViewById(R.id.editText_password_login);
        progressBar = findViewById(R.id.login_progress_bar);
        authProfile = FirebaseAuth.getInstance();

        // Define button for forgot password
        Button forgotPasswordButton = findViewById(R.id.button_forgot_password);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this , "You can reset your password now!",Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));

            }
        });

        // Show and Hide Password
        /**
        editTextPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final  int Right = 2;
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (motionEvent.getRawX() >= editTextPwd.getRight() - editTextPwd.getCompoundDrawables()[Right].getBounds().width()) {
                        int selection = editTextPwd.getSelectionEnd();
                        if(passwordVisible){
                            editTextPwd.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_hide_password,0);
                            editTextPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;


                        }else{
                            editTextPwd.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_show_password,0);
                            passwordVisible = true;

                        }
                        editTextPwd.setSelection(selection);
                        return true;
                    }

                    }
                return false;
            }
        });**/

        // Set Button Login
        Button loginButton = findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextPwd.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this , "Please enter your email",Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginEmail.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(LoginActivity.this , "Please re-enter your email", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Valid email is required ");
                    editTextLoginEmail.requestFocus();
                }  //end else if
                else if(TextUtils.isEmpty(textPwd)){
                    Toast.makeText(LoginActivity.this , "Please enter your password",Toast.LENGTH_LONG).show();
                    editTextPwd.setError("Password is required");
                    editTextPwd.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail,textPwd);
                }


            }
        });

    }

    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // Check if Task Successful
                if(task.isSuccessful()){
                    // Show AlertDialog Box if Email Not Verified

                    // Get instance of current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    // Check if email is verified before user can access their profile
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this,"You are logged in now",Toast.LENGTH_LONG).show();
                        // Start The UserProfileActivity
                        startActivity(new Intent(LoginActivity.this , UserProfileActivity.class));
                        finish();


                    }// end if
                    // open user profile
                    else{
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut(); // Sign-out user
                        showAlertDialog();
                    }


                }else{
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("User does not exists or is no longer valid. Please register again.");
                        editTextLoginEmail.requestFocus();

                    }
                    catch(FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("Invalid credentials. Check and re-enter");
                        editTextLoginEmail.requestFocus();

                    }
                    catch(Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();


                    }
                    Toast.makeText(LoginActivity.this,"Something went wrong!",Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);


            }
        });
    }

    private void showAlertDialog() {
        // Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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

    // Check if the User is already logged in. In such case, straightway take the user to the User's profile
    @Override
    protected void onStart() {
        super.onStart();
        if(authProfile.getCurrentUser() != null){
            Toast.makeText(LoginActivity.this , "Already Logged In!" , Toast.LENGTH_LONG).show();

            // Start The UserProfileActivity
            startActivity(new Intent(LoginActivity.this , UserProfileActivity.class));
            finish();
        }else{
            Toast.makeText(LoginActivity.this , "You can login now!" , Toast.LENGTH_LONG).show();

        }
    } // end onStart method
}