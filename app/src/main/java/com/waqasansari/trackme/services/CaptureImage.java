package com.waqasansari.trackme.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.waqasansari.trackme.handlers.SendEmail;
import com.waqasansari.trackme.utils.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;

/*
Reference: http://stackoverflow.com/questions/28003186/capture-picture-without-preview-using-camera2-api
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CaptureImage extends Service {
    protected static final String TAG = "ImageProcessing";
    protected static final int CAMERA_CHOICE = CameraCharacteristics.LENS_FACING_FRONT;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;

    private String email;

    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "CameraDevice.StateCallback onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
        }
    };

    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "CameraCaptureSession.StateCallback onConfigured");
            CaptureImage.this.session = session;
            try {
                session.setRepeatingRequest(createCaptureRequest(), null, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "onImageAvailable");
            Image img = reader.acquireLatestImage();
            if (img != null) {
                processImage(img);
            }
        }
    };

    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(300, 300, ImageFormat.JPEG, 1 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.i(TAG, "imageReader created");
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     *  Return the Camera Id which matches the field CAMERACHOICE.
     */
    public String getCamera(CameraManager manager){
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERA_CHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand flags " + flags + " startId " + startId);


        if(intent.getExtras() != null) {
            email = intent.getStringExtra("email");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                readyCamera();
            }
        }).run();

        return super.onStartCommand(intent, flags, startId);
    }

    public void actOnReadyCameraDevice()
    {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), sessionStateCallback, null);

        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service Destroyed.");
        try {
            session.abortCaptures();
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        session.close();
        if(email != null) {
            new SendEmail(CaptureImage.this,
                    email,
                    "Pictures",
                    "Attached photos may be of the suspect who has stolen your phone.",
                    Config.capturedBitmaps).execute();
        }

    }

    /**
     *  Process image data as desired.
     */
    private void processImage(Image image){
        //Process image data
        Toast.makeText(this, "Image Captured", Toast.LENGTH_SHORT).show();
        FileOutputStream fos = null;
        Bitmap bitmap = null;
        try {
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                if (planes[0].getBuffer() == null) {
                    return;
                }
                int width = image.getWidth();
                int height = image.getHeight();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                //byte[] newData = planes[0].getBuffer().array();

                ByteBuffer buffer = planes[0].getBuffer();
                byte[] newData = new byte[buffer.remaining()];
                buffer.get(newData);

                int offset = 0;
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);



                //ByteBuffer buffer = planes[0].getBuffer();
                for (int i = 0; i < height-1; ++i) {
                    for (int j = 0; j < width-1; ++j) {
                        int pixel = 0;
                        pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                        pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                        pixel |= (buffer.get(offset + 2) & 0xff);       // B
                        pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                        bitmap.setPixel(j, i, pixel);
                        offset += pixelStride;
                    }
                    offset += rowPadding;
                }

                bitmap = BitmapFactory.decodeByteArray(newData, offset, newData.length);

                String name = "DCIM/Camera/IMG_" + System.currentTimeMillis() + ".jpg";
                File file = new File(Environment.getExternalStorageDirectory(), name);
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Toast.makeText(this, "Image saved in" + Environment.getExternalStorageDirectory() + name, Toast.LENGTH_SHORT).show();
                Config.capturedBitmaps.add(Environment.getExternalStorageDirectory() + "/" + name);
                image.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != bitmap) {
                bitmap.recycle();
            }
            if (null != image) {
                image.close();
            }
            stopSelf();
        }
    }

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}