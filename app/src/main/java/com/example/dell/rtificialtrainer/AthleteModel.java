package com.example.dell.rtificialtrainer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by DELL on 2016-02-09.
 */
public class AthleteModel implements Parcelable{

    private int id;
    private String name;
    private String surname;
    private String sex;
    private float weight;
    private float height;
    private String email;
    private String dateOfBirth;
    private String record;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public static final String ATHL_PARCEL = "athlete_parcel";


    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * Oblicza wiek zawodnika na podstawie daty urodzin
     * @return wiek
     */
    public int getAge() {
        Calendar today = Calendar.getInstance();
        String[] dateParts = dateOfBirth.split("-");
        int bDay = Integer.parseInt(dateParts[0]);
        int bMonth = Integer.parseInt(dateParts[1]);
        int bYear = Integer.parseInt(dateParts[2]);
        int age = today.get(Calendar.YEAR) - bYear;
        if (bMonth > today.get(Calendar.MONTH)+1 ||
                (bMonth == today.get(Calendar.MONTH)+1 && bDay > today.get(Calendar.DATE))) {
            age--;
        }
        return age;
    }

    /**
     * Oblicza BMI na podstawie wagi i wzrostu
     * @return bmi
     */
    public float getBmi() {
        float bmi = (weight / (height * height));
        return bmi;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public AthleteModel(String name, String surname, String sex, float weight, float height, String email, String record, String dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.sex=sex;
        this.weight=weight;
        this.height=height;
        this.email=email;
        this.record=record;
        this.dateOfBirth=dateOfBirth;
    }

    public AthleteModel(int id, String name, String surname, String sex, float weight, float height, String email, String record, String dateOfBirth) {
        this.id=id;
        this.name = name;
        this.surname = surname;
        this.sex=sex;
        this.weight=weight;
        this.height=height;
        this.email=email;
        this.record=record;
        this.dateOfBirth=dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(sex);
        dest.writeFloat(weight);
        dest.writeFloat(height);
        dest.writeString(email);
        dest.writeString(record);
        dest.writeString(dateOfBirth);
    }

    public static final Parcelable.Creator<AthleteModel> CREATOR = new Parcelable.Creator<AthleteModel>(){

        @Override
        public AthleteModel createFromParcel(Parcel source) {
            return new AthleteModel(source);
        }

        @Override
        public AthleteModel[] newArray(int size) {
            return new AthleteModel[size];
        }
    };

    public AthleteModel(Parcel source){
        id=source.readInt();
        name = source.readString();
        surname = source.readString();
        sex = source.readString();
        weight=source.readFloat();
        height=source.readFloat();
        email=source.readString();
        record=source.readString();
        dateOfBirth=source.readString();
    }

    public boolean equals(AthleteModel o) {
        return (surname.equals(o.getSurname())
                &&name.equals(o.getName())
                &&email.equals(o.getEmail())
                &&record.equals(o.getRecord())
                &&dateOfBirth.equals(o.getDateOfBirth())
                &&(weight==o.getWeight())
                &&(height==o.getHeight())
                &&(sex.equals(o.getSex())));
    }

    @Override
    public String toString() {
        return "AthleteModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
