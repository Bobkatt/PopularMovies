package com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmovies.data.DataBaseContract.MovieEntry;
import com.example.android.popularmovies.data.DataBaseContract.ReviewEntry;
import com.example.android.popularmovies.data.DataBaseContract.TrailerEntry;

public class DataBaseProvider extends ContentProvider
{
    private final String LOG_TAG = DataBaseProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private DataBaseHelper mOpenHelper;

    private static final int MOVIE = 100;
    private static final int MOVIE_BY_ID = 101;
    private static final int MOVIE_WITH_DETAIL = 102;
    private static final int TRAILER = 200;
    private static final int TRAILER_BY_MOVIE_ID = 201;
    private static final int REVIEW = 300;
    private static final int REVIEW_BY_MOVIE_ID = 301;


    private static final SQLiteQueryBuilder sMovieWithDetailQueryBuilder;
    private static final SQLiteQueryBuilder sMovieTrailerQueryBuilder;
    private static final SQLiteQueryBuilder sMovieReviewQueryBuilder;

    static
    {
        sMovieWithDetailQueryBuilder = new SQLiteQueryBuilder();
        sMovieTrailerQueryBuilder = new SQLiteQueryBuilder();
        sMovieReviewQueryBuilder = new SQLiteQueryBuilder();

        sMovieWithDetailQueryBuilder.setTables(MovieEntry.TABLE_NAME);

        sMovieTrailerQueryBuilder.setTables(MovieEntry.TABLE_NAME +
                " INNER JOIN " + TrailerEntry.TABLE_NAME +
                " ON " + TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID +
                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID);

        sMovieReviewQueryBuilder.setTables(MovieEntry.TABLE_NAME +
                " INNER JOIN " + ReviewEntry.TABLE_NAME +
                " ON " + ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID +
                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID);
    }

    private static final String sMovieDetailSelection =
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieTrailerSelection =
            TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sMovieReviewSelection =
            ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    private static UriMatcher buildUriMatcher()
    {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataBaseContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, DataBaseContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, DataBaseContract.PATH_MOVIE + "/#",MOVIE_BY_ID );
        matcher.addURI(authority, DataBaseContract.PATH_MOVIE + "/#" + "/*", MOVIE_WITH_DETAIL);
        matcher.addURI(authority, DataBaseContract.PATH_TRAILERS, TRAILER);
        matcher.addURI(authority, DataBaseContract.PATH_TRAILERS + "/" +
                DataBaseContract.PATH_MOVIE + "/#", TRAILER_BY_MOVIE_ID);
        matcher.addURI(authority, DataBaseContract.PATH_REVIEWS, REVIEW);
        matcher.addURI(authority, DataBaseContract.PATH_REVIEWS + "/" +
                DataBaseContract.PATH_MOVIE + "/#", REVIEW_BY_MOVIE_ID);
        return matcher;
    }

    public boolean onCreate()
    {
        mOpenHelper = new DataBaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_BY_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILER:
                return TrailerEntry.CONTENT_TYPE;
            case TRAILER_BY_MOVIE_ID:
                return TrailerEntry.CONTENT_ITEM_TYPE;
            case REVIEW:
                return ReviewEntry.CONTENT_TYPE;
            case REVIEW_BY_MOVIE_ID:
                return ReviewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        switch (sUriMatcher.match(uri))
        {
            case MOVIE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query
                        (MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                                null, null, sortOrder);
                break;
            }
            case MOVIE_BY_ID:
            {
                String movieId = MovieEntry.getMovieIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query
                        (MovieEntry.TABLE_NAME, projection, sMovieDetailSelection,
                                new String[]{movieId}, null, null, sortOrder);
                break;
            }
            case TRAILER:
            {
                retCursor = mOpenHelper.getReadableDatabase().query
                        (TrailerEntry.TABLE_NAME, projection, selection, selectionArgs,
                                null, null, sortOrder);
                break;
            }
            case TRAILER_BY_MOVIE_ID:
            {
                String movieId = MovieEntry.getMovieIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query
                        (TrailerEntry.TABLE_NAME, projection, sMovieTrailerSelection,
                                new String[]{movieId}, null, null, sortOrder);
                break;
            }
            case REVIEW: {
                retCursor = mOpenHelper.getReadableDatabase().query
                        (ReviewEntry.TABLE_NAME, projection, selection, selectionArgs,
                                null, null, sortOrder);
                break;
            }
            case REVIEW_BY_MOVIE_ID:
            {
                String movieId = MovieEntry.getMovieIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query
                        (TrailerEntry.TABLE_NAME, projection, sMovieReviewSelection,
                                new String[]{movieId}, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match)
        {
            case MOVIE:
            {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if(_id > 0)
                {
                    returnUri = MovieEntry.buildMovieUri(_id);
                }
                else
                {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case TRAILER:
            {
                long _id = db.insert(TrailerEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = TrailerEntry.buildTrailerUri(_id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case REVIEW:
            {
                long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ReviewEntry.buildReviewUri(_id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match)
        {
            case MOVIE:
            {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TRAILER:
            {
                rowsDeleted = db.delete(TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REVIEW:
            {
                rowsDeleted = db.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (selection == null || rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match)
        {
            case MOVIE:
            {
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TRAILER:
            {
                rowsUpdated = db.update(TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case REVIEW:
            {
                rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown()
    {
        mOpenHelper.close();
        super.shutdown();
    }
}
