package com.example.robsmashed.facedetection.Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Picture implements Parcelable{
    private long id;
    private byte[] immagine;
    private String orecchio;
    private String data;
    private String config;
    private int width, height;
    private long byteCount;

    public Picture(){}

    private Picture(Parcel in){
        id = in.readLong();
        orecchio = in.readString();
        data = in.readString();
        config = in.readString();
        width = in.readInt();
        height = in.readInt();
        byteCount = in.readLong();
    }


    public static final Creator<Picture> CREATOR = new Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    public void setId(long id){
        this.id = id;
    }

    public void setImmagine(byte[] immagine){
        this.immagine = immagine;
        Bitmap bm = BitmapFactory.decodeByteArray(this.immagine, 0, this.immagine.length);
        config = bm.getConfig().toString();
        width = bm.getWidth();
        height = bm.getHeight();
        byteCount = bm.getByteCount();
    }

    public Association findAssociation(ArrayList<Association> associations){
        Association a = null;
        Boolean found = false;
        for(int i=0; i<associations.size() && !found; i++){
            if(associations.get(i).getImmagine_id()==id){
                a = associations.get(i);
                found = true;
            }
        }
        return a;
    }

    public String getConfig() {
        return config;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getByteCount() {
        return byteCount;
    }

    public void setOrecchio(String orecchio){
        this.orecchio = orecchio;
    }

    public void setData(String data){
        this.data = data;
    }

    public byte[] getImmagine(){
        return immagine;
    }

    public long getId(){
        return id;
    }

    public String getOrecchio(){
        return orecchio;
    }

    public String getData(){
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(orecchio);
        dest.writeString(data);
        dest.writeString(config);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeLong(byteCount);
    }

    public void update(Picture picture){
        this.orecchio = picture.getOrecchio();
    }
}
