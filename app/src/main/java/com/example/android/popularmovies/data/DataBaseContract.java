package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataBaseContract
{
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";
    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Paths (appended to base content URI for possible URI's)
    public static final String PATH_MOVIE = "movies";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_TRAILERS = "trailers";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_MOVIE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        // Table name
        public static final String TABLE_NAME = PATH_MOVIE;
        //Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RUN_TIME = "run_time";
        public static final String COLUMN_FAVOURITE = "favourite";

        public static Uri buildMovieUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieURL(String movieId)
        {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri buildMoviesURL()
        {
            return CONTENT_URI.buildUpon().build();
        }

        public static String getMovieIdFromUri(Uri uri)
        {
            return uri.getPathSegments().get(1) ;
        }
    }

    /* Inner class that defines the table contents of the review table */
    public static final class ReviewEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
                appendPath(PATH_REVIEWS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        // Table name
        public static final String TABLE_NAME = PATH_REVIEWS;
        //Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";

        public static Uri buildReviewUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReviewURL(String movieId)
        {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri buildMoviesURL()
        {
            return CONTENT_URI.buildUpon().build();
        }

        public static String getMovieIdFromUri(Uri uri)
        {
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the table contents of the review table */
    public static final class TrailerEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAILERS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        // Table name
        public static final String TABLE_NAME = PATH_TRAILERS;
        //Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TRAILER_DETAIL = "detail";
        public static final String COLUMN_TRAILER_URL = "url";

        public static Uri buildTrailerUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailerURL(String movieId)
        {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri getTrailersURL()
        {
            return CONTENT_URI.buildUpon().build();
        }

        public static String getMovieIdFromUri(Uri uri)
        {
            return uri.getPathSegments().get(1);
        }
    }
}