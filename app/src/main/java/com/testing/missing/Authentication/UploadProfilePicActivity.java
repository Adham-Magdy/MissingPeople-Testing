package com.testing.missing.Authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.testing.missing.MainActivity;
import com.testing.missing.R;

public class UploadProfilePicActivity extends AppCompatActivity {

    private ImageView imageViewUploadPic;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        getSupportActionBar().setTitle("Upload Profile Picture");


        // Creating choose pic button

        Button buttonChooseUploadPic = findViewById(R.id.button_choose_profile_pic);
        Button buttonUploadPic = findViewById(R.id.button_upload_profile_pic);
        imageViewUploadPic=findViewById(R.id.image_profile_upload);
        progressBar = findViewById(R.id.profile_pic_upload_progress_bar);
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri = firebaseUser.getPhotoUrl();

        // Set User's current DP in image view (if uploaded already). We Will Using Picasso
        Picasso.with(UploadProfilePicActivity.this).load(uri).into(imageViewUploadPic);

        // Define choose upload picture
        buttonChooseUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        // Define upload picture
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPic(); // method for uploading user profile picture
            }
        });




    }

    private void UploadPic() {
        // Check if user has chosen image
        if(uriImage != null){
            // Save the image with uid of current logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid()+"."+getFileExtension(uriImage));
            // Upload Image to storage

            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // Finally set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this,"Upload Successful!",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(UploadProfilePicActivity.this,UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

                }
            });
        }else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this,"No File Selected!" ,Toast.LENGTH_LONG).show();

        }

    }

    // Obtain the file extension of image
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
    }



}
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
            Intent intent = new Intent(UploadProfilePicActivity.this , UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_update_email){
            Intent intent = new Intent(UploadProfilePicActivity.this , UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_settings){
            Toast.makeText(UploadProfilePicActivity.this,"Menu Settings!",Toast.LENGTH_LONG).show();
        }
        else if(id == R.id.menu_change_password){
            Intent intent = new Intent(UploadProfilePicActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }

        else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(UploadProfilePicActivity.this , DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.menu_delete_logout){
            authProfile.signOut(); // Make user log-out
            Toast.makeText(UploadProfilePicActivity.this , "Logged Out",Toast.LENGTH_LONG).show();
            // Go-to MainActivity
            Intent intent = new Intent(UploadProfilePicActivity.this , MainActivity.class);

            // Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(UploadProfilePicActivity.this , "Something went wrong",Toast.LENGTH_LONG).show();

        }


        return  super.onOptionsItemSelected(item);
    }
}