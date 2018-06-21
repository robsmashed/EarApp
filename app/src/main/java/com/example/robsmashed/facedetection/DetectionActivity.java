package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Picture;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DetectionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        disableRotationAnim();

        findViewById(R.id.ToogleCameraFAB).getBackground().setAlpha(120);
        findViewById(R.id.discardButton).getBackground().setAlpha(120);
        findViewById(R.id.saveButton).getBackground().setAlpha(120);

        if(getIntent().getStringExtra("EAR").equals("Sinistro")) {
            ear = "SX";
            haarcascadeResource = R.raw.haarcascade_mcs_rightear;
        }
        else if(getIntent().getStringExtra("EAR").equals("Destro")) {
            ear = "DX";
            haarcascadeResource = R.raw.haarcascade_mcs_leftear;
        }
        subjectID = getIntent().getStringExtra("SUBJECT");
        session = getIntent().getStringExtra("SESSION");
        galleryMode = getIntent().getBooleanExtra("GALLERYMODE", false);
        cameraShutter = MediaPlayer.create(this, R.raw.camera_shutter);
        dbHelper = new DatabaseOpenHelper(this);
        buttons = findViewById(R.id.saveButtons);
        imagetoSaveView = findViewById(R.id.imageToSave);
        mOpenCvCameraView = findViewById(R.id.OpenCvView);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        NUMBER_OF_FRAMES_TO_DETECT = prefs.getInt("POSITIVEFRAMES", 15);
        MARGIN_ERROR_OF_FRAMES = prefs.getInt("NEGATIVEFRAMES", 2);
        FIXED_BORDER = prefs.getInt("FIXEDBORDER", 20);
        currentMarginError = MARGIN_ERROR_OF_FRAMES;
        
        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        rotation = display.getRotation();
        mOpenCvCameraView.setRotationValue(rotation);

        if(galleryMode) {
            if(!isPreview)
                pickPhotoFromGallery();
            else
                showPreview(true);
        } else {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
    }

    private void disableRotationAnim() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("ASSOCIATIONS", newAssociations);
        outState.putParcelableArrayList("PICTURES", newPictures);
        outState.putInt("CAMERA", mOpenCvCameraView.getCameraIndex());
        outState.putBoolean("PREVIEW", isPreview);
        if(isPreview)
            outState.putParcelable("IMAGE", preview);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int cameraIndex = savedInstanceState.getInt("CAMERA");
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        if(cameraIndex == 1)
            mIsFrontCamera = true;
        else
            mIsFrontCamera = false;
        if(savedInstanceState.getBoolean("PREVIEW")) {
            preview = savedInstanceState.getParcelable("IMAGE");
            showPreview(true);
        }

        newPictures = savedInstanceState.getParcelableArrayList("PICTURES");
        newAssociations = savedInstanceState.getParcelableArrayList("ASSOCIATIONS");
    }

    public void pickPhotoFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://media/internal/images/media"));
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode) {
            case 0:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    //To speed up loading of image
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap temp = BitmapFactory.decodeFile(picturePath, options);

                    //Rotating the image to get the correct orientation
                    try {
                        galleryBitmap = rotateBitmap(temp, picturePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Convert Bitmap to Mat
                    Bitmap tempBitmap = galleryBitmap.copy(Bitmap.Config.ARGB_8888,true); // galleryBitmap può essere null se l'immagine viene selezionata e deselezionata prima che Intent.ACTION_PICK si chiuda !!!
                    galleryMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
                    Utils.bitmapToMat(tempBitmap, galleryMat);
                    detectEarsOnGalleryMat();
                } else {
                    finish();
                }
                break;
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, String picturePath) throws IOException{
        ExifInterface imgParams = new ExifInterface(picturePath);
        int orientation = imgParams.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void takePicture(View v){
        // Salvataggio su db SQL
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(galleryMode)
            galleryBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        else
            preview.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte imageInByte[] = stream.toByteArray();
        Picture p = new Picture();
        p.setData(new SimpleDateFormat("dd/MM/yyyy-HH:mm").format(new Date()));
        p.setImmagine(imageInByte);
        p.setOrecchio(ear);
        p.setId(dbHelper.addPicture(p));
        if (p.getId()>0) {
            Toast.makeText(this, "Foto salvata", Toast.LENGTH_LONG).show();
            Association a = new Association();
            a.setId(dbHelper.addAssociation(p.getId(), Long.parseLong(subjectID), Long.parseLong(session)));
            a.setSoggetto_id(Long.parseLong(subjectID));
            a.setSessione_id(Long.parseLong(session));
            a.setImmagine_id(p.getId());
            newAssociations.add(a);
            newPictures.add(p);
        }
        else
            Toast.makeText(this, "Errore nel salvataggio!", Toast.LENGTH_LONG).show();

        // Salvataggio su file
        /*File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(path + File.separator + "EarDetection");
        boolean success = true;
        if (!folder.exists())
            success = folder.mkdirs();
        if (success) {
            FileOutputStream out = null;
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(path + "/EarDetection", "ear" + "_" + timeStamp + ".jpg");
            try {
                out = new FileOutputStream(file);
                preview.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "Salvo foto...", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Errore!", Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/

        hidePreview(findViewById(R.id.saveButton));
    }

    public void hidePreview(View v){
        isPreview = false;
        if(galleryMode)
            onBackPressed();
        else{
            canTakePicture = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imagetoSaveView.setVisibility(SurfaceView.GONE);
                    buttons.setVisibility(SurfaceView.GONE);
                    mOpenCvCameraView.setVisibility(View.VISIBLE);
            }
        });}
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        if(newAssociations.size()>0 && newPictures.size()>0) {
            resultIntent.putExtra("ASSOCIATIONS", newAssociations);
            resultIntent.putExtra("PICTURES", newPictures);
            setResult(Activity.RESULT_OK, resultIntent);
        } else
            setResult(Activity.RESULT_CANCELED, resultIntent);
        super.onBackPressed();
    }

    public void showPreview(final boolean restoreFromRotate){
        isPreview = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(galleryMode) {
                    imagetoSaveView.setImageBitmap(galleryBitmap);
                }
                else {
                    imagetoSaveView.setImageBitmap(preview);
                    if(!restoreFromRotate)
                        cameraShutter.start();
                }
                mOpenCvCameraView.setVisibility(View.INVISIBLE);
                imagetoSaveView.setVisibility(SurfaceView.VISIBLE);
                buttons.setVisibility(SurfaceView.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mOpenCVCallBack);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    public void detectEarsOnGalleryMat(){
        Mat grayMat = new Mat();
        Imgproc.cvtColor(galleryMat,grayMat,Imgproc.COLOR_BGR2GRAY);
        //Toast.makeText(this, String.valueOf(galleryMat.size().width) + "x" + String.valueOf(galleryMat.size().height), Toast.LENGTH_LONG).show();
        MatOfRect earRect = new MatOfRect();
        if(haarCascade != null)
            haarCascade.detectMultiScale(grayMat, earRect, 1.1, 2, 2, new Size(200,200), new Size());
        Rect[] earArray = earRect.toArray();

        if(earArray.length<=0){
            imagetoSaveView.setImageBitmap(galleryBitmap);
            imagetoSaveView.setVisibility(SurfaceView.VISIBLE);
            new AlertDialog.Builder(this)
                    .setTitle("Nessun orecchio rilevato")
                    .setMessage("Salvare comunque la foto?")
                    .setPositiveButton("Salva", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            takePicture(null);
                        }})
                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }).show();
        } else {
            showPreview(false);
        };
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mGray = inputFrame.gray();
        mRgba = inputFrame.rgba();

        if(rotation == Surface.ROTATION_0) {
            Core.rotate(mGray, mGray, Core.ROTATE_90_CLOCKWISE);
            Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE);
        } else if(rotation == Surface.ROTATION_270){
            Core.rotate(mGray, mGray, Core.ROTATE_180);
            Core.rotate(mRgba, mRgba, Core.ROTATE_180);
        }

        MatOfRect earRect = new MatOfRect();
        if(haarCascade != null)
            haarCascade.detectMultiScale(mGray, earRect, 1.1, 2, 2, new Size(200,200), new Size());

        Rect[] earArray = earRect.toArray();

        Mat cropedMat = mRgba.clone(); // creo copia prima di disegnare la regione dell'orecchio

        if(canTakePicture && earArray.length>0){ // se si ha la fotocamera su schermo e si è rilevato un orecchio
            // allarga l'eventuale regione rilevata con un bordo di dimensione fissa
            int fixedX = earArray[0].x - (FIXED_BORDER);
            int fixedY = earArray[0].y - (FIXED_BORDER);
            int fixedWidth = earArray[0].width + (FIXED_BORDER * 2);
            int fixedHeight = earArray[0].height + (FIXED_BORDER * 2);

            if (fixedX>=0 && fixedY >= 0 && fixedWidth+fixedX<=cropedMat.width() && fixedHeight+fixedY<=cropedMat.height()){ // controlla se i bordi aggiunti non superano i limiti dell'immagine originale
                Imgproc.rectangle(mRgba, earArray[0].tl(), earArray[0].br(), new Scalar(100), 3);
                consecutiveDetects++; //frame positivo
            } else { // frame negativo
                if(currentMarginError>0 && consecutiveDetects>0) // se vi è ancora margine di errore
                    currentMarginError--; // riduci possibilità di errore
                else { // altrimenti resetta
                    consecutiveDetects = 0;
                    currentMarginError = MARGIN_ERROR_OF_FRAMES;
                }
            }

            if(consecutiveDetects>NUMBER_OF_FRAMES_TO_DETECT) {
                earArray[0].x = fixedX;
                earArray[0].y = fixedY;
                earArray[0].height = fixedHeight;
                earArray[0].width = fixedWidth;
                cropedMat = new Mat(cropedMat, earArray[0]);
                preview = Bitmap.createBitmap(cropedMat.cols(), cropedMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropedMat, preview);

                canTakePicture = false;
                consecutiveDetects = 0;
                currentMarginError = MARGIN_ERROR_OF_FRAMES;

                showPreview(false);
            }
        }

        if(rotation == Surface.ROTATION_0) {
            Core.rotate(mRgba, mRgba, Core.ROTATE_90_COUNTERCLOCKWISE);
        } else if(rotation == Surface.ROTATION_270){
            Core.rotate(mRgba, mRgba, Core.ROTATE_180);
        }
        return mRgba;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy(){
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mItemSwitchCamera = menu.add("Toggle Front/Back camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemSwitchCamera)
            onToogleCamera(null);
        return true;
    }

    public void onToogleCamera(View v){
        String toastMessage = "";
        mOpenCvCameraView.setVisibility(SurfaceView.GONE);
        mIsFrontCamera = !mIsFrontCamera;
        if (mIsFrontCamera) {
            mOpenCvCameraView.setCameraIndex(1);
            toastMessage = "Front Camera";
        } else {
            mOpenCvCameraView.setCameraIndex(-1);
            toastMessage = "Back Camera";
        }
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();
        Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        toast.show();
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    try{
                        // Create a new folder cascade and copy the contents of the cascade file to a new file in that folder.
                        InputStream is = getResources().openRawResource(haarcascadeResource);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir,"cascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while((bytesRead = is.read(buffer)) != -1)
                            os.write(buffer, 0, bytesRead);
                        is.close();
                        os.close();

                        // Create a new CascadeClassifier object that will be used to detect faces in the camera feed
                        haarCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                            if (haarCascade.empty()) {
                            Log.i("Cascade Error","Failed to load cascade classifier");
                            haarCascade = null;
                        }
                    }
                    catch(Exception e) {
                        Log.i("Cascade Error: ","Cascase not found");
                    }
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private ArrayList<Association> newAssociations = new ArrayList<>();
    private ArrayList<Picture> newPictures = new ArrayList<>();
    private int rotation;
    private boolean isPreview = false;
    private boolean galleryMode;
    private String subjectID;
    private int haarcascadeResource;
    private CameraBridgeViewBase mOpenCvCameraView;
    private RelativeLayout buttons;
    private ImageView imagetoSaveView;
    private File mCascadeFile;
    private CascadeClassifier haarCascade;
    private Mat mRgba;
    private boolean mIsFrontCamera = false;
    private MenuItem mItemSwitchCamera;
    private boolean canTakePicture = true;
    private Bitmap preview;
    private int consecutiveDetects = 0;
    private MediaPlayer cameraShutter = null;
    private String ear, session;
    private DatabaseOpenHelper dbHelper;
    private Bitmap galleryBitmap = null;
    private Mat galleryMat;

    // settings
    private SharedPreferences prefs;
    private int NUMBER_OF_FRAMES_TO_DETECT;
    private int MARGIN_ERROR_OF_FRAMES;
    private int currentMarginError;
    private int FIXED_BORDER;
}
