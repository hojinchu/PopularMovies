package com.nanodegree.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.nanodegree.android.popularmovies.data.MovieDbHelper;
import com.nanodegree.android.popularmovies.data.MoviesContract;

public class MoviesProvider extends ContentProvider {
    private MovieDbHelper movieDbHelper;
    private static final UriMatcher sUriMatcher=buildUriMatcher();
    public static final int MOVIES=100;
    public static final int MOVIE=101;

    //Add 15.09.02 14:47
    public static final int VIDEOS=102;
    public static final int VIDEOS_WITH_MOVIE=103;
    public static final int REVIEWS=104;
    public static final int REVIEWS_WITH_MOVIE=105;

    //Add 15.09.02 15:01 리뷰쿼리용도
    private static final SQLiteQueryBuilder sDetailReviewQuery;

    static{
        sDetailReviewQuery=new SQLiteQueryBuilder();
        sDetailReviewQuery.setTables(
                MoviesContract.MovieEntry.TABLE_NAME +" INNER JOIN "+       //무비테이블에 대한 리뷰 테이블 조인
                MoviesContract.ReviewEntry.TABLE_NAME +" ON "+

                MoviesContract.MovieEntry.TABLE_NAME + "." +
                MoviesContract.MovieEntry._ID + " = "+
                                                                            //조건
                MoviesContract.ReviewEntry.TABLE_NAME + "." +
                MoviesContract.ReviewEntry.COLUMN_MOVIE_ID

        );
    }

    public static UriMatcher buildUriMatcher(){
        final UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
        final String authority= MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,MoviesContract.PATH_MOVIES,MOVIES);
        matcher.addURI(authority,MoviesContract.PATH_MOVIES+"/*",MOVIE);

        //Add 15.09.02 16:55
        matcher.addURI(authority, MoviesContract.PATH_VIDEOS, VIDEOS);
        matcher.addURI(authority, MoviesContract.PATH_VIDEOS +"/*", VIDEOS_WITH_MOVIE);

        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS + "/*", REVIEWS_WITH_MOVIE);

        return matcher;
    }

    public MoviesProvider() {
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db=movieDbHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);

        //Add 15.09.02 17:05
        int returnCount = 0;

        switch(match) {
            case MOVIES:
                db.beginTransaction();
                //int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return (returnCount);

            //Add 15.09.02   17:07
            case VIDEOS:
                db.beginTransaction();
                try{
                    for(ContentValues value : values){
                        long _id=db.insert(MoviesContract.VideoEntry.TABLE_NAME,null,value);
                        if(_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                }catch(Exception ex){
                    ex.printStackTrace();
                }finally{
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;

            //Add 15.09.03 00:33
            case REVIEWS:
                db.beginTransaction();
                try{
                    for(ContentValues contentValues : values){
                        long _id=db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, contentValues);
                        if(_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();

                }catch(Exception ex){
                    ex.printStackTrace();
                }finally{
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        final SQLiteDatabase db=movieDbHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        int rowsDeleted=0;

        if(null==selection)
            selection="1";
        switch(match){
            case MOVIES:
                rowsDeleted=db.delete(MoviesContract.MovieEntry.TABLE_NAME,selection,selectionArgs);
                break;
            //Add 15.09.03 00:36
            case VIDEOS:
                rowsDeleted=db.delete(MoviesContract.VideoEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted=db.delete(MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
        if(rowsDeleted !=0)
            getContext().getContentResolver().notifyChange(uri,null);
        return(rowsDeleted);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        final int match=sUriMatcher.match(uri);
        switch(match){
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            //Add 15.09.03 00:41
            case VIDEOS:
                return MoviesContract.VideoEntry.CONTENT_TYPE;
            case VIDEOS_WITH_MOVIE:
                return MoviesContract.VideoEntry.CONTENT_TYPE;
            //Add 15.09.03 09:41
            case REVIEWS:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case REVIEWS_WITH_MOVIE:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        final SQLiteDatabase db=movieDbHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        Uri returnUri;
        //Edit 15.09.03 09:45
        long _id=0;

        switch(match){
            case MOVIES:
                _id=db.insert(MoviesContract.MovieEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri=MoviesContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into "+uri);
                break;
            //Add 15.09.03 09:48
            case VIDEOS:
                _id=db.insert(MoviesContract.VideoEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri=MoviesContract.VideoEntry.buildVideosUri(_id);
                else
                    throw new android.database.SQLException(" 에 행 삽입 실패함." +uri);
                break;
            case REVIEWS:
                _id=db.insert(MoviesContract.ReviewEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri=MoviesContract.ReviewEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException(" 에 행 삽입 실패함." +uri);
                break;          //Add 15.9.15 23:33
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        movieDbHelper=new MovieDbHelper(getContext());
        return (true);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Cursor retCursor;

        switch(sUriMatcher.match(uri)){
            case MOVIES:
                retCursor=movieDbHelper.getReadableDatabase().query(MoviesContract.MovieEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(),uri);
                return(retCursor);
            case MOVIE:
                retCursor=movieDbHelper.getReadableDatabase().query(MoviesContract.MovieEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                retCursor.setNotificationUri(getContext().getContentResolver(),uri);
                return(retCursor);
            //Add 15.09.03 10:25
            case VIDEOS:
                retCursor=movieDbHelper.getReadableDatabase().query(
                        MoviesContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                retCursor.setNotificationUri(getContext().getContentResolver(),uri);
                return retCursor;
            case REVIEWS:
                retCursor=movieDbHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                retCursor.setNotificationUri(getContext().getContentResolver(),uri);
                return retCursor;
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    //여기서부터.......오후에 구현....


    @Override
    @TargetApi(11)
    public void shutdown() {
        movieDbHelper.close();
        super.shutdown();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        final SQLiteDatabase db=movieDbHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        int rowsUpdated=0;

        switch(match){
            case MOVIES:
                rowsUpdated=db.update(MoviesContract.MovieEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            //Add 15.09.03 10:34
            case VIDEOS:
                rowsUpdated=db.update(MoviesContract.VideoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated=db.update(MoviesContract.ReviewEntry.TABLE_NAME,values,selection,selectionArgs);
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }

        if(rowsUpdated !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return(rowsUpdated);
    }
}

























