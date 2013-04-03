package edu.muohio.batesokr.renderscript_example;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Camera_Activity extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
	public Bitmap bitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);
		
		// Attempt to get a camera
		getCamera();
		
		//Set bitmap
		
		
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		releaseCamera();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		// Attempt to get Camera one is not retrieved yet
		if(mCamera == null)
			getCamera();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Nothing right now
		return true;
	}
	
	/**
	 * Gets a camera to use. 
	 */
	private void getCamera(){
		// Check if a camera exists
		if (checkCameraHardware(this)) {
			// Get a camera instance
			mCamera = getCameraInstance();
			if (mCamera != null) {
				// Camera was successfully obtained - start showing preview
				mPreview = new CameraPreview(this, mCamera);
				FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
				frameLayout.addView(mPreview);
			}
		}
	}
	
	/**
	 * Checks if a camera exists on this device.
	 * @param context The context of the activity.
	 * @return if a camera exists.
	 */
	private boolean checkCameraHardware(Context context){
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// This device does have a camera
			return true;
		}else{
			// This device does not have a camera
			return false;
		}
	}
	
	/**
	 * Get a Camera instance to use
	 * @return a Camera Instance
	 */
	private Camera getCameraInstance(){
		Camera c = null;
		try{
			c = Camera.open();
		}catch(Exception e){
			// Camera is not available
		}
		
		return c;
	}
	
	/**
	 * Release the Camera, if this Activity is using it
	 */
	private void releaseCamera(){
		// Check if the Activity is using the camera
		if(mCamera != null){
			// Release the camera
			mCamera.release();
			mCamera = null;
		}
	}
}
