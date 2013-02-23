package com.voidonaut.androidapptest;

import static com.voidonaut.androidapptest.util.CameraHelper._cameraAvailable;
import static com.voidonaut.androidapptest.util.CameraHelper._getCameraInstance;
import static com.voidonaut.androidapptest.util.MediaHelper._getOutputMediaFile;
import static com.voidonaut.androidapptest.util.MediaHelper._saveToFile;

import java.io.File;

import com.voidonaut.androidapptest.FromXML;
import com.voidonaut.androidapptest.R;
import com.voidonaut.androidapptest.CameraPreview;
import com.voidonaut.androidapptest.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;

import android.os.Build;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.annotation.TargetApi;


/**
 * Takes a photo saves it to the SD card and returns the path of this photo to the calling Activity
 * @author paul.blundell
 *
 */
public class CameraActivity extends Activity implements PictureCallback {

    protected static final String EXTRA_IMAGE_PATH = "com.voidonaut.androidapptest.CameraActivity.EXTRA_IMAGE_PATH";

    private Camera camera;
    private CameraPreview cameraPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
        setResult(RESULT_CANCELED);
        // Camera may be in use by another activity or the system or not available at all
        camera = _getCameraInstance();
        if(_cameraAvailable(camera)){
            initCameraPreview();
        } else {
            finish();
        }
        /* Show the Up button in the action bar. */
        //  setupActionBar();
	}

    /* Show the camera view on the activity */
    private void initCameraPreview() {
        cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        cameraPreview.init(camera);
    }

    @FromXML
    public void onCaptureClick(View button){
        // Take a picture with a callback when the photo has been created
        // Here you can add callbacks if you want to give feedback when the picture is being taken
        camera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d("Picture taken");
        String path = savePictureToFileSystem(data);
        setResult(path);
        finish();
    }

    private static String savePictureToFileSystem(byte[] data) {
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
        if(camera != null){
            camera.release();
            camera = null;
        }
    }

    /* Set up the {@link android.app.ActionBar}, if the API is available. */
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

}
