package com.example.robsmashed.facedetection.Classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Session implements Parcelable{
    private long id;
    private int number;
    private boolean open;

    private Session(Parcel in){
        id = in.readLong();
        number = in.readInt();
        open = in.readByte() != 0;
    }

    public Session(){}

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    public int getNumber(){
        return number;
    }

    public boolean isOpen(){
        return open;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public void setNumber(int number){
        this.number = number;
    }

    public void setOpen(boolean open){
        this.open = open;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(number);
        dest.writeByte((byte)(open ? 1 : 0));
    }
}
