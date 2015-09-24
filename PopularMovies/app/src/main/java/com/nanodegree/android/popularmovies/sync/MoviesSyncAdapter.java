package com.nanodegree.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**Service
 * Created by hojin on 15. 7. 30.
 * http://kpbird.com/2015/04/Android-Simple-SyncAdapter-Example/
 * http://kpbird.com/assets/images/posts/SyncAdapter_large.png 참조
 *
 * 이 클래스가 핵심임.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter{
    private Context mContext;

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext=context;
    }

    //Add 15.8.5 01:29
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("Popular Movies", "onPerformSync 호출됨.");
        HttpURLConnection urlConnection=null;
        BufferedReader reader=null;

        try{
            Uri movieDbUri=Uri.parse(mContext.getString(R.string.base_moviedb_url)).buildUpon()
                    .appendQueryParameter(mContext.getString(R.string.url_sortBy_key),mContext.getString(R.string.url_sortBy_value))
                    .appendQueryParameter(mContext.getString(R.string.url_api_key_key),mContext.getString(R.string.url_api_key_value))
                    .build();

            // 호출을 위한 url 생성
            URL callLogLocation=new URL(movieDbUri.toString());
            // http 커넥션 열기
            urlConnection=(HttpURLConnection)callLogLocation.openConnection();

            ////////////////////
            // 요청으로부터 입력 스트림을 획득하기
            InputStream in=urlConnection.getInputStream();
            // 결과 문자열을 생성하기 위한 정보를 득하기 위해 readstream() 호출
            String result=readStream(in);

            getMovieDataFromJSON(result);

        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(urlConnection !=null){
                urlConnection.disconnect();
            }
            if(reader !=null){
                try{
                    reader.close();
                }catch(final IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public String readStream(InputStream in){
        BufferedReader reader=new BufferedReader(new InputStreamReader(in));
        StringBuilder response=new StringBuilder();
        String buffer;
        try{
            while((buffer=reader.readLine())!=null){
                response.append(buffer);
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return(response.toString());
    }

    public void getMovieDataFromJSON(String moviesJsonStr){
        final String MOVIE_ID="id";
        final String MOVIE_TITLE="original_title";
        final String RELEASE_DATE="release_date";
        final String OVERVIEW="overview";
        final String VOTE="vote_average";
        final String POPULARITY="popularity";
        final String BACKDROP="backdrop_path";
        final String POSTER="poster_path";

        try{
            JSONObject moviesJSON=new JSONObject(moviesJsonStr);
            JSONArray moviesArray=moviesJSON.getJSONArray("results");

            Vector<ContentValues> cVVector= new Vector<ContentValues>(moviesArray.length());

            for(int i=0; i<moviesArray.length();i++){
                int id=moviesArray.getJSONObject(i).getInt(MOVIE_ID);
                String title=moviesArray.getJSONObject(i).getString(MOVIE_TITLE);
                String release_date=moviesArray.getJSONObject(i).getString(RELEASE_DATE);
                String overview=moviesArray.getJSONObject(i).getString(OVERVIEW);
                double vote=moviesArray.getJSONObject(i).getDouble(VOTE);
                double popularity=moviesArray.getJSONObject(i).getDouble(POPULARITY);
                String backdrop=moviesArray.getJSONObject(i).getString(BACKDROP);
                String poster=moviesArray.getJSONObject(i).getString(POSTER);

                ContentValues movieValues=new ContentValues();
                movieValues.put(MoviesContract.MovieEntry._ID,id);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, release_date);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, backdrop);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, poster);
                //Add 15.9.14 23:02
                movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE,0);

                cVVector.add(movieValues);
            }

            int inserted=0;

            if(cVVector.size()>0){
                //기존 오래된 데이터 삭제
                //Edit 15.9.14 23:05
                mContext.getContentResolver().delete(
                        MoviesContract.MovieEntry.CONTENT_URI,
                        MoviesContract.MovieEntry.COLUMN_FAVORITE + " =?",
                        new String[]{
                                "0"
                        });
                //Add 15.9.14 23:06
                mContext.getContentResolver().delete(
                        MoviesContract.VideoEntry.CONTENT_URI,null,null
                );
                mContext.getContentResolver().delete(
                        MoviesContract.ReviewEntry.CONTENT_URI,null,null
                );

                //새로운 데이터를 추가함.
                ContentValues[] cvArray=new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted=mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI,cvArray);
            }

            Log.d("Popular Movies", "Sync Finished - " +Integer.toString(inserted)+ " records inserted!");

            //Add 15.9.14 23:10
            Cursor cursor= mContext.getContentResolver().query(
                    MoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            while(cursor.moveToNext()){
                getVideos(cursor.getInt(cursor.getColumnIndex(MoviesContract.MovieEntry._ID)));
                getReviews(cursor.getInt(cursor.getColumnIndex(MoviesContract.MovieEntry._ID)));
            }

            cursor.close();


        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    //Add 15.9.14 23:13
    public void getVideos(int movieId){
        HttpURLConnection httpURLConnection=null;
        BufferedReader bufferedReader=null;

        try{
            Uri movieDbUri=Uri.parse(mContext.getString(R.string.base_movievideo_url, movieId)).buildUpon()
                    .appendQueryParameter(mContext.getString(R.string.url_api_key_key),mContext.getString(R.string.url_api_key_value))
                    .build();

            //호출에 사용하기 위한 url 초기화
            URL callLogLocation=new URL(movieDbUri.toString());
            //http connection 열기
            httpURLConnection=(HttpURLConnection)callLogLocation.openConnection();
            //요청으로부터의 입력 스트림을 획득.
            InputStream inputStream=httpURLConnection.getInputStream();
            //결과 문자열을 초기화하기 위해 정보를 얻기 위한 readstream()호출
            String result=readStream(inputStream);

            getVideoDataFromJSON(result, movieId);



        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(httpURLConnection !=null){
                httpURLConnection.disconnect();
            }
            if(bufferedReader !=null){
                try{
                    bufferedReader.close();
                }catch(final IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //Add 15.9.14 23:26
    public void getVideoDataFromJSON(String jsonString, int movieId){
        final String ID="id";
        final String ISO="iso_639_1";
        final String KEY="key";
        final String NAME="name";
        final String SITE="site";
        final String SIZE="size";
        final String TYPE="type";

        try{
            JSONObject jsonObject=new JSONObject(jsonString);
            JSONArray jsonArray=jsonObject.getJSONArray("results");     //"results" ??

            Vector<ContentValues> contentValuesVector=new Vector<ContentValues>(jsonArray.length());
            for(int i=0; i<jsonArray.length();i++){
                String id=jsonArray.getJSONObject(i).getString(ID);
                String iso=jsonArray.getJSONObject(i).getString(ISO);
                String key=jsonArray.getJSONObject(i).getString(KEY);
                String name=jsonArray.getJSONObject(i).getString(NAME);
                String site=jsonArray.getJSONObject(i).getString(SITE);
                int size=jsonArray.getJSONObject(i).getInt(SIZE);
                String type=jsonArray.getJSONObject(i).getString(TYPE);

                ContentValues contentValues=new ContentValues();
                contentValues.put(MoviesContract.VideoEntry._ID,id);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID,movieId);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_ISO,iso);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_KEY,key);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_NAME,name);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_SITE,site);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_SIZE,size);
                contentValues.put(MoviesContract.VideoEntry.COLUMN_TYPE,type);

                contentValuesVector.add(contentValues);

            }

            int inserted=0;
            if(contentValuesVector.size()>0){
                //새로운 데이터 추가함.
                ContentValues[] contentValuesArray=new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);
                inserted=mContext.getContentResolver().bulkInsert(MoviesContract.VideoEntry.CONTENT_URI, contentValuesArray);
            }


        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    //Add 15.9.14 23:53
    public void getReviews(int movieId){
        HttpURLConnection httpURLConnection=null;
        BufferedReader bufferedReader=null;

        try{
            Uri movieDBUri=Uri.parse(mContext.getString(R.string.base_moviereview_url,movieId)).buildUpon()
                    .appendQueryParameter(mContext.getString(R.string.url_api_key_key), mContext.getString(R.string.url_api_key_value))
                    .build();
            //호출을 위한 url 초기화
            URL callLogLocation=new URL(movieDBUri.toString());
            //http 연결 열기
            httpURLConnection=(HttpURLConnection)callLogLocation.openConnection();
            //요청으로 부터의 입력 스트림 획득하기
            InputStream inputStream=httpURLConnection.getInputStream();
            //결과 스트링 초기화인 정보를 획득하기 위한 readstream() 획득.
            String result=readStream(inputStream);

            getReviewDataFromJSON(result, movieId);




        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(httpURLConnection !=null){
                httpURLConnection.disconnect();
            }
            if(bufferedReader !=null){
                try{
                    bufferedReader.close();
                }catch (final IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //Add 15.9.15 00:06
    public void getReviewDataFromJSON(String jsonString ,int movieId){
        final String ID="id";
        final String AUTHOR="author";
        final String CONTENT="content";
        final String URL="url";

        try{
            JSONObject jsonObject=new JSONObject(jsonString);
            JSONArray jsonArray=jsonObject.getJSONArray("results");             //"results" ??

            Vector<ContentValues> contentValuesVector=new Vector<ContentValues>(jsonArray.length());

            for(int i=0; i<jsonArray.length();i++){
                String id=jsonArray.getJSONObject(i).getString(ID);
                String author=jsonArray.getJSONObject(i).getString(AUTHOR);
                String content=jsonArray.getJSONObject(i).getString(CONTENT);
                String url=jsonArray.getJSONObject(i).getString(URL);

                ContentValues contentValues=new ContentValues();
                contentValues.put(MoviesContract.ReviewEntry._ID, id);
                contentValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
                contentValues.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
                contentValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, content);
                contentValues.put(MoviesContract.ReviewEntry.COLUMN_URL, url);

                contentValuesVector.add(contentValues);
            }

            int inserted=0;
            if(contentValuesVector.size()>0){
                // 세 데이터 추가하기
                ContentValues[] contentValuesArray=new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);
                inserted=mContext.getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, contentValuesArray);
            }

        }catch(JSONException e){
            e.printStackTrace();
        }
    }


    //Add 25.8.1
    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }

    public static Account getSyncAccount(Context context){
        AccountManager accountManager=
                (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount=new Account(context.getString(R.string.app_name),context.getString(R.string.sync_account_type));

        //여기 부터 미구현된 부분이 있음.
        //계정을 생성해야 함.
        if(accountManager.getPassword(newAccount)==null){
            if(!accountManager.addAccountExplicitly(newAccount,"",null)){
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return(newAccount);
    }

    public static final int SYNC_INTERVAL=60*180;
    public static final int SYNC_FLEXTIME=SYNC_INTERVAL/3;

    // 계정 생성 및 인증 완료 후 동작임.
    public static void onAccountCreated(Account newAccount, Context context){
        MoviesSyncAdapter.configurePeriodicSync(context,SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority),true);
        //syncImmediately(context);         //Edit 주석 처림함.15.09.14 17:33
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime){
        Account account=getSyncAccount(context);
        String authority=context.getString(R.string.content_authority);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            SyncRequest request=
                    new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        }else{
            ContentResolver.addPeriodicSync(account,authority,new Bundle(),syncInterval);
        }
    }

    public static void syncImmediately(Context context){
        Bundle bundle=new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority),bundle);
    }

}



































