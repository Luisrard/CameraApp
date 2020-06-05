package com.l.cameraapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PHOTO = 10;
    ImageView mPhoto;
    Button mPickPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPhoto = findViewById(R.id.image);
        mPickPhoto = findViewById(R.id.button_camera);
        mPickPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==mPickPhoto){
            chargeImage();
        }
    }

    @SuppressLint("IntentReset")
    private void chargeImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(Intent.createChooser(intent,"Select the app"),REQUEST_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PHOTO && resultCode == RESULT_OK && data != null){
            Uri path = data.getData();
            mPhoto.setImageURI(path);
        }
    }
}
