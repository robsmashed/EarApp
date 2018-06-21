package com.example.robsmashed.facedetection.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "earDetection";

    // Table Names
    private static final String TABLE_SUBJECTS = "soggetti";
    private static final String TABLE_PICTURES = "immagini";
    private static final String TABLE_SUBJECTS_PICTURES = "soggetti_immagini";
    private static final String TABLE_SESSIONS = "sessioni";

    // Common column names
    private static final String KEY_ID = "id";

    // SUBJECTS Table - column names
    private static final String KEY_SURNAME = "cognome";
    private static final String KEY_NAME = "nome";
    private static final String KEY_BIRTHDATE = "datadinascita";

    // PICTURES Table - column names
    private static final String KEY_PIC = "immagine";
    private static final String KEY_EAR = "orecchio";
    private static final String KEY_DATE = "data";

    // SUBJECTS_PICTURES Table - column names
    private static final String KEY_SUBJECT_ID = "soggetto_id";
    private static final String KEY_PICTURE_ID = "immagine_id";
    private static final String KEY_SESSION_ID = "sessione_id";

    // SESSIONS Table - column names
    private static final String KEY_OPEN = "aperta";
    private static final String KEY_NUMBER = "numero";


    // Table Create Statements
    private final static String CREATE_TABLE_SOGGETTI =
            "CREATE TABLE IF NOT EXISTS "+TABLE_SUBJECTS+" ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_SURNAME + " TEXT NOT NULL, "
                    + KEY_BIRTHDATE + " TEXT NOT NULL);";

    private final static String CREATE_TABLE_IMMAGINI =
            "CREATE TABLE IF NOT EXISTS "+TABLE_PICTURES+" ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_PIC + " BLOB, "
                    + KEY_EAR + " TEXT NOT NULL, "
                    + KEY_DATE + " TEXT NOT NULL);";

    private final static String CREATE_TABLE_SOGGETTI_IMMAGINI =
            "CREATE TABLE IF NOT EXISTS "+TABLE_SUBJECTS_PICTURES+" ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_SUBJECT_ID + " TEXT NOT NULL, "
                    + KEY_PICTURE_ID + " TEXT NOT NULL, "
                    + KEY_SESSION_ID + " TEXT NOT NULL);";

    private final static String CREATE_TABLE_SESSIONI =
            "CREATE TABLE IF NOT EXISTS "+TABLE_SESSIONS+" ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NUMBER + " INGEGER NOT NULL, "
                    + KEY_OPEN + " BOOLEAN NOT NULL DEFAULT 1);";

    public long addSubject(Subject s){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, s.getNome());
        values.put(KEY_SURNAME, s.getCognome());
        values.put(KEY_BIRTHDATE, s.getDataDiNascita());
        return db.insert(TABLE_SUBJECTS, null, values);
    }

    public long addPicture(Picture p){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PIC, p.getImmagine());
        values.put(KEY_EAR, p.getOrecchio());
        values.put(KEY_DATE, p.getData());
        long id = db.insert(TABLE_PICTURES, null, values);
        return id;
    }

    public long addSession(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        List<Session> sessions = getAllSessions();
        int n;
        if(sessions.size()==0)
            n = 1;
        else
            n = sessions.get(sessions.size()-1).getNumber() + 1;
        values.put(KEY_NUMBER, n);
        values.put(KEY_OPEN, 1);
        return db.insert(TABLE_SESSIONS, null, values);
    }

    public long addAssociation(long pic_id, long sub_id, long session_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PICTURE_ID, pic_id);
        values.put(KEY_SUBJECT_ID, sub_id);
        values.put(KEY_SESSION_ID, session_id);
        return db.insert(TABLE_SUBJECTS_PICTURES, null, values);
    }

    public long deleteSubject(long subject_id){
        SQLiteDatabase db = this.getWritableDatabase();
        List<Picture> pictures;

        pictures = getSubjectPictures(subject_id);
        for(int i=0; i<pictures.size(); i++)
            db.delete(TABLE_PICTURES, KEY_ID + " = ?", new String[]{String.valueOf(pictures.get(i).getId())}); // cancella immagini del soggetto
        db.delete(TABLE_SUBJECTS_PICTURES, KEY_SUBJECT_ID + " = ?", new String[]{String.valueOf(subject_id)}); // cancella associazioni
        if(db.delete(TABLE_SUBJECTS, KEY_ID + " = ?", new String[] { String.valueOf(subject_id) })>0) // cancella soggetto
            return 1;
        else
            return 0;
    }

    public long deletePicture(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_SUBJECTS_PICTURES, KEY_PICTURE_ID + " = ?", new String[]{String.valueOf(id)}); // cancella associazioni
        if(db.delete(TABLE_PICTURES, KEY_ID + " = ?", new String[]{String.valueOf(id)})>0) // cancella immagine del soggetto
            return 1;
        else
            return 0;
    }

    public int deleteSession(long session_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Picture> pictures;

        pictures = getSessionPictures(session_id);
        for(int i=0; i<pictures.size(); i++)
            db.delete(TABLE_PICTURES, KEY_ID + " = ?", new String[]{String.valueOf(pictures.get(i).getId())}); // cancella immagini della sessione
        db.delete(TABLE_SUBJECTS_PICTURES, KEY_SESSION_ID + " = ?", new String[]{String.valueOf(session_id)}); // cancella associazioni
        if(db.delete(TABLE_SESSIONS, KEY_ID + " = ?", new String[] { String.valueOf(session_id) })>0) // cancella sessione
            return 1;
        else
            return 0;
    }

    public ArrayList<Subject> getAllSubjects(){
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        String selectQuery = "SELECT  * FROM " + TABLE_SUBJECTS + " ORDER BY " + KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    Subject s = new Subject();
                    s.setID(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    s.setNome(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    s.setCognome(cursor.getString(cursor.getColumnIndex(KEY_SURNAME)));
                    s.setDataDiNascita(cursor.getString(cursor.getColumnIndex(KEY_BIRTHDATE)));
                    subjects.add(s);
                } while (cursor.moveToNext());
            
        }
        return subjects;
    }

    public ArrayList<Session> getAllSessions() {
        ArrayList<Session> sessions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SESSIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    Session s = new Session();
                    s.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    s.setNumber(cursor.getInt(cursor.getColumnIndex(KEY_NUMBER)));
                    s.setOpen(cursor.getInt(cursor.getColumnIndex(KEY_OPEN)) > 0);
                    sessions.add(s);
                } while (cursor.moveToNext());
            
        }
        return sessions;
    }

    public ArrayList<Picture> getAllPictures() {
        ArrayList<Picture> pictures = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PICTURES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    Picture p = new Picture();
                    p.setOrecchio(cursor.getString(cursor.getColumnIndex(KEY_EAR)));
                    p.setData(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                    p.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    p.setImmagine(cursor.getBlob(cursor.getColumnIndex(KEY_PIC)));
                    pictures.add(p);
                }
                while (cursor.moveToNext());
            
        }
        return pictures;
    }

    public ArrayList<Association> getAllAssociations() {
        ArrayList<Association> associations = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SUBJECTS_PICTURES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                Association p = new Association();
                p.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                p.setImmagine_id(cursor.getLong(cursor.getColumnIndex(KEY_PICTURE_ID)));
                p.setSessione_id(cursor.getLong(cursor.getColumnIndex(KEY_SESSION_ID)));
                p.setSoggetto_id(cursor.getLong(cursor.getColumnIndex(KEY_SUBJECT_ID)));
                associations.add(p);
            } while(cursor.moveToNext());
        }
        return associations;
    }

    public Session getSession(long id) {
        Session s = new Session();
        String selectQuery = "SELECT * FROM " + TABLE_SESSIONS + " WHERE " + KEY_ID + "=" + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    s.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    s.setNumber(cursor.getInt(cursor.getColumnIndex(KEY_NUMBER)));
                    s.setOpen(cursor.getInt(cursor.getColumnIndex(KEY_OPEN)) > 0);
                } while (cursor.moveToNext());
            
        }
        return s;
    }

    public Subject getSubject(long id) {
        Subject s = new Subject();
        String selectQuery = "SELECT * FROM " + TABLE_SUBJECTS + " WHERE " + KEY_ID + "=" + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    s.setID(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    s.setNome(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    s.setCognome(cursor.getString(cursor.getColumnIndex(KEY_SURNAME)));
                    s.setDataDiNascita(cursor.getString(cursor.getColumnIndex(KEY_BIRTHDATE)));
                } while (cursor.moveToNext());
            
        }
        return s;
    }

    public Picture getPicture(double id) {
        String selectQuery = "SELECT  * FROM " + TABLE_PICTURES + " WHERE " + KEY_ID + "=" + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Picture p = new Picture();
        if (cursor.moveToFirst()) {
            
                do {
                    p.setData(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                    p.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    p.setImmagine(cursor.getBlob(cursor.getColumnIndex(KEY_PIC)));
                    p.setOrecchio(cursor.getString(cursor.getColumnIndex(KEY_EAR)));
                }
                while (cursor.moveToNext());
            
        }
        return p;
    }

    public ArrayList<Picture> getPictures(ArrayList<Long> picturesIDs) {
        ArrayList<Picture> pictures = new ArrayList<>();

        String query = "SELECT  * FROM " + TABLE_PICTURES + " WHERE " + KEY_ID + " in(";

        for (int i=0; i < picturesIDs.size(); i++) {
            query += picturesIDs.get(i);
            if (i+1<picturesIDs.size())
                query += ",";
        }
        query += ");";

        String selectQuery = query;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Picture p = new Picture();
                p.setData(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                p.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                p.setImmagine(cursor.getBlob(cursor.getColumnIndex(KEY_PIC)));
                p.setOrecchio(cursor.getString(cursor.getColumnIndex(KEY_EAR)));
                pictures.add(p);
            }
            while (cursor.moveToNext());
        }
        return pictures;
    }

    public List<Picture> getSubjectPictures(long subject_id){
        List<Picture> pictures = new ArrayList<>();
        String selectQuery = "SELECT * FROM immagini, soggetti_immagini WHERE immagini.id=soggetti_immagini.immagine_id AND soggetti_immagini.soggetto_id=" + subject_id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    Picture p = new Picture();
                    p.setOrecchio(cursor.getString(cursor.getColumnIndex(KEY_EAR)));
                    p.setData(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                    p.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    p.setImmagine(cursor.getBlob(cursor.getColumnIndex(KEY_PIC)));
                    pictures.add(p);
                }
                while (cursor.moveToNext());
        }
        return pictures;
    }

    public List<Picture> getSessionPictures(long session_id){
        List<Picture> pictures = new ArrayList<>();
        String selectQuery = "SELECT * FROM immagini, soggetti_immagini WHERE immagini.id=soggetti_immagini.immagine_id AND soggetti_immagini.sessione_id=" + session_id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    Picture p = new Picture();
                    p.setOrecchio(cursor.getString(cursor.getColumnIndex(KEY_EAR)));
                    p.setData(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                    p.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                    p.setImmagine(cursor.getBlob(cursor.getColumnIndex(KEY_PIC)));
                    pictures.add(p);
                }
                while (cursor.moveToNext());
            
        }
        return pictures;
    }

    public Subject getSubjectByPictureId(long id) {
        Subject s;
        long subject_id = 0;
        String selectQuery = "SELECT " + KEY_SUBJECT_ID + " FROM " + TABLE_SUBJECTS_PICTURES + " WHERE " + KEY_PICTURE_ID + "=" + id;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            
                do {
                    subject_id = cursor.getLong(cursor.getColumnIndex(KEY_SUBJECT_ID));
                } while (cursor.moveToNext());
            
        }
        s = getSubject(subject_id);
        return s;
    }

    public Session getSessionByPictureId(long id) {
        Session s;
        long session_id = 0;
        String selectQuery = "SELECT " + KEY_SESSION_ID + " FROM " + TABLE_SUBJECTS_PICTURES + " WHERE " + KEY_PICTURE_ID + "=" + id;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
                do {
                    session_id = cursor.getLong(cursor.getColumnIndex(KEY_SESSION_ID));
                } while (cursor.moveToNext());
        }
        s = getSession(session_id);
        return s;
    }

    public int getSessionPicturesCount(long session_id){
        int count = 0;
        String selectQuery = "SELECT COUNT(*) AS count FROM immagini, soggetti_immagini WHERE immagini.id=soggetti_immagini.immagine_id AND soggetti_immagini.sessione_id=" + session_id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
                do
                    count = cursor.getInt(cursor.getColumnIndex("count"));
                while (cursor.moveToNext());
        }
        return count;
    }

    public void setSessionOpen(long session_id, boolean sessionOpen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        int open;
        if(sessionOpen)
            open = 1;
        else
            open = 0;
        args.put(KEY_OPEN, open);
        db.update(TABLE_SESSIONS, args, KEY_ID + "=" + session_id, null);
    }

    public void updatePictureSession(long picture_id, long newSession_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_SESSION_ID, newSession_id);
        db.update(TABLE_SUBJECTS_PICTURES, args, KEY_PICTURE_ID + "=" + picture_id, null);
    }

    public void updatePictureSubject(long picture_id, long newSubject_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_SUBJECT_ID, newSubject_id);
        db.update(TABLE_SUBJECTS_PICTURES, args, KEY_PICTURE_ID + "=" + picture_id, null);
    }

    public void updatePictureEar(long picture_id, String newEar){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_EAR, newEar);
        db.update(TABLE_PICTURES, args, KEY_ID + "=" + picture_id, null);
    }

    public boolean updateSubject(Subject editedSubject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, editedSubject.getNome());
        args.put(KEY_SURNAME, editedSubject.getCognome());
        args.put(KEY_BIRTHDATE, editedSubject.getDataDiNascita());
        if(db.update(TABLE_SUBJECTS, args, KEY_ID + "=" + editedSubject.getID(), null)>0)
            return true;
        else
            return false;
    }

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SOGGETTI);
        db.execSQL(CREATE_TABLE_IMMAGINI);
        db.execSQL(CREATE_TABLE_SESSIONI);
        db.execSQL(CREATE_TABLE_SOGGETTI_IMMAGINI);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICTURES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS_PICTURES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }

    final private static Integer VERSION = 1;



}
