package com.voidonaut.androidapptest;

import static com.voidonaut.androidapptest.util.MediaHelper._getOutputMediaFile;
import static com.voidonaut.androidapptest.util.MediaHelper._saveToFile;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.voidonaut.androidapptest.util.Log;


/**
 * Takes a photo saves it to the SD card and returns the path of this photo to the calling Activity
 * @author paul.blundell
 *
 */
public class CameraActivity extends Activity implements PictureCallback {

    protected static final String EXTRA_IMAGE_PATH = "com.voidonaut.androidapptest.CameraActivity.EXTRA_IMAGE_PATH";

    private Camera mCamera;
    private Camera.Parameters mCamParams;
    private CameraPreview cameraPreview;
    private ImageView focusImg;
    private ImageView mImageOverlay;
    private SeekBar mSeekbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
        setResult(RESULT_CANCELED);

        focusImg = (ImageView) findViewById(R.id.cam_focus);
        mImageOverlay = (ImageView) findViewById(R.id.cam_image_overlay);
        mSeekbar = (SeekBar) findViewById(R.id.cam_overlay_alpha);

        mSeekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
            @SuppressWarnings("deprecation")
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
            	mImageOverlay.setAlpha(progress);
            	Log.d(Integer.toString(progress));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        // Camera may be in use by another activity or the system or not available at all
        mCamera = null;
		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			// Camera is not available or doesn't exist
			Log.d("Camera.open failed", e);
		}

        if(mCamera != null){
            // Get camera parameters
        	mCamParams = mCamera.getParameters();
        	Log.d(mCamParams.flatten());

        	//Parameters interested in: flash-mode-values, focus-mode-values, preview-size-values, picture-size-values
        	// List<String> flashModes = mCamParams.getSupportedFlashModes();
        	// List<Camera.Area> focusAreas = mCamParams.getFocusAreas();
        	// Integer maxFocusAreas = mCamParams.getMaxNumFocusAreas();
        	List<Camera.Size> pictureSizes = mCamParams.getSupportedPictureSizes();
        	List<Camera.Size> previewSizes = mCamParams.getSupportedPreviewSizes();
        	for (Integer i = 0 ; i < pictureSizes.size() ; i++) {
            	// Log.d(Integer.toString(pictureSizes.get(i).height) + " " + Integer.toString(pictureSizes.get(i).width) + " ; ");
        	}

        	// Set camera parameters

        	// Setting picture size to first entry in array returned by getSupportedPictureSizes()
    		mCamParams.setPictureSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
    		mCamParams.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
    		mCamParams.setJpegQuality(100);

        	if (mCamParams.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            	mCamParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        	}
        	if (mCamParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        	    mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        		mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        	} else {
        		mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        	}

    		mCamera.setParameters(mCamParams);
    		focusImg.setAlpha(30);

    		Toast.makeText(getApplicationContext(), "Camera Parameters set", Toast.LENGTH_SHORT).show();

        	// Show the camera view on the activity
            cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
            cameraPreview.init(mCamera);
        } else {
            finish();
        }

        // Show the Up button in the action bar.
        //  setupActionBar();
	}


    public AutoFocusCallback _cameraFocused = new AutoFocusCallback() {
		@SuppressWarnings("deprecation")
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			// TODO Auto-generated method stub
			focusImg.setAlpha(100);
	        Log.d("Focused");
	        Toast.makeText(getApplicationContext(), "Focused", Toast.LENGTH_SHORT).show();
		}
	};

    @FromXML
    public void onCaptureClick(View button){
        // Take a picture with a callback when the photo has been created
        // Here you can add callbacks if you want to give feedback when the picture is being taken
        Log.d("Snap clicked");
        Toast.makeText(getApplicationContext(), "Button clicked", Toast.LENGTH_SHORT).show();
        mCamera.takePicture(null, null, this);
//    	mCamera.autoFocus(_cameraFocused);
    }

    @SuppressWarnings("deprecation")
	public void onFocusClick(View button){
        Log.d("Focus clicked");
        Toast.makeText(getApplicationContext(), "Focus clicked", Toast.LENGTH_SHORT).show();
		focusImg.setAlpha(30);
    	mCamera.autoFocus(_cameraFocused);
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d("Picture taken");
        Toast.makeText(getApplicationContext(), "Picture taken", Toast.LENGTH_LONG).show();
        String path = _savePictureToFileSystem(data);
        setResult(path);
        finish();
    }

    private static String _savePictureToFileSystem(byte[] data) {
        File file = _getOutputMediaFile();
        _saveToFile(data, file);
        return file.getAbsolutePath();
    }
 
    private void setResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IMAGE_PATH, path);
        setResult(RESULT_OK, intent);
    }

    // ALWAYS remember to release the camera when you are finished
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
 
    private void releaseCamera() {
        if(mCamera != null){
        	mCamera.release();
        	mCamera = null;
        }
    }

/*
    // Set up the {@link android.app.ActionBar}, if the API is available.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/

}
