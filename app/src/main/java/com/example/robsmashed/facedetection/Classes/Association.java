package com.example.robsmashed.facedetection.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Association implements Parcelable, Serializable {
    private static final long serialVersionUID = 0L;
    private long id;
    private long soggetto_id;
    private long immagine_id;
    private long sessione_id;

    private Association(Parcel in){
        id = in.readLong();
        soggetto_id = in.readLong();
        immagine_id = in.readLong();
        sessione_id = in.readLong();
    }

    public Association(){}

    public static final Creator<Association> CREATOR = new Creator<Association>() {
        @Override
        public Association createFromParcel(Parcel in) {
            return new Association(in);
        }

        @Override
        public Association[] newArray(int size) {
            return new Association[size];
        }
    };

    public void setSoggetto_id(long soggetto_id) {
        this.soggetto_id = soggetto_id;
    }

    public void setImmagine_id(long immagine_id){
        this.immagine_id = immagine_id;
    }

    public void setSessione_id(long sessione_id) {
        this.sessione_id = sessione_id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getImmagine_id() {
        return immagine_id;
    }

    public long getSessione_id() {
        return sessione_id;
    }

    public long getSoggetto_id() {
        return soggetto_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(soggetto_id);
        dest.writeLong(immagine_id);
        dest.writeLong(sessione_id);
    }
}
