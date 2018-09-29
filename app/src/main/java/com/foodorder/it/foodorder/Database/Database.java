package com.foodorder.it.foodorder.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class Database extends SQLiteAssetHelper {

     public static final String DB_NAME = "foodOrderDB";
     public static final int DB_V = 1;

     public Database(Context context) {
        super(context, DB_NAME, null, DB_V);

    }
}
