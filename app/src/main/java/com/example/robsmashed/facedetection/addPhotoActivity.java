package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class addPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);
        setTitle("Aggiungi foto");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String[] ears = {"Sinistro", "Destro"};

        earSelect = findViewById(R.id.ear_spinner);
        subjectSelect = findViewById(R.id.soggetto_spinner);
        sessionSelect = findViewById(R.id.sessione_spinner);

        ArrayAdapter<String> earsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ears);
        earSelect.setAdapter(earsAdapter);
        earSelect.setSelection(prefs.getInt("LASTEAR", 0));

        // spinner soggetti
        subjects = getIntent().getExtras().getParcelableArrayList("SUBJECTS");
        if(subjects.size()==0) {
            Subject fake = new Subject();
            fake.setNome("Nessun");
            fake.setCognome("soggetto");
            subjects.add(fake);
            subjectSelect.setEnabled(false);
        } else
            subjectSelect.setEnabled(true);
        subjectsArray = new String[subjects.size()];
        for(int i=0; i<subjects.size(); i++)
            subjectsArray[i] = subjects.get(i).getNome() + " " + subjects.get(i).getCognome();
        subjectsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjectsArray);
        subjectSelect.postInvalidate();
        subjectSelect.setAdapter(subjectsAdapter);
        String lastSubject = prefs.getString("LASTSUBJECT", "");
        if(lastSubject.equals(""))
            subjectSelect.setSelection(0);
        else {
            for (int i = 0; i < subjectsArray.length; i++) {
                if (subjectsArray[i].equals(lastSubject))
                    subjectSelect.setSelection(i);
            }
        }

        // spinner sessioni
        sessions = getIntent().getExtras().getParcelableArrayList("SESSIONS");
        openedSessions = new ArrayList<>();
        for(int i=0; i<sessions.size(); i++) {
            if(sessions.get(i).isOpen())
                openedSessions.add(sessions.get(i));
        }
        if(openedSessions.size()==0){
            sessionsArray = new String[1];
            sessionsArray[0] = "Nessuna sessione attiva";
            sessionSelect.setEnabled(false);
        } else{
            sessionSelect.setEnabled(true);
            sessionsArray = new String[openedSessions.size()];
            for(int i=0; i<openedSessions.size(); i++)
                sessionsArray[i] = "Sessione " + String.valueOf(openedSessions.get(i).getNumber());
        }
        sessionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sessionsArray);
        sessionSelect.postInvalidate();
        sessionSelect.setAdapter(sessionsAdapter);
        String lastSession = prefs.getString("LASTSESSION", "");
        if(lastSession.equals(""))
            sessionSelect.setSelection(0);
        else {
            for (int i = 0; i < sessionsArray.length; i++) {
                if (sessionsArray[i].equals(lastSession))
                    sessionSelect.setSelection(i);
            }
        }

        //pulsante avanti
        if(!subjectSelect.isEnabled() || !sessionSelect.isEnabled())
            ((TextView)findViewById(R.id.nextButton)).setTextColor(Color.parseColor("#BDBDBD"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveStateToPreferences();
    }

    private void saveStateToPreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LASTSUBJECT", subjectSelect.getSelectedItem().toString());
        editor.putString("LASTSESSION", sessionSelect.getSelectedItem().toString());
        editor.putInt("LASTEAR", earSelect.getSelectedItemPosition());
        editor.commit();
    }

    public void addPhoto(View view) {
        if(!subjectSelect.isEnabled() && !sessionSelect.isEnabled())
            Toast.makeText(getApplicationContext(), "Aggiungi prima un soggetto e una sessione", Toast.LENGTH_LONG).show();
        if(!subjectSelect.isEnabled())
            Toast.makeText(getApplicationContext(), "Aggiungi prima un soggetto", Toast.LENGTH_LONG).show();
        else if(!sessionSelect.isEnabled())
            Toast.makeText(getApplicationContext(), "Aggiungi prima una sessione", Toast.LENGTH_LONG).show();
        else {
            saveStateToPreferences();
            Intent intent = new Intent(this, DetectionActivity.class);
            if (getIntent().getStringExtra("TYPE").equals("gallery")) {
                intent.putExtra("EAR", earSelect.getSelectedItem().toString());
                intent.putExtra("SUBJECT", String.valueOf(subjects.get(subjectSelect.getSelectedItemPosition()).getID()));
                intent.putExtra("SESSION", String.valueOf(openedSessions.get(sessionSelect.getSelectedItemPosition()).getId()));
                intent.putExtra("GALLERYMODE", true);
            } else {
                intent.putExtra("EAR", earSelect.getSelectedItem().toString());
                intent.putExtra("SUBJECT", String.valueOf(subjects.get(subjectSelect.getSelectedItemPosition()).getID()));
                intent.putExtra("SESSION", String.valueOf(openedSessions.get(sessionSelect.getSelectedItemPosition()).getId()));
                intent.putExtra("GALLERYMODE", false);
            }
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==Activity.RESULT_OK) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ASSOCIATIONS", data.getExtras().getParcelableArrayList("ASSOCIATIONS"));
                resultIntent.putExtra("PICTURES", data.getExtras().getParcelableArrayList("PICTURES"));
                setResult(Activity.RESULT_OK, resultIntent);
            }
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    private Spinner earSelect, subjectSelect, sessionSelect;
    private String[] subjectsArray, sessionsArray;
    private ArrayAdapter<String> subjectsAdapter, sessionsAdapter;
    private ArrayList<Subject> subjects;
    private ArrayList<Session> sessions;
    private ArrayList<Session> openedSessions;
    private SharedPreferences prefs;
}
