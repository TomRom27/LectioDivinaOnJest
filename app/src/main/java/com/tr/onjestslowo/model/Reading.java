package com.tr.onjestslowo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by bpl2111 on 2014-05-29.
 */
public class Reading implements Comparable, Serializable, Parcelable {
    public Long Id;
    public String Content;
    public Date DateParsed;
    public String Title;

    public Reading()
    {
    }

    @Override
    public int compareTo(Object o) {
        return this.DateParsed.compareTo(((Reading)o).DateParsed);
    }

    // Parcelable
    public static final Parcelable.Creator<Reading> CREATOR = new Parcelable.Creator<Reading>() {

        @Override
        public Reading createFromParcel(Parcel source) {
            return new Reading(source);
        }

        @Override
        public Reading[] newArray(int size) {
            return new Reading[size];
        }
    };

    public Reading(Parcel in)
    {
        Reading r = (Reading)in.readSerializable();

        this.Id=r.Id;
        this.Content=r.Content;
        this.DateParsed=r.DateParsed;
        this.Title=r.Title;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

}
