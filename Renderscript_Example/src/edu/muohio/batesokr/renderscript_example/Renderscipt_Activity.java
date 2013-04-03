package edu.muohio.batesokr.renderscript_example;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.RenderScript;
import android.renderscript.Allocation;
import android.widget.ImageView;

public class Renderscipt_Activity extends Activity {
	private Bitmap mBitmapIn, mBitmapOut;
	private RenderScript mRS;
	private Allocation mInAllocation, mOutAllocation;
	private ScriptC_mono mScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.renderscript_activity);
        
        mBitmapIn = loadBitmap(R.drawable.land);
        mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(), mBitmapIn.getHeight(), mBitmapIn.getConfig());
        
        ImageView in = (ImageView) findViewById(R.id.bitmap_in);
        in.setImageBitmap(mBitmapIn);
        
        ImageView out = (ImageView) findViewById(R.id.bitmap_out);
        out.setImageBitmap(mBitmapOut);
    
        createScript();
    }

    private void createScript(){
    	mRS = RenderScript.create(this);
    	mInAllocation = Allocation.createFromBitmap(mRS, mBitmapIn, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
    	mOutAllocation = Allocation.createTyped(mRS,  mInAllocation.getType());
    	mScript = new ScriptC_mono(mRS, getResources(), R.raw.mono);
    	mScript.forEach_root(mInAllocation, mOutAllocation);
    	mOutAllocation.copyTo(mBitmapOut);
    }
    
    private Bitmap loadBitmap(int resource){
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    	
    	return BitmapFactory.decodeResource(getResources(), resource, options);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.renderscript_activity, menu);
        return true;
    }
    
}
