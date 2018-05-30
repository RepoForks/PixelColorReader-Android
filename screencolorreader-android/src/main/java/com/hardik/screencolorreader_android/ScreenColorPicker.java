package com.hardik.screencolorreader_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;

import java.nio.ByteBuffer;

import static android.app.Activity.RESULT_OK;

public class ScreenColorPicker {

    /**
     * static variables must be initialised once only
     */
    private static final String TAG = "ScreenColorPicker";//log tag
    private static final int REQUEST_CODE = 111;//request code
    private static final String VIRTUAL_DISPLAY_NAME = "ScreenColorPicker";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection mediaProjection;

    /**
     * reusable variables
     */
    private MediaProjectionManager projectionManager;
    private ImageReader imageReader;
    private Handler handler;
    private Display display;
    private VirtualDisplay virtualDisplay;
    private int density;
    private int width;
    private int height;
    private int rotation;
    private OrientationChangeCallback orientationChangeCallback;
    private Context context;
    private Bitmap latestBitmap;//this will hold the latest bitmap

    /**
     * callback variables and objects
     */
    private PermissionCallbacks permissionCallbacks;

    public ScreenColorPicker(Context context, int width, int height) {//only things necessary
        this.width = width;
        this.height = height;
        this.context = context;
    }

    /*****************************************Pre use functions************************************************************/
    /**
     * This is the 1st method to be called
     */
    public void initialise() {
        //get the media projection manager
        projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        //start capture handling thread to loop image getting
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                Looper.loop();
            }
        }.start();
        Log.d(TAG,"Initialisation Successful");
    }

    //checks if permission is granted or not
    public void checkPermission(PermissionCallbacks obj, int requestCode, int resultCode, Intent data) {
        permissionCallbacks = obj;
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Permission Granted");
                mediaProjection = projectionManager.getMediaProjection(resultCode, data);

                if (mediaProjection != null) {

                    // display metrics
                    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                    density = metrics.densityDpi;
                    display = ((Activity) context).getWindowManager().getDefaultDisplay();

                    // create virtual display depending on device width / height
                    createVirtualDisplay();

                    // register orientation change callback
                    orientationChangeCallback = new OrientationChangeCallback(context);
                    if (orientationChangeCallback.canDetectOrientation()) {
                        orientationChangeCallback.enable();
                    }

                    // register media projection stop callback
                    mediaProjection.registerCallback(new MediaProjectionStopCallback(), handler);
                }
                permissionCallbacks.onPermissionGranted();
            }
            else {
                Log.d(TAG, "Permission Denied");
                permissionCallbacks.onPermissionDenied();
            }
        }
    }

    private void createVirtualDisplay() {
        // start capture reader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(VIRTUAL_DISPLAY_NAME, width, height, density, VIRTUAL_DISPLAY_FLAGS, imageReader.getSurface(), null, handler);
        imageReader.setOnImageAvailableListener(new ImageAvailableListener(), handler);
        Log.d(TAG, "Virtual Display Created Successfully");
    }

    /***************************************Lifecycle Methods***************************************************************************/
    public void start() {
        ((Activity) context).startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        Log.d(TAG, "Start Successful");
    }

    public void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaProjection != null) {
                    mediaProjection.stop();
                }
            }
        });
        Log.d(TAG, "Stop Successful");
    }

    /*******************************************Data Handling Function(s)****************************************************/
    public int getColorInt(int x, int y) {//returns int color code
        return latestBitmap.getPixel(x, y);
    }

    public String getColorHex(int x, int y) {
        return String.format("#%06X", (0xFFFFFF & getColorInt(x, y)));
    }

    public Bitmap getLatestBitmap() {
        return latestBitmap;
    }

    /***************************************Callback classes********************************************************************/
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            Bitmap bitmap = null;

            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

                    //updating it
                    latestBitmap = croppedBitmap;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int trotation = display.getRotation();
            if (trotation != rotation) {
                rotation = trotation;
                try {
                    // clean up
                    if (virtualDisplay != null)
                        virtualDisplay.release();
                    if (imageReader != null)
                        imageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (virtualDisplay != null)
                        virtualDisplay.release();
                    if (imageReader != null)
                        imageReader.setOnImageAvailableListener(null, null);
                    if (orientationChangeCallback != null)
                        orientationChangeCallback.disable();
                    mediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    /**********************************************Callback interface***************************************************************************************/
    public interface PermissionCallbacks {
        //permission granted
        void onPermissionGranted();

        //permission denied
        void onPermissionDenied();
    }


}