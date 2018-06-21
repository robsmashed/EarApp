package com.example.robsmashed.facedetection.Classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Subject implements Parcelable, Comparable{
    private long id;
    private String nome, cognome, dataDiNascita;

    private Subject(Parcel in){
        id = in.readLong();
        nome = in.readString();
        cognome = in.readString();
        dataDiNascita = in.readString();
    }

    public Subject() {}

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    public long getID() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome(){
        return cognome;
    }

    public String getDataDiNascita(){
        return dataDiNascita;
    }

    public void setID(long id){
        this.id = id;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setCognome(String cognome){
        this.cognome = cognome;
    }

    public void setDataDiNascita(String dataDiNascita){
        this.dataDiNascita = dataDiNascita;
    }

    public void update(Subject s){
        nome = s.getNome();
        cognome = s.getCognome();
        dataDiNascita = s.getDataDiNascita();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nome);
        dest.writeString(cognome);
        dest.writeString(dataDiNascita);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Subject other = (Subject) o;
        return nome.compareTo(other.getNome());
    }
}
