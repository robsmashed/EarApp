package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.CustomAdapterSessions;
import com.example.robsmashed.facedetection.Classes.CustomAdapterSubjects;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sessioni");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        setContentView(R.layout.activity_sessions);
        listView = findViewById(R.id.listview);

        if(savedInstanceState==null) {
            sessions = getIntent().getExtras().getParcelableArrayList("SESSIONS");
            edited = false;
            deletedSessions = new ArrayList<>();
        } else {
            sessions = savedInstanceState.getParcelableArrayList("SESSIONS");
            edited = savedInstanceState.getBoolean("EDITED");
            deletedSessions = savedInstanceState.getParcelableArrayList("DELETEDSESSIONS");
        }
        update();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("SESSIONS", sessions);
        outState.putBoolean("EDITED", edited);
        outState.putParcelableArrayList("DELETEDSESSIONS", deletedSessions);
    }

    private void update(){
        ArrayList<Session> listToShow = ( ArrayList<Session>) sessions.clone();
        listToShow.add(fakeSession);
        customAdapter = new CustomAdapterSessions(this, R.layout.list_element, listToShow);
        listView.setAdapter(customAdapter);
    }

    public void onItemClick(View view){
        if(view.getTag().toString().equals("add")){
            addSession();
        } else{
            final int position = Integer.parseInt(view.getTag().toString());
            final Session s = customAdapter.getItem(position);
            Intent intent = new Intent(this, SessionDetailActivity.class);
            intent.putExtra("SESSION", s);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addSession() {
        final DatabaseOpenHelper dbHelper;
        dbHelper = new DatabaseOpenHelper(this);

        new AlertDialog.Builder(this)
                .setTitle("Crea sessione")
                .setMessage("Creare una nuova sessione?")
                .setPositiveButton("Crea", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        long id = dbHelper.addSession();
                        if(id>0) {
                            Toast.makeText(getApplicationContext(), "Sessione creata", Toast.LENGTH_LONG).show();
                            Session s = new Session();
                            s.setId(id);
                            int n;
                            if(sessions.size()==0)
                                n = 1;
                            else
                                n = sessions.get(sessions.size()-1).getNumber() + 1;
                            s.setNumber(n);
                            s.setOpen(true);
                            sessions.add(s);
                            edited = true;
                            update();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Errore durante la creazione", Toast.LENGTH_LONG).show();
                    }})
                .setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // non fare niente
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        if(edited) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SESSIONS", sessions);
            resultIntent.putExtra("DELETEDSESSIONS", deletedSessions);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==Activity.RESULT_OK){
            long id = data.getLongExtra("SESSION", -1);
            if(data.getBooleanExtra("DELETE", false)){ // la sessione è stata cancellata
                Session deletedSession = new Session();
                deletedSession.setId(id);
                deletedSessions.add(deletedSession);
                boolean found = false;
                for(int i=0; i<sessions.size() && !found; i++){
                    if(sessions.get(i).getId()==id) {
                        found = true;
                        sessions.remove(i);
                    }
                }
                edited = true;
            } else {
                if(data.getBooleanExtra("EDITED", false)) { // la sessione è stata modificata
                    boolean found = false;
                    for (int i = 0; i < sessions.size() && !found; i++) {
                        if (sessions.get(i).getId() == id) {
                            found = true;
                            sessions.get(i).setOpen(!sessions.get(i).isOpen());
                        }
                    }
                    edited = true;
                }
            }
            update();
        }
    }

    private ArrayList<Session> deletedSessions;
    private boolean edited;
    private Session fakeSession = new Session();
    private ArrayList<Session> sessions;
    private CustomAdapterSessions customAdapter;
    private ListView listView;
}
