package com.voidonaut.androidapptest;

import static com.voidonaut.androidapptest.util.MediaHelper._getOutputMediaFile;
import static com.voidonaut.androidapptest.util.MediaHelper._saveToFile;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.voidonaut.androidapptest.util.BitmapHelper;
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
    private List<String> mFlashModes;
    private List<String> mFocusModes;
    private Button mFlashButton;
    private Button mFocusButton;
    private Button mSaveButton;
    private Button mDiscardButton;
    private Button mCameraChangeButton;
    private byte[] mPictureData;
    private String mSeriesFolder;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
        setResult(RESULT_CANCELED);

        focusImg = (ImageView) findViewById(R.id.cam_focus_crosshair);
        mImageOverlay = (ImageView) findViewById(R.id.cam_image_overlay);

        mSeekbar = (SeekBar) findViewById(R.id.cam_overlay_alpha);

        mFlashButton = (Button) findViewById(R.id.cam_controls_flash);
//      mFocusButton = (Button) findViewById(R.id.cam_controls_focus);
        mSaveButton = (Button) findViewById(R.id.cam_controls_save);
        mDiscardButton = (Button) findViewById(R.id.cam_controls_discard);
        mCameraChangeButton = (Button) findViewById(R.id.cam_change_camera);

        mSaveButton.setVisibility(View.GONE);
        mDiscardButton.setVisibility(View.GONE);
		focusImg.setAlpha(100);

	    // Get the message from the intent
	    Intent intent = getIntent();
	    String overlayImgPath = intent.getStringExtra(MainActivity.EXTRA_OVERLAY_IMG_PATH);
	    mImageOverlay.setImageBitmap(BitmapHelper._decodeSampledBitmap(overlayImgPath, 300, 250));
	    mSeriesFolder = "";


        mSeekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
            @SuppressWarnings("deprecation")
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
            	mImageOverlay.setAlpha(progress);
//            	Log.d(Integer.toString(progress));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        // If front camera exists, enable Change Camera Button
        // http://stackoverflow.com/questions/6599454/switch-back-front-camera-on-fly
        if (_getFrontCameraId() < 0) {
        	mCameraChangeButton.setVisibility(View.GONE);
        }
        Log.d(Integer.toString(_getFrontCameraId()));

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
        	List<Camera.Size> pictureSizes = mCamParams.getSupportedPictureSizes();
        	List<Camera.Size> previewSizes = mCamParams.getSupportedPreviewSizes();
        	mFlashModes = mCamParams.getSupportedFlashModes();
        	mFocusModes = mCamParams.getSupportedFocusModes();
            // List<Camera.Area> focusAreas = mCamParams.getFocusAreas();
        	// Integer maxFocusAreas = mCamParams.getMaxNumFocusAreas();

        	// Set camera parameters
        	// Setting picture size to first entry in array returned by getSupportedPictureSizes()
    		mCamParams.setPictureSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
    		mCamParams.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
    		mCamParams.setJpegQuality(100);

    		if (mCamParams.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            	mCamParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        	}
        	mFlashButton.setText(Camera.Parameters.FLASH_MODE_AUTO);
        	if (mCamParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//        		mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        		mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        	} else {
        		mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        	}
//        	mFocusButton.setText(Camera.Parameters.FOCUS_MODE_AUTO);
    		mCamera.setParameters(mCamParams);
//    		Toast.makeText(getApplicationContext(), "Camera Parameters set", Toast.LENGTH_SHORT).show();


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
			focusImg.setAlpha(255);
	        Log.d("Focused");
	        Toast.makeText(getApplicationContext(), "Focused", Toast.LENGTH_SHORT).show();
		}
	};

    public void onChangeCameraClick(View button){
    	int currentCameraId = mCamera.hashCode();

    	mCamera.stopPreview();
    	mCamera.release();

        if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        // Camera may be in use by another activity or the system or not available at all
        mCamera = null;
		try {
	        mCamera = Camera.open(currentCameraId);
		} catch (Exception e) {
			// Camera is not available or doesn't exist
			Log.d("Camera.open failed", e);
		}

        if(mCamera != null){
            cameraPreview.init(mCamera);
        } else {
            finish();
        }

/*
        mCamera.setCameraDisplayOrientation(currentCameraId);
        setCameraDisplayOrientation(CameraActivity.this, currentCameraId, mCamera);
        try {
            //this step is critical or preview on new camera will no know where to render to
        	mCamera.setPreviewDisplay(previewHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
*/
    }

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
		focusImg.setAlpha(100);
    	mCamera.autoFocus(_cameraFocused);
    }

	public void onFlashSettingClick(View button){
		// Get current flash mode
    	mCamParams = mCamera.getParameters();
		Log.d("Current flash mode: " + mCamParams.getFlashMode());

		// Find out next available mode
		Integer nextFlashModeIndex = mFlashModes.indexOf(mCamParams.getFlashMode()) + 1;
		if (nextFlashModeIndex >= mFlashModes.size()) {
			nextFlashModeIndex = 0;
		}
        Log.d( "Next flash mode: " + mFlashModes.get(nextFlashModeIndex) );

		// Set it
        mFlashButton.setText( mFlashModes.get(nextFlashModeIndex));
    	mCamParams.setFlashMode(mFlashModes.get(nextFlashModeIndex));
	    mCamera.setParameters(mCamParams);
	}

	public void onFocusSettingClick(View button){
/*
        PopupMenu mFocusModePopup = new PopupMenu(getBaseContext(), button);
        // Adding menu items to the popumenu
        mFocusModePopup.getMenuInflater().inflate(R.menu.popup_focus_mode, mFocusModePopup.getMenu());
        // Defining menu item click listener for the popup menu
        mFocusModePopup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(getBaseContext(), "You selected the action : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        // Showing the popup menu
        mFocusModePopup.show();
*/

        // Get current focus mode
    	mCamParams = mCamera.getParameters();
		Log.d("Current focus mode: " + mCamParams.getFocusMode());

		// Find out next available mode
		Integer nextFocusModeIndex = mFocusModes.indexOf(mCamParams.getFocusMode()) + 1;
		if (nextFocusModeIndex >= mFocusModes.size()) {
			nextFocusModeIndex = 0;
		}
        Log.d( "Next focus mode: " + mFocusModes.get(nextFocusModeIndex) );

		// Set it
        mFocusButton.setText( mFocusModes.get(nextFocusModeIndex));
    	mCamParams.setFocusMode(mFocusModes.get(nextFocusModeIndex));
	    mCamera.setParameters(mCamParams);
	}

	public void onSaveClick(View button){
        String path = _savePictureToFileSystem(mPictureData);
        // releaseCamera();
        setResult(path);
        finish();
	}

	public void onDiscardClick(View button){
        mCamera.startPreview();
	}

	@Override
    public void onPictureTaken(byte[] data, Camera camera) {
		mPictureData = data;
		mCamera = camera;

        // Show the Save/Discard buttons
        mSaveButton.setVisibility(View.VISIBLE);
        mDiscardButton.setVisibility(View.VISIBLE);

		Log.d("Picture taken");
        Toast.makeText(getApplicationContext(), "Picture taken", Toast.LENGTH_LONG).show();
    }

    private static String _savePictureToFileSystem(byte[] data) {
        File file = _getOutputMediaFile();
        _saveToFile(data, file);
        return file.getAbsolutePath();
    }

    private int _getFrontCameraId() {
        CameraInfo ci = new CameraInfo();
        for (int i = 0 ; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) return i;
        }
        return -1; // No front-facing camera found
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
        	mCamera.stopPreview();
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
