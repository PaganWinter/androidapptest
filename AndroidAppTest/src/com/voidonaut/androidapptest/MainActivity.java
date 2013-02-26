package com.voidonaut.androidapptest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.voidonaut.androidapptest.util.BitmapHelper;
import com.voidonaut.androidapptest.util.Log;

//import android.util.Log;
//import android.view.Menu;

public class MainActivity extends Activity {
	public final static String EXTRA_OVERLAY_IMG_PATH = "com.voidonaut.androidapptest.OVERLAY_IMG_PATH";
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

        // List existing Series

        _generateSeriesList();

	}

    private boolean cameraNotDetected() {
        return !getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** Called when the user clicks the Send button */
    @FromXML
    public void onUseCameraClick(View view) {
//    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    	Intent cameraIntent = new Intent(this, CameraActivity.class);
    	String overLayImagePath = "/storage/emulated/0/Pictures/COAA/test/IMG_20130222_133833.jpg";

    	cameraIntent.putExtra(EXTRA_OVERLAY_IMG_PATH, overLayImagePath);
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

    private void _generateSeriesList() {
//    	http://wptrafficanalyzer.in/blog/listview-with-images-and-text-using-simple-adapter-in-android/

    	List<HashMap<String,String>> mSeriesListData = new ArrayList<HashMap<String,String>>();

	    TextView debug = (TextView) findViewById(R.id.main_debug);
	    String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/COAA/";
	    Log.d(path);
	    File[] mDirs = new File(path).listFiles();
	    for ( File mDir : mDirs ) {
	    	Log.d(mDir.getName());
	        if ( mDir.isDirectory() ) {
	            String dirPath = Environment.getExternalStorageDirectory().toString()+"/Pictures/COAA/" + mDir.getName();
	            File[] mFiles = new File(dirPath).listFiles();
	            for ( File mFile : mFiles ) {
	            	Log.d(mFile.getName());
	            }
	            HashMap<String, String> hm = new HashMap<String,String>();
	            hm.put("series_name", mDir.getName());
	            hm.put("series_img_first", mFiles[0].getAbsolutePath());
	//            hm.put("series_img_last", );
	            mSeriesListData.add(hm);
	        }
	    }
	
	    // Keys used in Hashmap
	    String[] from = { "series_img_first", "series_name" }; 
	    // Ids of views in listview_series_item
	    int[] to = { R.id.series_img_first, R.id.series_name };
	
	    // Instantiating an adapter to store each items
	    // R.layout.listview_series_item defines the layout of each item
	    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), mSeriesListData, R.layout.listview_series_item, from, to);
	
	    // Getting a reference to listview_series_list of main.xml layout file
	    final ListView listView = ( ListView ) findViewById(R.id.listview_series_list);
	
	    // Setting the adapter to the listView
	    listView.setAdapter(adapter);

	    listView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Log.d( "Position: " + position );
	            Log.d( "Id: " + Long.toString(id) );
	            Log.d( "View id: " + Integer.toString(v.getId()) );
	            Log.d( "Item: " + parent.getAdapter().getItem(position).toString() );
	            //listView.getItemAtPosition(position);
//	            Cursor c = (Cursor) parent.getAdapter().getItem(position);
//	            Log.d( "Item: " + c.getString(c.getColumnIndex("series_img_first")) );
	        }
	    });
	};

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
