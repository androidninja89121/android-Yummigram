package com.zhs1.Yummigram.takephoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.aviary.android.feather.sdk.AviaryIntent;
import com.aviary.android.feather.sdk.internal.Constants;
import com.aviary.android.feather.sdk.internal.filters.ToolLoaderFactory;
import com.aviary.android.feather.sdk.internal.headless.utils.MegaPixels;
import com.aviary.android.feather.sdk.internal.utils.DecodeUtils;
import com.aviary.android.feather.sdk.internal.utils.ImageInfo;
import com.zhs1.Yummigram.R;
import com.zhs1.Yummigram.global.Global;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import it.sephiroth.android.library.exif2.ExifInterface;

import static android.graphics.Bitmap.createBitmap;


public class TakePhotoActivity extends Activity implements SurfaceHolder.Callback, OnClickListener, Camera.ShutterCallback, Camera.PictureCallback{
	Camera mCamera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean isPreviewRunning = false;
	boolean isFlashOn = false;
	boolean isFrontCamera = false;
	Button btnClose, btnRear, btnFlash, btnShutter, btnGallery;

    public static final String LOG_TAG = TakePhotoActivity.class.getName();
    private static final String FOLDER_NAME = "aviary-sample";
    private static final int ACTION_REQUEST_GALLERY = 99;
    private static final int ACTION_REQUEST_FEATHER = 100;
    private static final int EXTERNAL_STORAGE_UNAVAILABLE = 1;

    String mOutputFilePath;
    Uri mImageUri;
    File mGalleryFolder;
	
