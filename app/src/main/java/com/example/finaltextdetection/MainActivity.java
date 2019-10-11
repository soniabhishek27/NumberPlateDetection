package com.example.finaltextdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SparseArrayCompat;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;


import android.Manifest;
import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.content.res.Resources;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Display;
import android.view.Surface;

import android.view.WindowManager;

import android.hardware.Camera;


import android.media.Ringtone;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.theartofdev.edmodo.cropper.CropImage;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final int CAMERA_REQUEST_CODE = 200;
    public static int STORAGE_REQUEST_CODE = 400;
    public static int IMAGE_PICK_GALLERY_CODE = 1000;
    public static int IMAGE_PICK_CAMERA_CODE = 1001;

    SurfaceView mCameraView,transparentView;
    TextView mTextView;
    Button getData, draw, exit;
    CameraSource mCameraSource;
    ImageView cropImageView;
    SurfaceHolder holder,holderTransparent;
    Camera camera;
    Rect rect = new Rect();


    private float RectLeft, RectTop,RectRight,RectBottom ;

    int  deviceHeight,deviceWidth;

    private static final int requestPermissionID = 101;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = findViewById(R.id.surfaceView);
        transparentView= findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);
        holder = mCameraView.getHolder();

        holder.addCallback((SurfaceHolder.Callback) this);

            mCameraView.setSecure(true);

        //second holder

     transparentView = (SurfaceView)findViewById(R.id.TransparentView);

holderTransparent = transparentView.getHolder();

holderTransparent.addCallback((SurfaceHolder.Callback)this);

        holderTransparent.setFormat(PixelFormat.TRANSLUCENT);

        transparentView.setZOrderMediaOverlay(true);

        //getting device height and weight

        deviceHeight = getScreenHeight();
        deviceWidth = getScreenWeight();

        cropImageView = findViewById(R.id.cropImageView);

        getData = findViewById(R.id.getData);
        exit = findViewById(R.id.exit);

        getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdata();
            }
        });

        //to Exit the app
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        startCameraSource();
    }

    public int getScreenWeight()
    {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight()
    {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private void Draw()
    {
        Canvas canvas = holderTransparent.lockCanvas(null);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(0);


        RectLeft = 80;

        RectTop = 20 ;

        RectRight = RectLeft+ deviceWidth-100;

        RectBottom =RectTop+ 200;

        Rect rec =new Rect((int) RectLeft,(int)RectTop,(int)RectRight,(int)RectBottom);

        canvas.drawRect(rec,paint);

        holderTransparent.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            synchronized (holder)
            {
                Draw();
            }   //call a draw method

            camera = Camera.open(); //open a camera
        }

        catch (Exception e) {

            Log.i("Exception", e.toString());

            return;

        }
        Camera.Parameters param;

        param = camera.getParameters();
        param.setZoom(param.getMaxZoom());

       param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//        param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//

        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)

        {

            camera.setDisplayOrientation(90);

        }
        camera.setParameters(param);
        try {

            camera.setPreviewDisplay(holder);

            camera.startPreview();

        }


        catch (Exception e) {


            return;

        }

    }
    @Override

    protected void onDestroy() {

        super.onDestroy();

    }
    @Override

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        refreshCamera(); //call method for refresh camera
    }

    public void refreshCamera() {

        if (holder.getSurface() == null) {

            return;
        }
        try
        {
            camera.stopPreview();
        }
        catch (Exception e) {
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }

        catch (Exception e) {

        }
    }
    @Override

    public void surfaceDestroyed(SurfaceHolder holder) {

        camera.release(); //for release a camera

    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode != requestPermissionID) {
            Toast.makeText(MainActivity.this, "Unexpected permission result" + requestCode, Toast.LENGTH_SHORT).show();
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(transparentView.getHolder());
              //  mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        if (requestCode == RESULT_OK)
        {

            startCameraSource();

        }
    }

    private void startCameraSource() {


        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext())
                .build();



        if (!textRecognizer.isOperational())
        {
            Toast.makeText(MainActivity.this, "Try again dependencies not loadded yet", Toast.LENGTH_SHORT).show();
        }
        else
            {
                mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setAutoFocusEnabled(true)

                        //.setRequestedPreviewSize(RectTop,RectBottom)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedFps(30.0f)
                        .build();


            }

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                requestPermissionID);
                        return;
                    }
                    mCameraSource.start(mCameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                mCameraSource.stop();
            }
        });

        //Set the text recognizer process
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release()
            {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if (items.size() != 0) {

                    mTextView.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = 0; i < items.size(); i++) {
                                TextBlock item = items.valueAt(i);
                                stringBuilder.append(item.getValue());
                                stringBuilder.append("\n");
                            }
                            mTextView.setText(stringBuilder.toString());
                        }
                    });
                }
            }
        });
    }
    public void getdata()
    {
        try {

            String Result= mTextView.getText().toString();
            Intent intent= new Intent(MainActivity.this,Data.class);
            intent.putExtra("value",Result);
            startActivity(intent);

        }
        catch (Exception e)
        {

        }

    }
}


