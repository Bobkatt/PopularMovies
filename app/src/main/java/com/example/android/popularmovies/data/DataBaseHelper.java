package com.example.android.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.DataBaseContract.MovieEntry;
import com.example.android.popularmovies.data.DataBaseContract.ReviewEntry;
import com.example.android.popularmovies.data.DataBaseContract.TrailerEntry;

public class DataBaseHelper extends SQLiteOpenHelper
{
    //Local Variables
    private static final int DATABASE_VERSION = 4;
    static final String DATABASE_NAME = "movie.db";
    //Constructor
    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //Create database Tables
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RUN_TIME + " INTEGER NOT NULL DEFAULT 0, " +
                MovieEntry.COLUMN_FAVOURITE + " INTEGER NOT NULL DEFAULT 0, " +
                "UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " +
                ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID +
                ") REFERENCES " + MovieEntry.TABLE_NAME +
                " (" + MovieEntry.COLUMN_MOVIE_ID + "), " +
                "UNIQUE (" + ReviewEntry.COLUMN_MOVIE_ID +
                ", " + ReviewEntry.COLUMN_AUTHOR +
                ", " + ReviewEntry.COLUMN_CONTENT +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " +
                TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_DETAIL + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_URL + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID +
                ") REFERENCES " + MovieEntry.TABLE_NAME +
                " (" + MovieEntry.COLUMN_MOVIE_ID + "), " +
                "UNIQUE (" + TrailerEntry.COLUMN_TRAILER_URL +
                ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
    }
    //Upgrade - destroy and rebuild tables - data will re populate new tables as required
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
