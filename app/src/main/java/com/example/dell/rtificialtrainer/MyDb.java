package com.example.dell.rtificialtrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by DELL on 2016-02-09.
 */
public class MyDb {

    private SQLiteDatabase db;
    private MyDbHelper dbHelper;
    private Context context;

    public MyDb(Context context){

        this.context=context;
        dbHelper = new MyDbHelper(this.context);
    }

    /**
     * Dodaje nowego zawodnika do bazy danych
     * @param athlete obiekt AthleteModel reprezentujący zawodnika
     * @return wynik operacji na bazie danych
     */
    public long insertAthlete(AthleteModel athlete){

        long result = -1;

        try{
            if(athlete==null)throw new Exception("Przesłany obiekt zawodnika = null");

            db=dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(AthleteTable.NAME,athlete.getName());
            cv.put(AthleteTable.SURNAME, athlete.getSurname());
            cv.put(AthleteTable.SEX, athlete.getSex());
            cv.put(AthleteTable.WEIGHT, athlete.getWeight());
            cv.put(AthleteTable.HEIGHT, athlete.getHeight());
            cv.put(AthleteTable.EMAIL, athlete.getEmail());
            cv.put(AthleteTable.RECORD, athlete.getRecord());
            cv.put(AthleteTable.DATE_OF_BIRTH, athlete.getDateOfBirth());

            result = db.insert(AthleteTable.TABLE_NAME,null,cv);//-1 jak błąd albo numer wiersza ostatnio dodanego
            db.close();
        } catch (android.database.SQLException sqe){
            Log.wtf("insertAthlete SQLException","Błąd zapisu zawodnika: "+sqe.toString());
        }catch (Exception e){
            Log.wtf("insertAthlete Exception",""+e.getMessage());
        }finally {
            return result;
        }
    }

    /**
     * Zwraca zawodników znajdujących się w bazie danych
     * @return Lista zwodników w bazie danych
     */
    public ArrayList<AthleteModel> getAthletes(){
        ArrayList<AthleteModel> athletes;
        athletes = new ArrayList<>();
        try {
            db = dbHelper.getReadableDatabase();
            Cursor c = db.query(AthleteTable.TABLE_NAME,null,null,null,null,null,AthleteTable.NAME+","+AthleteTable.SURNAME+" ASC");
            if(c!=null && c.moveToFirst()){
                do{
                    int id = c.getInt(c.getColumnIndex(AthleteTable._ID));
                    String name =c.getString(c.getColumnIndex(AthleteTable.NAME));
                    String surname =c.getString(c.getColumnIndex(AthleteTable.SURNAME));
                    String sex =c.getString(c.getColumnIndex(AthleteTable.SEX));
                    Float weight = c.getFloat(c.getColumnIndex(AthleteTable.WEIGHT));
                    Float height = c.getFloat(c.getColumnIndex(AthleteTable.HEIGHT));
                    String email = c.getString(c.getColumnIndex(AthleteTable.EMAIL));
                    String record = c.getString(c.getColumnIndex(AthleteTable.RECORD));
                    String dateOfBirth = c.getString(c.getColumnIndex(AthleteTable.DATE_OF_BIRTH));
                    AthleteModel athlete = new AthleteModel(id,name,surname,sex,weight,height,email,record,dateOfBirth);
                    athletes.add(athlete);
                }while (c.moveToNext());
            }
            db.close();
        }catch (android.database.SQLException e){
            Log.wtf("getAthletes SQLException","Błąd odczytu zawodników");
            athletes=null;
        }catch(Exception ee){
            Log.wtf("getAthletes Exception","Błąd odczytu zawodników");
            athletes=null;
        }finally {
            return athletes;
        }
    }

    /**
     * Usuwa zawodnika z bazy danych
     * @param id identyfikator zawodnika w bazie danych
     * @return rezultat usunięcie w bazie danych
     */
    public int deleteAthlete(int id){

        int result = 0;

        try{
            if(id<=0)throw new Exception("Błędne id zawodnika do usunięcia (<=0)");
            db=dbHelper.getWritableDatabase();

            String where = AthleteTable._ID+"= ?";
            String[] args = new String[]{String.valueOf(id)};
            result = db.delete(AthleteTable.TABLE_NAME,where,args);//zwraca ilość zmodyfikowanych wierszy lub 0 gdy nie zmodyfikowano
        } catch (android.database.SQLException sqe){
            Log.wtf("deleteAthlete SQLException","Błąd usuwania zawodnika");
            result = 0;
        }catch(Exception e){
            Log.wtf("deleteAthlete Exception","Błąd usuwania zawodnika");
        } finally{
            return result;
        }
    }

    /**
     * Usuwa kilku zawodników z bazy danych
     * @param athletes_id tablica identyfikatorów zawodników do usunięcia
     * @return ilość usuniętych rekordów
     */
    public int deleteMultiAthletes(String[] athletes_id){

        int result = 0;

        try{
            if(athletes_id.length==0||athletes_id==null)throw new Exception("Brak id zawodników do usunięcia");
            db=dbHelper.getWritableDatabase();
            String where = AthleteTable._ID+" IN ("+ new String(new char[athletes_id.length-1]).replace("\0","?,")+"?)";
            String[] args = athletes_id;
            result = db.delete(AthleteTable.TABLE_NAME,where,args);//zwraca ilość zmodyfikowanych wierszy lub 0 gdy nie zmodyfikowano
        } catch (android.database.SQLException sqe){
            Log.wtf("deleteMultiAthletes SQLException","Błąd usuwania zawodników");
            result = 0;
        }catch (Exception e){
            Log.wtf("deleteMultiAthletes Exception",""+e.getMessage());
        }finally{
            return result;
        }
    }

    /**
     * Aktualizuje dane o zawodniku w bazie danych
     * @param athlete obiekt AthleteModel reprezentujący zawodnika
     * @return rezultat operacji aktualizacji na bazie danych
     */
    public int updateAthlete(AthleteModel athlete){

        int result = 0;

        try{
            if(athlete==null)throw new Exception("Przesłany obiekt zawodnika = null");
            db=dbHelper.getWritableDatabase();

            String where = AthleteTable._ID+"= ?";
            String[] args = new String[]{String.valueOf(athlete.getId())};

            ContentValues cv = new ContentValues();
            cv.put(AthleteTable.NAME,athlete.getName());
            cv.put(AthleteTable.SURNAME,athlete.getSurname());
            cv.put(AthleteTable.SEX, athlete.getSex());
            cv.put(AthleteTable.WEIGHT, athlete.getWeight());
            cv.put(AthleteTable.HEIGHT, athlete.getHeight());
            cv.put(AthleteTable.EMAIL, athlete.getEmail());
            cv.put(AthleteTable.RECORD, athlete.getRecord());
            cv.put(AthleteTable.DATE_OF_BIRTH, athlete.getDateOfBirth());

            result = db.update(AthleteTable.TABLE_NAME,cv,where,args);
        } catch (android.database.SQLException sqe){
            Log.wtf("updateAthlete SQLException","Błąd edytowania zawodnika");
            result = 0;
        }catch (Exception e){
            Log.wtf("updateAthlete Exception",""+e.getMessage());
        }finally {
            return result;//ilość zaktualizowanych wierszy tabeli
        }
    }
}
