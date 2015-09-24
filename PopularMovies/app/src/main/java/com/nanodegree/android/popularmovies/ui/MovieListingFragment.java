package com.nanodegree.android.popularmovies.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.Utils;
import com.nanodegree.android.popularmovies.data.MoviesContract;
import com.nanodegree.android.popularmovies.sync.uiadapters.MovieListingAdapter;

/**
 * Created by hojin on 15. 7. 30.
 * Loaders
 * : Easy way to asynchronously load data in an Activity or Fragment.
 *   Monitors data source and deliver results when content changes.
 *   Automatically reconnect after configuration change.
 */
public class MovieListingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private MovieListingAdapter movieListingAdapter;
    private GridView mGridView;
    public static int mPosition;

    //Add 15.09.08 10:40
    private CardView emptyView;

    //Use a single unique id for each kind of Loader.
    //Create a loader id constant for each and every kind of Loader accross your entire app.
    //Just create private constants in your Activity or Fragment for each kind of loader in it.
    private static final int MOVIE_LOADER=0;

    private static final String SELECTED_KEY="selected_position";

    private static final String POPULARITY_CASE="Popularity DESC";
    private static final String RATING_CASE="Vote_Average DESC";

    //Edit 15.9.9 16:36  public으로 지정.
    public static final int COL_ID=0;
    public static final int COL_BACKDROP_URL=1;
    public static final int COL_POSTER_URL=2;
    public static final int COL_TITLE=3;
    public static final int COL_OVERVIEW=4;
    public static final int COL_VOTE=5;
    public static final int COL_POPULARITY=6;
    public static final int COL_RELEASE_DATE=7;

    //Add 15.9.8 10:59 -싱글톤 패턴 적용.
    public static MovieListingFragment newInstance() {
        return new MovieListingFragment();
    }

    public interface Callback{
        void onItemSelected(Uri backdropPath,Uri uri, String title, int position);
    }

    //커서로더  2)onCreateLoader 호출
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder= Utils.getPreferredSortOrder(getActivity().getBaseContext());
        Uri movieLocationUri=MoviesContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),movieLocationUri,null,null,null,sortOrder);
    }

    //커서로더  3)onLoadFinished 호출
    //Called when a previously created Loader has finished its load.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Add 15.9.9 15:54
        if(data==null || data.getCount() ==0){
            //엠티뷰가 보여야 함.
            emptyView.setVisibility(View.VISIBLE);
        }else{
            //데이터가 있으므로 엠티뷰는 안 보여야 함.
            emptyView.setVisibility(View.GONE);
        }

        movieListingAdapter.swapCursor(data);
        if(mPosition != GridView.INVALID_POSITION){
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    //Called when a previously created Loader is being reset, thus making its data.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieListingAdapter.swapCursor(null);
    }





    /**
     *  http://xsun.info/?p=47
     *  http://4.bp.blogspot.com/-dp7lP0CNxEk/UsFKjQpPEUI/AAAAAAAABes/QJTnv85mKjU/s1600/Fragment_life-cycle.png
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        movieListingAdapter=new MovieListingAdapter(getActivity(),null,0);

        View view=inflater.inflate(R.layout.fragment_movie_listing, container, false);
        mGridView=(GridView)view.findViewById(R.id.gridview_movies);
        mGridView.setAdapter(movieListingAdapter);
        mGridView.setClickable(true);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    cursor.moveToPosition(position);

                    // 단계 이론 확인하자!!......
                    String movieID = cursor.getString(COL_ID);
                    Uri movieUri=MoviesContract.MovieEntry.buildMovieLocationWithId(movieID);
                    Uri backdropPath=Uri.parse(getString(R.string.base_moviebackdrop_url, cursor.getString(COL_BACKDROP_URL)));

                    String movieTitle=cursor.getString(COL_TITLE);
                    mPosition=position;

                    ((Callback)getActivity()).onItemSelected(backdropPath, movieUri,movieTitle,mPosition);

                }
            }
        });

        if(savedInstanceState !=null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition=savedInstanceState.getInt(SELECTED_KEY);
        }else{
            mPosition=GridView.INVALID_POSITION;
        }

        //Add 15.9.10 15:18 데이터가 없는 경우의 해당 뷰 구성.
        emptyView= (CardView)view.findViewById(R.id.no_items_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);    //커서로더  1)이니로더 호출
        super.onActivityCreated(savedInstanceState);
    }

    //Add 15.8.1. 20:44
    public void onSortOrderChanged(){
        String sortOrder=Utils.getPreferredSortOrder(getActivity().getBaseContext());
        switch(sortOrder){
            case POPULARITY_CASE:
                Toast.makeText(getActivity().getBaseContext(),"Popularity에 의해 지금 정렬중임.",Toast.LENGTH_SHORT).show();
                break;
            case RATING_CASE:
                Toast.makeText(getActivity().getBaseContext(),"User Rating에 의해 지금 정렬중임.",Toast.LENGTH_SHORT).show();
                break;
        }
        Cursor cursor=getActivity().getContentResolver().query(
          MoviesContract.MovieEntry.CONTENT_URI,
                null,null,null,Utils.getPreferredSortOrder(getActivity().getBaseContext()),null
        );
        movieListingAdapter.swapCursor(cursor);
    }

    //Add 15.8.4. 22:14
    public void moveToPosition(int position){
        mGridView.smoothScrollToPosition(position);
    }

    //Add 15.8.4 23:18
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != GridView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    //Add 15.9.9 16:05
    //페이버리트를 클릭했을 때, 해당 페이버리트가 클릭된 것만 보여지게 함.
    //-토글식으로 구성
    public void switchData(Boolean favorites){
        Cursor cursor;

        if(favorites){
            cursor=getActivity().getBaseContext().getContentResolver()
                    .query(MoviesContract.MovieEntry.CONTENT_URI,
                            null,
                            MoviesContract.MovieEntry.COLUMN_FAVORITE + " =?",
                            new String[]{"1"},
                            Utils.getPreferredSortOrder(getActivity().getBaseContext()));

            if(cursor==null || cursor.getCount()==0){
                //엠티뷰가 보여야 함.
                emptyView.setVisibility(View.VISIBLE);
            }else{
                emptyView.setVisibility(View.GONE);
            }

            movieListingAdapter.swapCursor(cursor);
        }else{
            cursor=getActivity().getBaseContext().getContentResolver()
                    .query(MoviesContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            Utils.getPreferredSortOrder(getActivity().getBaseContext()));
            if(cursor==null || cursor.getCount()==0){
                emptyView.setVisibility(View.VISIBLE);
            }else{
                emptyView.setVisibility(View.GONE);
            }

            movieListingAdapter.swapCursor(cursor);
        }
    }
}



























