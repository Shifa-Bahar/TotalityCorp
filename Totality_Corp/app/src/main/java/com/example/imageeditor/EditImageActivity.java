package com.example.imageeditor;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.example.imageeditor.MainActivity.bitmap;
import static com.example.imageeditor.MainActivity.imageFileName;
import static com.example.imageeditor.MainActivity.uri;
import static com.example.imageeditor.MainActivity.uri1;

public class EditImageActivity extends AppCompatActivity {

    ImageView imageView;
    Button rotateButton;
    Button saveButton;
    Button cropButton;
    int mCurrRotation = 0;
    static Bitmap rotateBitmap;
    static Bitmap cropThenRotateBitmap;
    static Bitmap rotateThenCropBitmap;
    final int PIC_CROP = 1;
    static Bitmap croppedBitmap;
    static Uri resultUri;
    static Bitmap unChangedBitmap;
    boolean isRotate = false;
    static Bitmap bitmapBasic;
    float fromRotation;
    float toRotation;


    public void makeBitmapNull(){
        mCurrRotation=0;
        toRotation=0;
        fromRotation=0;
        rotateBitmap=null;
        croppedBitmap=null;
        rotateThenCropBitmap=null;
        cropThenRotateBitmap=null;
    }

    public void undo(View view) {
        Matrix matrix = new Matrix();


         toRotation = mCurrRotation += 90;

        final RotateAnimation rotateAnimation = new RotateAnimation(
                fromRotation, 0, imageView.getWidth() / 2, imageView.getHeight() / 2);

        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);


        matrix.setRotate(toRotation);
        System.out.println(toRotation + "TO ROTATION");
        System.out.println(fromRotation + "FROM ROTATION");
        if (croppedBitmap != null) {
            cropThenRotateBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
        } else {
            rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        imageView.setImageBitmap(bitmap);
        imageView.startAnimation(rotateAnimation);
        makeBitmapNull();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void crop(View view) {

        if(rotateBitmap!=null){
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), rotateBitmap, imageFileName+".jpg", null);
//            System.out.println(Uri.parse(path));
             uri=Uri.parse(path);
            CropImage.activity(uri)
                    .start(this);
        }
        else if(uri1!=null){
            CropImage.activity(uri1)
                    .start(this);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                imageView.setImageURI(resultUri);
//                Matrix matrix = new Matrix();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                System.out.println(imageView.getRotation());
                croppedBitmap = bitmapDrawable.getBitmap();
                if (isRotate) {
                    rotateThenCropBitmap = croppedBitmap;
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void save(View view) throws IOException {


        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName + ".jpg");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        OutputStream imageOutStream = null;

        try {
            if (uri == null) {
                throw new IOException("Failed to insert MediaStore row");
            }

            imageOutStream = resolver.openOutputStream(uri);
            if (cropThenRotateBitmap != null) {
                if (!cropThenRotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else if (rotateThenCropBitmap != null) {
                if (!rotateThenCropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else if (croppedBitmap != null) {
                if (!croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else if (rotateBitmap != null) {
                if (!rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            } else {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                    throw new IOException("Failed to compress bitmap");
                }
            }

            Toast.makeText(this, "Imave Saved", Toast.LENGTH_SHORT).show();

        } finally {
            if (imageOutStream != null) {
                imageOutStream.close();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                finish();
                startActivity(intent);
            }
        }

    }

    public void rotate(View view) {
        isRotate = true;
        mCurrRotation %= 360;
        Matrix matrix = new Matrix();


        System.out.println(imageView.getRotation());
        fromRotation = mCurrRotation;
        toRotation = mCurrRotation += 90;

        final RotateAnimation rotateAnimation = new RotateAnimation(
                fromRotation, toRotation, imageView.getWidth() / 2, imageView.getHeight() / 2);

        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);


        matrix.setRotate(toRotation);
        System.out.println(toRotation + "TO ROTATION");
        System.out.println(fromRotation + "FROM ROTATION");
        if (croppedBitmap != null) {
            cropThenRotateBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
        } else {
            rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }


        imageView.startAnimation(rotateAnimation);


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        imageView = findViewById(R.id.editImageView);
        rotateButton = findViewById(R.id.rotateButton);
        saveButton = findViewById(R.id.saveButton);
        cropButton = findViewById(R.id.cropButton);
        ActivityCompat.requestPermissions(EditImageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        try {
            imageView.setImageBitmap(bitmap);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}