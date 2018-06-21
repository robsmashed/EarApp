package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.CustomAdapterPictures;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;

import java.util.ArrayList;

public class BrowseGroupPhotosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_group_photos);
        listView = findViewById(R.id.listview);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        noSubjectsText = findViewById(R.id.noSubjectsText);
        dbHelper = new DatabaseOpenHelper(this);

        type = getIntent().getStringExtra("TYPE");
        session = getIntent().getExtras().getParcelable("SESSION");
        subject = getIntent().getExtras().getParcelable("SUBJECT");
        pictures = getIntent().getExtras().getParcelableArrayList("PICTURES");
        associations =  getIntent().getExtras().getParcelableArrayList("ASSOCIATIONS");
        sessions = getIntent().getExtras().getParcelableArrayList("SESSIONS");
        subjects = getIntent().getExtras().getParcelableArrayList("SUBJECTS");

        if(savedInstanceState==null) {
            edited = false;
            ArrayList<Long> photosIDsInGroup = new ArrayList<>();
            if (type.equals("SUBJECT")) {
                // cerca tutte le foto del soggetto
                for (Association a : associations) {
                    if (a.getSoggetto_id() == subject.getID())
                        photosIDsInGroup.add(a.getImmagine_id());
                }
            } else if (type.equals("SESSION")) {
                // cerca tutte le foto della sessione
                for (Association a : associations) {
                    if (a.getSessione_id() == session.getId())
                        photosIDsInGroup.add(a.getImmagine_id());
                }
            }
            photosInGroup = dbHelper.getPictures(photosIDsInGroup);
        } else {
            edited = savedInstanceState.getBoolean("EDITED", false);
            photosInGroup = savedInstanceState.getParcelableArrayList("PHOTOSINGROUP");
        }
        refresh();
        Log.d("EDITED", String.valueOf(edited));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("EDITED", edited);
        outState.putParcelableArrayList("PHOTOSINGROUP", photosInGroup);
    }

    private void refresh(){
        if (type.equals("SUBJECT")) {
            // cerca le foto che non sono più del soggetto
            for(int i=0; i<photosInGroup.size(); i++){
                if(photosInGroup.get(i).findAssociation(associations).getSoggetto_id()!=subject.getID()) {
                    photosInGroup.remove(i);
                    i--;
                }
            }
        } else if (type.equals("SESSION")) {
            // cerca le foto che non appartengono più alla sessione
            for(int i=0; i<photosInGroup.size(); i++){
                if(photosInGroup.get(i).findAssociation(associations).getSessione_id()!=session.getId()) {
                    photosInGroup.remove(i);
                    i--;
                }
            }
        }

        if(type.equals("SESSION")) {
            customAdapter = new CustomAdapterPictures(this, R.layout.list_photo, photosInGroup);
            setTitle("Sessione " + session.getNumber());
        } else if(type.equals("SUBJECT")){
            setTitle(subject.getNome() + " " + subject.getCognome());
            customAdapter = new CustomAdapterPictures(this, R.layout.list_photo, photosInGroup);
        }
        listView.setAdapter(customAdapter);
        if(customAdapter.getCount()==0)
            noSubjectsText.setVisibility(View.VISIBLE);
        else
            noSubjectsText.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        //mItemSelect = menu.add("Seleziona...");
        if(getIntent().getStringExtra("TYPE").equals("SESSION")){
            mItemDelete = menu.add("Elimina sessione");
        } else
            mItemDelete = menu.add("Elimina soggetto");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == mItemDelete){
            if(getIntent().getStringExtra("TYPE").equals("SESSION")){
                new AlertDialog.Builder(this)
                        .setTitle("Attenzione!")
                        .setMessage("Quest'azione comporterà l'eliminazione di tutte le foto della sessione, procedere comunque?")
                        .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                long id = session.getId();
                                if(dbHelper.deleteSession(id)>0) {
                                    Toast.makeText(getApplicationContext(), "Sessione eliminata", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent();
                                    for(int i = 0; i< pictures.size(); i++){ // cancella le immagini della sessione
                                        if(pictures.get(i).findAssociation(associations).getSessione_id()==id) { // se l'immagine fa parte della sessione
                                            pictures.remove(i);
                                            i--;
                                        }
                                    }
                                    for(int j=0; j<associations.size(); j++){ // cancella le associazioni della sessione
                                        if(associations.get(j).getSessione_id()==id) {
                                            associations.remove(j);
                                            j--;
                                        }
                                    }
                                    boolean found = false;
                                    for(int i=0; i<sessions.size() && !found; i++){ // cerca e cancella la sessione
                                        if(sessions.get(i).getId()==id) {
                                            sessions.remove(i);
                                            found = true;
                                        }
                                    }
                                    intent.putParcelableArrayListExtra("SESSIONS", sessions);
                                    intent.putParcelableArrayListExtra("SUBJECTS", subjects);
                                    intent.putParcelableArrayListExtra("PICTURES", pictures);
                                    intent.putExtra("ASSOCIATIONS", associations);
                                    setResult(RESULT_OK, intent);
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
            } else{
                new AlertDialog.Builder(this)
                        .setTitle("Attenzione!")
                        .setMessage("Quest'azione comporterà l'eliminazione di tutte le foto del soggetto, procedere comunque?")
                        .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                long id = subject.getID();
                                if(dbHelper.deleteSubject(id)>0) {
                                    Toast.makeText(getApplicationContext(), "Soggetto eliminato", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent();
                                    for(int i = 0; i< pictures.size(); i++){ // cancella le immagini del soggetto
                                        if(pictures.get(i).findAssociation(associations).getSoggetto_id()==id) { // se l'immagine è del soggetto
                                            pictures.remove(i);
                                            i--;
                                        }
                                    }
                                    for(int j=0; j<associations.size(); j++){ // cancella le associazioni del soggetto
                                        if(associations.get(j).getSoggetto_id()==id) {
                                            associations.remove(j);
                                            j--;
                                        }
                                    }
                                    boolean found = false;
                                    for(int i=0; i<subjects.size() && !found; i++){ // cerca e cancella il soggetto
                                        if(subjects.get(i).getID()==id) {
                                            subjects.remove(i);
                                            found = true;
                                        }
                                    }
                                    intent.putParcelableArrayListExtra("SESSIONS", sessions);
                                    intent.putParcelableArrayListExtra("SUBJECTS", subjects);
                                    intent.putParcelableArrayListExtra("PICTURES", pictures);
                                    intent.putExtra("ASSOCIATIONS", associations);
                                    setResult(RESULT_OK, intent);
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
        }
        else if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    public void onPhotoClick(View view){
        Picture p = customAdapter.getItem(Integer.parseInt(view.getTag().toString()));
        Intent intent = new Intent(this, ViewPhotoActivity.class);
        intent.putExtra("PICTURE", p);
        intent.putExtra("EDITED", edited);
        intent.putParcelableArrayListExtra("SESSIONS", sessions);
        intent.putParcelableArrayListExtra("SUBJECTS", subjects);
        intent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK && requestCode==1){
            edited = true;
            associations = data.getExtras().getParcelableArrayList("ASSOCIATIONS");
            Picture newPic = data.getExtras().getParcelable("PICTURE");
            if(!data.getBooleanExtra("DELETE", false)){ // eventuale modifica della singola foto
                for(Picture p: pictures){
                    if(p.getId()==newPic.getId())
                        p.setOrecchio(newPic.getOrecchio());
                }
            } else { // cancella foto
                boolean found = false;
                for(int i=0; i<pictures.size() && !found; i++){ // cancella dalla lista di tutte le immagini
                    if(pictures.get(i).getId()==newPic.getId()) {
                        found = true;
                        pictures.remove(i);
                    }
                }
                found = false;
                for(int i=0; i<photosInGroup.size() && !found; i++){ // cancella dalla lista delle immagini da mostrare
                    if(photosInGroup.get(i).getId()==newPic.getId()) {
                        found = true;
                        photosInGroup.remove(i);
                    }
                }
            }
            refresh();
        }
        Log.d("EDITED", String.valueOf(edited));
    }

    @Override
    public void onBackPressed() {
        if(edited) {
            Intent returnIntent = new Intent();
            returnIntent.putParcelableArrayListExtra("PICTURES", pictures);
            returnIntent.putParcelableArrayListExtra("ASSOCIATIONS", associations);
            returnIntent.putParcelableArrayListExtra("SESSIONS", sessions);
            returnIntent.putParcelableArrayListExtra("SUBJECTS", subjects);
            setResult(Activity.RESULT_OK, returnIntent);
        }
        super.onBackPressed();
    }

    private boolean edited;
    private ArrayList<Subject> subjects;
    private ArrayList<Session> sessions;
    private ArrayList<Picture> photosInGroup;
    private String type;
    private ArrayList<Association> associations;
    private DatabaseOpenHelper dbHelper;
    private MenuItem mItemDelete;
    private Session session;
    private Subject subject;
    private TextView noSubjectsText;
    private CustomAdapterPictures customAdapter;
    private GridView listView;
    private ArrayList<Picture> pictures;
}
