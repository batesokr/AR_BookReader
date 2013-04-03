package edu.muohio.batesokr.renderscript_example;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.ImageView;

public class CameraPreview extends SurfaceView implements Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private int mCamWidth, mCamHeight;
	private Context mContext;
	
	private final String TAG = "CAMERA";
	
	
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mContext = context;
		
		// Install a SurfaceHolder.Callback so we  get notified
		// when the underlying surface is created or destroyed
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if(mHolder.getSurface() == null){
			// Preview surface does not exist
			return;
		}
		
		// Stop the preview before making changes
		try{
			mCamera.stopPreview();
		}catch(Exception e){
			// Ignore - tried to stop a non-existent preview
		}
		
		// Get Camera Preview parameters
		Camera.Parameters params = mCamera.getParameters();
		
		// Get supported sizes for the Camera Preview
		ArrayList<Camera.Size> supportedSizes = (ArrayList<Camera.Size>) params.getSupportedPreviewSizes();
		
		//ArrayList<Integer> supportedFormats = (ArrayList<Integer>) params.getSupportedPreviewFormats();
		
		// Set Camera Preview size for the Camera parameters -- Using the NV21 format
		mCamWidth = supportedSizes.get(0).width; 
		mCamHeight = supportedSizes.get(0).height;
		params.setPreviewSize(mCamWidth, mCamHeight);
		
		// Set Camera parameters
		mCamera.setParameters(params);
		
		// Start preview with new settings
		try{
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			
			// Set a callback for the preview
			mCamera.setPreviewCallback(new CameraPreviewCallback());
		}catch(Exception e){
			Log.d(TAG, "Error starting Camera preview: "+e.getMessage());
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The surface view has been created - tell the camera 
		// where the draw the preview
		try{
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		}catch(Exception e){
			Log.d(TAG, "Error setting camera preview: "+e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Empty - take care of releasing the Camera in the activity
	}
	
	class CameraPreviewCallback implements PreviewCallback{

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			int format = camera.getParameters().getPreviewFormat();
			//int bits = ImageFormat.getBitsPerPixel(format);
			
			// Conversion of Y'UV420P (NV21) to ARGB_8888 (for Bitmap)
			int size = mCamWidth*mCamHeight;
			int offset = size;
			int[] pixels = new int[size];
			int u, v, y1, y2, y3, y4;
			for(int i=0, k=0; i < size; i+=2, k+=2){
				y1 = data[i] & offset;
				y2 = data[i+1] & offset;
				y3 = data[mCamWidth+i] & offset;
				y4 = data[mCamWidth+i+1] & offset;
				
				u = data[offset+k] & offset;
				v = data[offset+k+1] & offset;
				u = u-128;
				v = v-128;
				
				pixels[i] = convertYUVtoARGB(y1, u, v);
				pixels[i] = convertYUVtoARGB(y2, u, v);
				pixels[mCamWidth+i] = convertYUVtoARGB(y3, u, v);
				pixels[mCamWidth+i+1] = convertYUVtoARGB(y4, u, v);
				
				if((i != 0) && ((i+2)%mCamWidth == 0)){
					i += mCamWidth;
				}
				
				Log.d("CAMERA", String.valueOf(i)+", "+String.valueOf(size));
				
			
			}
			
			// Convert ARGB_888 pixel data to Bitmap
			DisplayMetrics metrics = new DisplayMetrics();
			((Camera_Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			//Bitmap bitmap = Bitmap.createBitmap(metrics, pixels, mWidth, mHeight, Bitmap.Config.ARGB_8888);
			Bitmap bitmap = Bitmap.createBitmap(pixels, mCamWidth, mCamHeight, Bitmap.Config.ARGB_8888);
			ImageView imageView = (ImageView) ((Camera_Activity) mContext).findViewById(R.id.bitmap);
			imageView.setImageBitmap(bitmap);
			
			//mHolder.lockCanvas();
			//mHolder.unlockCanvasAndPost(new Canvas());
		}
		
		private int convertYUVtoARGB(int y, int u, int v){
			int r, g, b;
			
			r = y + (int)1.420f*v;
			g = y - (int)(0.344f*u + 0.714f*v);
			b = y + (int)1.772f*u;
			
			// Clamp values between 0 and 255
			r = r>255 ? 255 : (r<0 ? 0 : r);
			g = g>255 ? 255 : (g<0 ? 0 : g);
			b = b>255 ? 255 : (b<0 ? 0 : b);
			
			return 0xff000000 | (r<<16) | (g<<8) | b;
		}
	}
}
