package com.voidonaut.androidapptest;

import com.voidonaut.androidapptest.util.Log;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 *
 * @author paul.blundell
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 
    private Camera mCamera;
    private SurfaceHolder mHolder;
 
    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public CameraPreview(Context context) {
        super(context);
    }

    public void init(Camera camera) {
        this.mCamera = camera;
        initSurfaceHolder();
    }
 
    @SuppressWarnings("deprecation") // needed for < 3.0
    private void initSurfaceHolder() {
    	mHolder = getHolder();
    	mHolder.addCallback(this);
    	mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	// The Surface has been created, now tell the camera where to draw the preview.
        try {
        	mCamera.setPreviewDisplay(holder);
        	mCamera.startPreview();
        } catch (Exception e) {
            Log.d("Error setting camera preview", e);
        }
    }
 
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Error starting camera preview: " + e.getMessage());
        }
    }
 
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }
}