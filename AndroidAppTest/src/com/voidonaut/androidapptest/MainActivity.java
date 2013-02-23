package com.voidonaut.androidapptest;

import com.voidonaut.androidapptest.FromXML;
import com.voidonaut.androidapptest.R;
import com.voidonaut.androidapptest.util.BitmapHelper;
import com.voidonaut.androidapptest.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

//import android.util.Log;
//import android.view.Menu;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.voidonaut.androidapptest.MESSAGE";
    private static final int CAMERA_PIC_REQUEST = 1337;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String message = "Click the button below to start";
        if(cameraNotDetected()){
         message = "No camera detected, clicking the button below will have unexpected behaviour.";
        }
        TextView cameraDescriptionTextView = (TextView) findViewById(R.id.text_view_camera_description);
        cameraDescriptionTextView.setText(message);
	}

    private boolean cameraNotDetected() {
        return !getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** Called when the user clicks the Send button */
    @FromXML
    public void onUseCameraClick(View view) {
//    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    	Intent cameraIntent = new Intent(this, CameraActivity.class);
    	startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public void onUseDefaultCameraClick(View view) {
    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    	startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if(requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK){
            String imgPath = data.getStringExtra(CameraActivity.EXTRA_IMAGE_PATH);
            Log.i("Got image path: "+ imgPath);
            displayImage(imgPath);
        }
        else if(requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_CANCELED){
                Log.i("User didn't take an image");
        }
    }

    private void displayImage(String path) {
        ImageView imageView = (ImageView) findViewById(R.id.photo_result_view);
        imageView.setImageBitmap(BitmapHelper._decodeSampledBitmap(path, 300, 250));
    }

/*
    // Called when the user clicks the Send button
    public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
*/



}
