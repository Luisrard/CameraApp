package com.l.cameraapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PHOTO = 10;
    private static final int REQUEST_TAKE_PHOTO = 11;
    private final String CARPETA_RAIZ = "imageExample/";
    private final String RUTA_IMAGEN = CARPETA_RAIZ + "myPhotos";
    private String path;

    ImageView mPhoto;
    Button mPickPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPhoto = findViewById(R.id.image);
        mPickPhoto = findViewById(R.id.button_camera);
        mPickPhoto.setOnClickListener(this);
        if(validatePermission()){
            mPickPhoto.setEnabled(true);
        }else{
            mPickPhoto.setEnabled(false);
        }
    }

    private boolean validatePermission() {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){//Marshmallow
            return true;
        }
        else if(checkSelfPermission(CAMERA)== PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            return true;
        }
        if(shouldShowRequestPermissionRationale(CAMERA) || shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)){
            chargeDialogRecommendation();
        }
        else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
        }
        return false;
    }

    private void chargeDialogRecommendation() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Permission disabled");
        dialog.setMessage("You should enable permissions");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){
                mPickPhoto.setEnabled(true);
            }else{
                requestPerimissions();
            }
        }
    }

    private void requestPerimissions() {
        final CharSequence[] options = {"Ok","Cancel"};
        final AlertDialog.Builder alertOption = new AlertDialog.Builder(MainActivity.this);
        alertOption.setTitle("¿Do you want configure the permissions manually");
        alertOption.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Ok")){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                else{
                    dialog.dismiss();
                }
            }
        });
        alertOption.show();
    }

    @Override
    public void onClick(View v) {
        if(v==mPickPhoto){
            chargeImage();
        }
    }

    @SuppressLint("IntentReset")
    private void chargeImage() {
        final CharSequence[] options = {"Tomar Foto","Cargar Imagen","Cancelar"};
        final AlertDialog.Builder alertOption = new AlertDialog.Builder(MainActivity.this);
        alertOption.setTitle("Selecciona una opción");
        alertOption.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Tomar Foto")){
                    takePhoto();
                }
                else if(options[which].equals("Cargar Imagen")){
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    intent.setType("image/");
                    startActivityForResult(Intent.createChooser(intent, "Select the app"),
                            REQUEST_PHOTO);
                }
                else{
                    dialog.dismiss();
                }
            }
        });
        alertOption.show();
    }

    private void takePhoto(){
        File fileImage = new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        boolean isCreate = fileImage.exists();
        String nameImage = "";
        if(!isCreate){
            isCreate = fileImage.mkdir();
        }
        if(isCreate){
            nameImage = (System.currentTimeMillis()/1000) + ".jpg";
        }

        path = Environment.getExternalStorageDirectory() +
                File.separator + RUTA_IMAGEN + File.separator + nameImage;

        File image = new File(path);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
        startActivityForResult(intent,REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_PHOTO && data != null) {
                Uri mPath = data.getData();
                mPhoto.setImageURI(mPath);
            }
            else if(requestCode == REQUEST_TAKE_PHOTO){
                MediaScannerConnection.scanFile(this, new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Ruta de almacemiento","Path: " + path);
                            }
                        });
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                mPhoto.setImageBitmap(bitmap);
            }
        }
    }
}
