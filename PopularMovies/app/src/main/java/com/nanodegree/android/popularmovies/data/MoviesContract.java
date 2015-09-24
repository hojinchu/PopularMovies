package com.nanodegree.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hojin on 15. 7. 31.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY="com.nanodegree.android.popularmovies";
    public static final Uri BASE_CONTENT_URI=Uri.parse("content://"+CONTENT_AUTHORITY);

    public static final String PATH_MOVIES="movies";
    //Add 15.08.31 17:01
    public static final String PATH_REVIEWS="reviews";
    public static final String PATH_VIDEOS="videos";



    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI= BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static final String CONTENT_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE= ContentResolver.CURSOR_ITEM_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ PATH_MOVIES;

        //Edit 15.9.15 01:00
        public static final String TABLE_NAME="Movies";

        public static final String _ID="_id";
        public static final String COLUMN_BACKDROP_PATH="Backdrop_path";
        public static final String COLUMN_POSTER_PATH="Poster_Path";
        public static final String COLUMN_TITLE="Title";
        public static final String COLUMN_OVERVIEW="Overview";
        public static final String COLUMN_VOTE_AVERAGE="Vote_Average";
        public static final String COLUMN_POPULARITY="Popularity";
        public static final String COLUMN_RELEASE_DATE="Release_Date";
        //Add 15.08.31 17:06
        public static final String COLUMN_FAVORITE="is_favorite";

        public static Uri buildMovieUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildMovieLocationWithId(String movieId){
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        public static String getMovieIdFromURI(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    //Add 15.08.31 17:10
    public static final class ReviewEntry implements BaseColumns{
        public static final Uri CONTENT_URI=
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        //리뷰들 전체.
        public static final String CONTENT_TYPE=
                ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ PATH_REVIEWS;
        //전체 리뷰 중 낱개 리뷰 하나.
        public static final String CONTENT_ITEM_TYPE=
                ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY + "/" +PATH_REVIEWS;

        //리뷰 테이블과 컬럼들 선언
        public static final String TABLE_NAME="Reviews";
        public static final String _ID="_id";
        public static final String COLUMN_MOVIE_ID="movie_id";
        public static final String COLUMN_AUTHOR="author";
        public static final String COLUMN_CONTENT="content";
        public static final String COLUMN_URL="url";

        public static Uri buildReviewsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildMovieReviewsUri(String movieId){
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }

    //Add 15.09.01 01:08
    public static final class VideoEntry implements BaseColumns{
        public static final Uri CONTENT_URI=
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();

        // 비디오들 전체
        public static final String CONTENT_TYPE=
                ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ PATH_VIDEOS;
        // 전체 비디오 중 낱개 비디오 하나.
        public static final String CONTENT_ITEM_TYPE=
                ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY +"/"+ PATH_VIDEOS;

        //비디오 테이블과 컬럼들 선언
        public static final String TABLE_NAME="Videos";

        //Add 15.09.01 23:49 테이블에 사용할 필드 선언
        public static final String _ID="_id";
        public static final String COLUMN_MOVIE_ID="movie_id";
        public static final String COLUMN_ISO="iso";
        public static final String COLUMN_KEY="key";
        public static final String COLUMN_NAME="name";
        public static final String COLUMN_SITE="site";
        public static final String COLUMN_TYPE="type";
        //Add 15.09.03 10:52
        public static final String COLUMN_SIZE="size";

        //Add 15.09.01 23:53
        public static Uri buildVideosUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
        //Add 15.09.01 23:57
        public static Uri buildMovieVideosUri(String movieId){
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }
}












































