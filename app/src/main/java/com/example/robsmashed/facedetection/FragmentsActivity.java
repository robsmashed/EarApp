package com.example.robsmashed.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.example.robsmashed.facedetection.Classes.Association;
import com.example.robsmashed.facedetection.Classes.CustomAdapterNav;
import com.example.robsmashed.facedetection.Classes.DatabaseOpenHelper;
import com.example.robsmashed.facedetection.Classes.Picture;
import com.example.robsmashed.facedetection.Classes.Session;
import com.example.robsmashed.facedetection.Classes.Subject;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FragmentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        navListView = findViewById(R.id.navListView);
        fabMenu = findViewById(R.id.multipleActions);
        noPhotosText = findViewById(R.id.noPhotosText);
        setTitle("Ear Detection");
        dbHelper = new DatabaseOpenHelper(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        List<String> categories = new ArrayList<>();
        categories.add("Foto");
        categories.add("Sessioni");
        categories.add("Soggetti");
        categories.add("Impostazioni");
        customAdapter = new CustomAdapterNav(this, R.layout.list_element, categories);
        navListView.setAdapter(customAdapter);

        fm = getSupportFragmentManager();
        groupBySubject = prefs.getBoolean("GROUPBY", true);

        new AsyncTask(savedInstanceState).execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("ASSOCIATIONS", associations);
        outState.putParcelableArrayList("SESSIONS", sessions);
        outState.putParcelableArrayList("PICTURES", pictures);
        outState.putParcelableArrayList("SUBJECTS", subjects);
    }

    public void onItemClick(View view){
        if(view.getTag().toString().equals("1"))
            manageSessions();
        else if(view.getTag().toString().equals("2"))
            manageSubjects();
        else if(view.getTag().toString().equals("3")) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("DATABASENAME", dbHelper.getDatabaseName());
            startActivity(intent);
        }
        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(fabMenu.isExpanded())
            fabMenu.collapse();
    }

    // chiude FAM quando si clicca altrove, se aperto
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(fabMenu.isExpanded()){
                Rect outRect = new Rect();
                fabMenu.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY())){
                    fabMenu.collapse();
                    return false;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void manageSubjects() {
        Intent intent = new Intent(this, SubjectsActivity.class);
        intent.putParcelableArrayListExtra("SUBJECTS", subjects);
        startActivityForResult(intent, 4);
    }

    public void manageSessions(){
        Intent intent = new Intent(this, SessionsActivity.class);
        intent.putParcelableArrayListExtra("SESSIONS", sessions);
        startActivityForResult(intent, 5);
    }

    private void refresh(){
        // rimuovi tutti gli eventuali fragment attivi
        for(Fragment fragment:getSupportFragmentManager().getFragments())
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        if(groupBySubject){ // mostra immagini per soggetto
            for(Subject s:subjects){ // crea un fragment per ogni soggetto
                // controlla se vi è almeno una foto
                Boolean empty = true;
                for(int i=0; i<associations.size() && empty; i++){
                    if(associations.get(i).getSoggetto_id()==s.getID())
                        empty=false;
                }
                if(!empty){ // crea fragment solo se il soggetto ha qualche foto
                    PhotoGroupFragment f = new PhotoGroupFragment();
                    Bundle args = new Bundle();
                    args.putString("TYPE", "SUBJECT"); // indica che si tratta di un soggetto
                    args.putParcelableArrayList("LIST", pictures); // passa le foto del soggetto
                    args.putParcelable("SUBJECT", s); // passa soggetto
                    args.putParcelableArrayList("ASSOCIATIONS", associations);
                    args.putParcelableArrayList("SUBJECTS", subjects);
                    args.putParcelableArrayList("SESSIONS", sessions);
                    f.setArguments(args);
                    fm.beginTransaction().add(R.id.rootLayout, f).commit(); // aggiungi fragment alla view
                }
            }
        }else{ // mostra immagini per sessioni
            for(Session s:sessions){ // crea un fragment per ogni sessione
                // controlla se vi è almeno una foto
                Boolean empty = true;
                for(int i=0; i<associations.size() && empty; i++){
                    if(associations.get(i).getSessione_id()==s.getId())
                        empty=false;
                }
                if(!empty){ // crea fragment solo se la sessione ha qualche foto
                    PhotoGroupFragment f = new PhotoGroupFragment();
                    Bundle args = new Bundle();
                    args.putString("TYPE", "SESSION"); // indica che si tratta di una sessione
                    args.putParcelableArrayList("LIST", pictures); // passa le foto della sessione
                    args.putParcelable("SESSION", s); // passa sessione
                    args.putParcelableArrayList("ASSOCIATIONS", associations);
                    args.putParcelableArrayList("SUBJECTS", subjects);
                    args.putParcelableArrayList("SESSIONS", sessions);
                    f.setArguments(args);
                    fm.beginTransaction().add(R.id.rootLayout, f).commit(); // aggiungi fragment alla view
                }
            }
        }

        // se non ci sono foto mostra messaggio a schermo
        if(pictures.size()==0)
            noPhotosText.setVisibility(View.VISIBLE);
        else
            noPhotosText.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if(groupBySubject){
            mItemGroup = menu.add("Raggruppa per sessioni");
        } else
            mItemGroup = menu.add("Raggruppa per soggetti");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == mItemGroup){
            if(groupBySubject){
                item.setTitle("Raggruppa per soggetti");
            } else{
                item.setTitle("Raggruppa per sessioni");
            }
            groupBySubject = !groupBySubject;
            saveStateToPreferences();
            refresh();
        } else if(item.getItemId()==android.R.id.home){
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }

    private void saveStateToPreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("GROUPBY", groupBySubject);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==Activity.RESULT_OK) {
            if (requestCode == 1) { // BrowseGroupPhotosActivity
                associations = data.getExtras().getParcelableArrayList("ASSOCIATIONS");
                sessions = data.getExtras().getParcelableArrayList("SESSIONS");
                subjects = data.getExtras().getParcelableArrayList("SUBJECTS");
                ArrayList<Picture> editedPictures = data.getExtras().getParcelableArrayList("PICTURES");
                // Aggiorna la lista delle foto con le eventuali modifiche
                for(int j=0; j<pictures.size(); j++){
                    Boolean found = false;
                    for(int i = 0; i< editedPictures.size() && !found; i++){
                        if(editedPictures.get(i).getId()==pictures.get(j).getId()) { // se la foto non è stata cancellata
                            found = true;
                            pictures.get(j).setOrecchio(editedPictures.get(i).getOrecchio()); // aggiorna foto
                        }
                    }
                    if(!found){ // cancella la foto se non è più presente
                        pictures.remove(j);
                        j--;
                    }
                }
            } else if(requestCode==2){ // ViewPhotoActivity
                associations = data.getExtras().getParcelableArrayList("ASSOCIATIONS");
                Picture newPic = data.getExtras().getParcelable("PICTURE");

                if(data.getBooleanExtra("DELETE", false)){ // cancellazione della singola foto
                    Boolean found = false;
                    for(int i=0; i<pictures.size() && !found; i++){
                        if(pictures.get(i).getId()==newPic.getId()) {
                            pictures.remove(i);
                            found = true;
                        }
                    }
                } else { // modifica della singola foto
                    Boolean found = false;
                    for(int i=0; i<pictures.size() && !found; i++){
                        if(pictures.get(i).getId()==newPic.getId()) {
                            pictures.get(i).setOrecchio(newPic.getOrecchio());
                            found = true;
                            Log.d("SESSIONE", String.valueOf(pictures.get(i).findAssociation(associations).getSessione_id()));
                        }
                    }
                }
            } else if(requestCode==3){ // addPhotoActivity
                ArrayList<Association> newAssociations = data.getExtras().getParcelableArrayList("ASSOCIATIONS");
                ArrayList<Picture> newPictures = data.getExtras().getParcelableArrayList("PICTURES");
                if(newPictures.size()>0 && newAssociations.size()>0) {
                    ArrayList<Long> newPicturesIDs = new ArrayList<>();
                    for (Picture p : newPictures)
                        newPicturesIDs.add(p.getId());
                    pictures.addAll(dbHelper.getPictures(newPicturesIDs));
                    associations.addAll(newAssociations);
                }
            } else if(requestCode==5){ // sessionsActivity
                // aggiorna la lista delle sessioni
                sessions = data.getExtras().getParcelableArrayList("SESSIONS");
                ArrayList<Session> deletedSessions = data.getExtras().getParcelableArrayList("DELETEDSESSIONS");

                // elimina eventuali foto di sessioni non più esistenti
                for (int i = 0; i < pictures.size(); i++) {
                    Boolean found = false;
                    for(int j=0; j<deletedSessions.size() && !found; j++){
                        if(pictures.get(i).findAssociation(associations).getSessione_id()==deletedSessions.get(j).getId()) {
                            found = true;
                            pictures.remove(i);
                            i--;
                        }
                    }
                }
                // elimina eventuali associazioni non più esistenti
                for(Session deletedSession: deletedSessions) {
                    for (int i = 0; i < associations.size(); i++) {
                        if(associations.get(i).getSessione_id()==deletedSession.getId()){
                            associations.remove(i);
                            i--;
                        }
                    }
                }
            } else if(requestCode==4){ // subjectsActivity
                subjects = data.getExtras().getParcelableArrayList("SUBJECTS");

                // elimina eventuali foto di soggetti non più esistenti
                ArrayList<Long> deletedIDs = (ArrayList<Long>) data.getSerializableExtra("DELETEDIDS");
                for(Long id: deletedIDs) {
                    for (int i = 0; i < pictures.size(); i++) {
                        if (pictures.get(i).findAssociation(associations).getSoggetto_id() == id){
                            pictures.remove(i);
                            i--;
                        }
                    }
                }
            }
            refresh();
        }
    }

    public void addPhotoActivity(View v){
        Intent intent = new Intent(this, addPhotoActivity.class);
        intent.putExtra("TYPE", v.getTag().toString());
        intent.putParcelableArrayListExtra("SUBJECTS", subjects);
        intent.putParcelableArrayListExtra("SESSIONS", sessions);
        startActivityForResult(intent, 3);
    }

    private class AsyncTask extends android.os.AsyncTask<Void, Integer, String>{
        private Bundle savedInstanceState;

        AsyncTask(Bundle savedInstanceState){
            this.savedInstanceState = savedInstanceState;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.photosLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.pbHeaderProgress).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            if(savedInstanceState != null){
                associations = savedInstanceState.getParcelableArrayList("ASSOCIATIONS");
                sessions = savedInstanceState.getParcelableArrayList("SESSIONS");
                pictures = savedInstanceState.getParcelableArrayList("PICTURES");
                subjects = savedInstanceState.getParcelableArrayList("SUBJECTS");
            } else {
                // crea una copia del database
                associations = dbHelper.getAllAssociations(); // ottieni tutte le associazioni
                pictures = dbHelper.getAllPictures(); // ottieni tutte le immagini
                subjects = dbHelper.getAllSubjects(); // ottieni tutti i soggetti
                sessions = dbHelper.getAllSessions();  // ottieni tutte le sessioni
            }
            refresh();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            findViewById(R.id.photosLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.pbHeaderProgress).setVisibility(View.INVISIBLE);
        }
    }

    private ArrayList<Association> associations;
    private ArrayList<Picture> pictures;
    private ArrayList<Session> sessions;
    private ArrayList<Subject> subjects;
    private ListView navListView;
    private CustomAdapterNav customAdapter;
    private DrawerLayout mDrawerLayout;
    private TextView noPhotosText;
    private FloatingActionsMenu fabMenu;
    private FragmentManager fm;
    private DatabaseOpenHelper dbHelper;
    private boolean groupBySubject = true;
    private MenuItem mItemGroup;
    private SharedPreferences prefs;
}
