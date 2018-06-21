package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.util.ArrayList;

public class PhotoEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_edit);
        setTitle("Modifica attributi");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        soggetto = findViewById(R.id.soggettoText);
        sessione = findViewById(R.id.sessioneText);
        orecchio = findViewById(R.id.orecchioText);

        if(savedInstanceState==null) {
            edited = getIntent().getBooleanExtra("EDITED", false);
            p = getIntent().getExtras().getParcelable("PICTURE");
            subject = getIntent().getExtras().getParcelable("SUBJECT");
            session = getIntent().getExtras().getParcelable("SESSION");
            associations = getIntent().getExtras().getParcelableArrayList("ASSOCIATIONS");
            subjects = getIntent().getExtras().getParcelableArrayList("SUBJECTS");
            sessions = getIntent().getExtras().getParcelableArrayList("SESSIONS");
        } else {
            edited = savedInstanceState.getBoolean("EDITED", false);
            p = savedInstanceState.getParcelable("PICTURE");
            subject = savedInstanceState.getParcelable("SUBJECT");
            session = savedInstanceState.getParcelable("SESSION");
            associations = savedInstanceState.getParcelableArrayList("ASSOCIATIONS");
            subjects = savedInstanceState.getParcelableArrayList("SUBJECTS");
            sessions = savedInstanceState.getParcelableArrayList("SESSIONS");
        }

        update();
        Log.d("EDITED", String.valueOf(edited));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("EDITED", false);
        outState.putParcelable("PICTURE", p);
        outState.putParcelable("SBUJECT", subject);
        outState.putParcelable("SESSION", session);
        outState.putParcelableArrayList("ASSOCIATIONS", associations);
        outState.putParcelableArrayList("SUBJECTS", subjects);
        outState.putParcelableArrayList("SESSIONS", sessions);
    }

    private void update(){
        soggetto.setText(subject.getNome() + " " + subject.getCognome());
        sessione.setText("Sessione " + session.getNumber());
        if(p.getOrecchio().equals("SX"))
            orecchio.setText("Sinistro");
        else
            orecchio.setText("Destro");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    public void onSoggettoClick(View v){
        Intent intent = new Intent(this, EditPhotoAttributeActivity.class);
        intent.putExtra("TYPE", "SUBJECT");
        intent.putParcelableArrayListExtra("SUBJECTS", subjects);
        intent.putExtra("PICTURE", p);
        intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
        startActivityForResult(intent, 1);
    }

    public void onSessioneClick(View view){
        Intent intent = new Intent(this, EditPhotoAttributeActivity.class);
        intent.putExtra("TYPE", "SESSION");
        intent.putParcelableArrayListExtra("SESSIONS", sessions);
        intent.putExtra("PICTURE", p);
        intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
        startActivityForResult(intent, 1);
    }

    public void onOrecchioClick(View view) {
        Intent intent = new Intent(this, EditPhotoAttributeActivity.class);
        intent.putExtra("TYPE", "PICTURE");
        intent.putExtra("PICTURE", p);
        intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            edited = true;
            p = data.getExtras().getParcelable("PICTURE");
            associations = data.getExtras().getParcelableArrayList("ASSOCIATIONS");
            // aggiorna soggetto
            for(Subject s: subjects){
                if(p.findAssociation(associations).getSoggetto_id()==s.getID())
                    subject = s;
            }
            // aggiorna sessione
            for(Session s: sessions){
                if(p.findAssociation(associations).getSessione_id()==s.getId())
                    session = s;
            }
            update();
        }
        Log.d("EDITED", String.valueOf(edited));
    }

    @Override
    public void onBackPressed() {
        if(edited) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("PICTURE", p);
            returnIntent.putExtra("ASSOCIATIONS", associations);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        super.onBackPressed();
    }

    private ArrayList<Subject> subjects;
    private ArrayList<Session> sessions;
    private ArrayList<Association> associations;
    private Subject subject;
    private Session session;
    private Picture p;
    private boolean edited;

    private TextView soggetto;
    private TextView sessione;
    private TextView orecchio;
}
