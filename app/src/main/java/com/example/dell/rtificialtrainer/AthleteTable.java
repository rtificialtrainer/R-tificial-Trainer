package com.example.dell.rtificialtrainer;

import android.provider.BaseColumns;

/**
 * Definuje nazwy pól tabeli z zawodnikami w bazie danych oraz zapytania do jej tworzenia oraz usuwania
 */
public class AthleteTable implements BaseColumns {

    public static final String TABLE_NAME="athlete";
    public static final String NAME="name";
    public static final String SURNAME="surname";
    public static final String SEX="sex";
    public static final String WEIGHT="weight";
    public static final String HEIGHT="height";
    public static final String DATE_OF_BIRTH="date_of_birth";
    public static final String RECORD="record";
    public static final String EMAIL="email";

    public static final String CREATE_TABLE_ATHLETE =
            "CREATE TABLE "+TABLE_NAME+" ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +NAME+" TEXT NOT NULL, "
                    +SURNAME+" TEXT NOT NULL, "
                    +SEX+" TEXT NOT NULL, "
                    +WEIGHT+" REAL NOT NULL,"
                    +HEIGHT+" REAL NOT NULL,"
                    +EMAIL+" TEXT NOT NULL,"
                    +RECORD+" TEXT NOT NULL,"
                    +DATE_OF_BIRTH+" TEXT NOT NULL);";

    public static final String DROP_TABLE_ATHLETE =
            "DROP TABLE IF EXISTS "+TABLE_NAME+";";
}
