package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.util.ArrayList;

public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        setContentView(R.layout.activity_view_photo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.button1).getBackground().setAlpha(0);
        findViewById(R.id.button2).getBackground().setAlpha(0);
        findViewById(R.id.button3).getBackground().setAlpha(0);

        mDecorView = getWindow().getDecorView();
        shadow1 = findViewById(R.id.shadow1);
        shadow2 = findViewById(R.id.shadow2);
        showSystemUI();
        areBarsVisible = true;
        mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            areBarsVisible = true;
                        } else {
                            areBarsVisible = false;
                        }
                    }
                });

        myGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {}

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {}

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

        });

        gestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (myGestureDetector.onTouchEvent(event))
                    return true;

                if(areBarsVisible) {
                    hideSystemUI();
                    buttonsToolbar.setVisibility(View.INVISIBLE);
                }
                else {
                    showSystemUI();
                    buttonsToolbar.setVisibility(View.VISIBLE);
                }
                return false;
            }
        };

        photoToShow = findViewById(R.id.photoToShow);
        photoToShow.setOnTouchListener(gestureListener);
        buttonsToolbar = findViewById(R.id.buttonsToolbar);

        dbHelper = new DatabaseOpenHelper(this);

        if(savedInstanceState==null) {
            associations = getIntent().getExtras().getParcelableArrayList("ASSOCIATIONS");
            sessions = getIntent().getExtras().getParcelableArrayList("SESSIONS");
            subjects = getIntent().getExtras().getParcelableArrayList("SUBJECTS");
            Picture Picture = getIntent().getExtras().getParcelable("PICTURE");
            p = dbHelper.getPicture(Picture.getId());
            edited = getIntent().getBooleanExtra("EDITED", false);
            update();
        } else{
            sessions = savedInstanceState.getParcelableArrayList("SESSIONS");
            subjects = savedInstanceState.getParcelableArrayList("SUBJECTS");
            associations = savedInstanceState.getParcelableArrayList("ASSOCIATIONS");
            p = savedInstanceState.getParcelable("PICTURE");
            session = savedInstanceState.getParcelable("SESSION");
            subject = savedInstanceState.getParcelable("SUBJECT");
            edited = savedInstanceState.getBoolean("EDITED");
        }

        Bitmap bm = BitmapFactory.decodeByteArray(p.getImmagine(), 0, p.getImmagine().length);
        photoToShow.setImageBitmap(bm);
        Log.d("EDITED", String.valueOf(edited));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("SESSIONS", sessions);
        outState.putParcelableArrayList("SUBJECTS", subjects);
        outState.putParcelableArrayList("ASSOCIATIONS", associations);
        outState.putParcelable("SUBJECT", subject);
        outState.putParcelable("SESSION", session);
        outState.putParcelable("PICTURE", p);
        outState.putBoolean("EDITED", edited);
    }

    private void update(){
        for(Session s:sessions){ // trova sessione della foto
            if(s.getId()==p.findAssociation(associations).getSessione_id())
                session = s;
        }
        for(Subject s:subjects){ // trova soggetto della foto
            if(s.getID()==p.findAssociation(associations).getSoggetto_id())
                subject = s;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        shadow1.setVisibility(View.INVISIBLE);
        shadow2.setVisibility(View.INVISIBLE);
    }

    // This snippet shows the system bars.
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        shadow1.setVisibility(View.VISIBLE);
        shadow2.setVisibility(View.VISIBLE);
    }

    public void onDeleteClick(View v){
        new AlertDialog.Builder(this)
                .setTitle("Conferma eliminazione")
                .setMessage("Eliminare definitivamente?")
                .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(dbHelper.deletePicture(p.getId())>0) {
                            Toast.makeText(getApplicationContext(), "Foto eliminata", Toast.LENGTH_LONG).show();
                            boolean found = false;
                            for(int j=0; j<associations.size() && !found; j++){
                                if(associations.get(j).getImmagine_id()==p.getId()) {
                                    associations.remove(j);
                                    found = true;
                                }
                            }
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("DELETE", true);
                            returnIntent.putExtra("ASSOCIATIONS", associations);
                            returnIntent.putExtra("PICTURE", p);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Errore durante l'eliminazione", Toast.LENGTH_LONG).show();
                    }})
                .setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // non fare niente
                    }
                }).show();
    }

    public void onInfoClick(View v){
        Intent intent = new Intent(this, PhotoInfoActivity.class);
        intent.putExtra("SUBJECT", subject);
        intent.putExtra("PICTURE", p);
        intent.putExtra("SESSION", session);
        startActivity(intent);
    }

    public void onEditClick(View v){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        String shareBody = "Here is the share content body";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mItemEdit = menu.add("Modifica attributi");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemEdit){
            Intent intent = new Intent(this, PhotoEditActivity.class);
            intent.putExtra("SUBJECT", subject);
            intent.putExtra("PICTURE", p);
            intent.putExtra("SESSION", session);
            intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
            intent.putParcelableArrayListExtra("SESSIONS", getIntent().getExtras().getParcelableArrayList("SESSIONS"));
            intent.putParcelableArrayListExtra("SUBJECTS", getIntent().getExtras().getParcelableArrayList("SUBJECTS"));
            intent.putExtra("EDITED", edited);
            startActivityForResult(intent, 1);
        }
        else if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode == Activity.RESULT_OK){
            Picture Picture = data.getExtras().getParcelable("PICTURE");
            p.update(Picture);
            associations = data.getExtras().getParcelableArrayList("ASSOCIATIONS");
            edited = true;
            update();
        }
        Log.d("EDITED", String.valueOf(edited));
    }

    @Override
    public void onBackPressed() {
        if(edited) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("PICTURE", p);
            returnIntent.putExtra("DELETE", false);
            returnIntent.putExtra("ASSOCIATIONS", associations);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        super.onBackPressed();
    }

    private ArrayList<Session> sessions;
    private ArrayList<Subject> subjects;
    private ArrayList<Association> associations;
    private Subject subject;
    private Session session;
    private Picture p;
    private boolean edited;

    private DatabaseOpenHelper dbHelper;
    private FrameLayout shadow1, shadow2;
    private LinearLayout buttonsToolbar;
    private MenuItem mItemEdit;
    private boolean areBarsVisible;
    private View.OnTouchListener gestureListener;
    private GestureDetector myGestureDetector;
    private View mDecorView;
    private ImageView photoToShow;
}