	 /** Called when the activity is first created. */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_takephoto);
	     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	  
	     getWindow().setFormat(PixelFormat.UNKNOWN);
	     surfaceView = (SurfaceView)findViewById(R.id.surface);
	     surfaceHolder = surfaceView.getHolder();
	     surfaceHolder.addCallback(this);
	     surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	     
	     btnClose   = (Button)findViewById(R.id.btnCloseTakePhoto);
	     btnRear    = (Button)findViewById(R.id.btnRearTakePhoto);
	     btnFlash   = (Button)findViewById(R.id.btnFlashTakePhoto);
	     btnShutter = (Button)findViewById(R.id.btnShutterTakePhoto);
	     btnGallery = (Button)findViewById(R.id.btnGalleryTakePhoto);
	     
	     btnClose.setOnClickListener(this);
	     btnRear.setOnClickListener(this);
	     btnFlash.setOnClickListener(this);
	     btnShutter.setOnClickListener(this);
	     btnGallery.setOnClickListener(this);

         mGalleryFolder = createFolders();
	 }
	 
	 @Override
		public void onClick(View v) {
		 if(v == btnClose){
			 finish();
		 }
		 
		 if(v == btnRear || v == btnFlash){
	
			 if (isPreviewRunning)
	         {
	             mCamera.stopPreview();
	             isPreviewRunning = false;
	         }
			 
			
			 
			 if(v == btnFlash){
				 Parameters parameters = mCamera.getParameters();
				 
				 isFlashOn = !isFlashOn;
				 
				 if(isFlashOn){
					 parameters.setFlashMode(Parameters.FLASH_MODE_ON);
					 btnFlash.setBackgroundResource(R.drawable.btn_flash_on);
				 }else{
					 parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
					 btnFlash.setBackgroundResource(R.drawable.btn_flash_off);
				 }
				 
				 mCamera.setParameters(parameters);
				 
				 if (mCamera != null){
			       	 try {
				       	  mCamera.setPreviewDisplay(surfaceHolder);
				       	  mCamera.startPreview();
				       	  isPreviewRunning = true;
			       	 } catch (IOException e) {
			       	  // TODO Auto-generated catch block
			       		 e.printStackTrace();
			       	 }
		       	}   
				 
			 }else{
				 isFrontCamera = !isFrontCamera;
				 
				 int nCameraId = 0;
				 
				 if(isFrontCamera){
					 nCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
				 }else{
					 nCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
				 }
				 mCamera.stopPreview();
		         mCamera.release();
		         mCamera = null;
		         
				 mCamera = Camera.open(nCameraId);
				 
				 Parameters parameters = mCamera.getParameters();
			        
			        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
			        Camera.Size selected = sizes.get(0);
			        parameters.setPreviewSize(selected.width,selected.height);
			        
			        mCamera.setParameters(parameters);        
			        mCamera.setDisplayOrientation(90);
			        
			        if (mCamera != null){
				       	 try {
					       	  mCamera.setPreviewDisplay(surfaceHolder);
					       	  mCamera.startPreview();
					       	  isPreviewRunning = true;
				       	 } catch (IOException e) {
				       	  // TODO Auto-generated catch block
				       		 e.printStackTrace();
				       	 }
			       	}            
			 }
		 }
		 
		 if(v == btnShutter){
			 mCamera.takePicture(this, null, null, this);
		 }
		 
		 if(v == btnGallery){
			 pickFromGallery();
		 }
	 }

    @Override
    /**
     * This method is called when feather has completed ( ie. user clicked on "done" or just exit the activity without saving ).
     * <br />
     * If user clicked the "done" button you'll receive RESULT_OK as resultCode, RESULT_CANCELED otherwise.
     *
     * @param requestCode
     * 	- it is the code passed with startActivityForResult
     * @param resultCode
     * 	- result code of the activity launched ( it can be RESULT_OK or RESULT_CANCELED )
     * @param data
     * 	- the result data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTION_REQUEST_GALLERY:
                    // user chose an image from the gallery
                    startFeather(data.getData());
                    break;

                case ACTION_REQUEST_FEATHER:
                    Global.uriSharePhoto = data.getData();

                    Intent intent = new Intent(TakePhotoActivity.this, ShareActivity.class);

                    startActivity(intent);

                    finish();
                    break;

            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case ACTION_REQUEST_FEATHER:

                    // delete the result file, if exists
                    break;
            }
        }
    }

    /**
     * Check the external storage status
     *
     * @return
     */
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Once you've chosen an image you can start the feather activity
     *
     * @param uri
     */
    @SuppressWarnings ("deprecation")
    private void startFeather(Uri uri) {
        Log.d(LOG_TAG, "uri: " + uri);

        // first check the external storage availability
        if (!isExternalStorageAvailable()) {
            showDialog(EXTERNAL_STORAGE_UNAVAILABLE);
            return;
        }

        // create a temporary file where to store the resulting image
        File file = getNextFileName();

        if (null != file) {
            mOutputFilePath = file.getAbsolutePath();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage("Failed to create a new File").show();
            return;
        }

        ToolLoaderFactory.Tools[] toolList = {ToolLoaderFactory.Tools.EFFECTS, ToolLoaderFactory.Tools.FOCUS,
                ToolLoaderFactory.Tools.ENHANCE, ToolLoaderFactory.Tools.ORIENTATION, ToolLoaderFactory.Tools.LIGHTING};

        // Create the intent needed to start feather
        Intent newIntent = new AviaryIntent.Builder(this).setData(uri)
                .withOutput(Uri.parse("file://" + mOutputFilePath))
                .withOutputFormat(Bitmap.CompressFormat.JPEG)
                .withOutputSize(MegaPixels.Mp5).withNoExitConfirmation(true)
                .saveWithNoChanges(true).withPreviewSize(1024)
                .withToolList(toolList)
                .build();

        // ..and start feather
        startActivityForResult(newIntent, ACTION_REQUEST_FEATHER);

    }

    private File createFolders() {
        File baseDir;

        if (android.os.Build.VERSION.SDK_INT < 8) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        if (baseDir == null) {
            return Environment.getExternalStorageDirectory();
        }

        Log.d(LOG_TAG, "Pictures folder: " + baseDir.getAbsolutePath());
        File aviaryFolder = new File(baseDir, FOLDER_NAME);

        if (aviaryFolder.exists()) {
            return aviaryFolder;
        }
        if (aviaryFolder.mkdirs()) {
            return aviaryFolder;
        }

        return Environment.getExternalStorageDirectory();
    }

    /**
     * Return a new image file. Name is based on the current time. Parent folder
     * will be the one created with createFolders
     *
     * @return

     */
    private File getNextFileName() {
        if (mGalleryFolder != null) {
            if (mGalleryFolder.exists()) {
                File file = new File(
                        mGalleryFolder, "aviary_"
                        + System.currentTimeMillis() + ".jpg");
                return file;
            }
        }
        return null;
    }

	@Override
    public void onShutter() {
        Toast.makeText(this, "Click!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        //Here, we chose internal storage
        Bitmap bmpOfTheImageFromCamera = BitmapFactory.decodeByteArray(
                data, 0, data.length);

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmpOfTheImageFromCamera,surfaceView.getMeasuredWidth(),
                surfaceView.getMeasuredHeight(),true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmpOfTheImageFromCamera.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getBaseContext().getContentResolver(), rotatedBitmap,
                "Title", null);

        Uri imageUri = Uri.parse(path);

        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startFeather(imageUri);

        camera.startPreview();
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {            
        if (isPreviewRunning)
        {
            mCamera.stopPreview();
            isPreviewRunning = false;
        }

//        Parameters parameters = mCamera.getParameters();
//        
//        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//        Camera.Size selected = sizes.get(0);
//        parameters.setPreviewSize(selected.width,selected.height);
//        
//        mCamera.setParameters(parameters);        
//        mCamera.setDisplayOrientation(90);
        
        Parameters parameters = mCamera.getParameters();
        Display display = this.getWindowManager().getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(height, width);                           
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(width, height);                           
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(height, width);               
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            mCamera.setDisplayOrientation(180);
        }
        
        if (mCamera != null){
	       	 try {
		       	  mCamera.setPreviewDisplay(surfaceHolder);
		       	  mCamera.startPreview();
		       	  isPreviewRunning = true;
	       	 } catch (IOException e) {
	       	  // TODO Auto-generated catch block
	       		 e.printStackTrace();
	       	 }
       	}                      
    }

    /**
     * Start the activity to pick an image from the user gallery
     */
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        Intent chooser = Intent.createChooser(intent, "Choose a Picture");
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	// TODO Auto-generated method stub
	mCamera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	// TODO Auto-generated method stub
	mCamera.stopPreview();
	mCamera.release();
	mCamera = null;
	isPreviewRunning = false;
	}


}