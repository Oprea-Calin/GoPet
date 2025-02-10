package com.example.gopet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class dbConnect extends SQLiteOpenHelper {

    private static String dbName = "GoPet";
    private static String dbTable = "users";
    private static int dbVersion = 1;

    private static String ID = "id";
    private static String USERNAME = "username";
    private static String PASSWORD = "password";

    public dbConnect(@Nullable Context context) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String query = "create table " + dbTable + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ USERNAME + " TEXT, " + PASSWORD + " TEXT)" ;
        sqLiteDatabase.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + dbTable);
        onCreate(sqLiteDatabase);
    }
    public void addUser(Users user)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, user.getUsername());
        values.put(PASSWORD, user.getPassword());

        db.insert(dbTable, null, values);
    }

    public List<Users> getAllUsers()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Users> usersList = new ArrayList<>();

        String query = "SELECT * FROM " + dbTable;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst())
        {
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                String username_ = cursor.getString(cursor.getColumnIndexOrThrow(this.USERNAME));
                String password_ = cursor.getString(cursor.getColumnIndexOrThrow(this.PASSWORD));

                Users user = new Users(id, username_, password_);
                usersList.add(user);


            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return  usersList;

    }

    public boolean checkIfUserExists(String username_){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + dbTable + " HWERE " + username_ + " = ?" ;
        Cursor cursor = db.rawQuery(query, new String[]{username_});

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        db.close();
        return exists;


    }



}
