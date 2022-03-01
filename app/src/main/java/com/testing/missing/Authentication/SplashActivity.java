package com.testing.missing.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.testing.missing.HomePageActivity;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
          getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) ;
          getSupportActionBar();
        } // It will hide the status bar

        // Configure progress bar
        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(3000);
                    startActivity(new Intent(SplashActivity.this , MainActivity.class));
                    finish();
                }catch(Exception e){

                }

            } // end run void method
        }; // end thread
        thread.start();
    }
}