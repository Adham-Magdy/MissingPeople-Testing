package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Define Values
    private Button buttonPwdReset;
    private EditText editTextPwdEmailReset;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setTitle("Forgot Password");
        editTextPwdEmailReset = findViewById(R.id.editText_password_reset_email);
        buttonPwdReset = findViewById(R.id.button_password_reset);
        progressBar = findViewById(R.id.password_reset_progress_bar);

        buttonPwdReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextPwdEmailReset.getText().toString();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPasswordActivity.this , "Please enter your registered email",Toast.LENGTH_LONG).show();
                    editTextPwdEmailReset.setError("Email is required");
                    editTextPwdEmailReset.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(ForgotPasswordActivity.this , "Please enter valid email", Toast.LENGTH_LONG).show();
                    editTextPwdEmailReset.setError("Valid email is required ");
                    editTextPwdEmailReset.requestFocus();
                } // end else if
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email); // method for resetting password
                }


            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this , "Check your inbox for password reset link",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this , MainActivity.class);

                    // Clear stack to prevent user coming back to ForgotPasswordActivity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthInvalidUserException e){
                        editTextPwdEmailReset.setError("User does not exists or is no longer valid. Please register again");
                    }catch(Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this , e.getMessage(),Toast.LENGTH_LONG).show();

                    }

                }
                progressBar.setVisibility(View.GONE);

            }
        });

    } // end method
}