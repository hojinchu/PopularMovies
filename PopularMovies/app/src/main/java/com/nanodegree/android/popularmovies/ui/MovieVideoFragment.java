package com.nanodegree.android.popularmovies.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;
import com.nanodegree.android.popularmovies.sync.uiadapters.VideoRecycler;

import org.solovyev.android.views.llm.DividerItemDecoration;
import com.nanodegree.android.popularmovies.ui.MainActivity;

/**
 * Created by hojin on 15. 9. 9.
 * 해당 영화와 관련된 비디오를 보여주는 프레그먼트
 * Add 15.9.10 12:09
 */
public class MovieVideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static int VIDEO_LIST_LOADER=2;
    private ShareActionProvider mShareActionProvider;
    private RecyclerView mRecyclerView;
    private VideoRecycler videoRecycler;
    private TextView emptyView;
    private CardView cardView;
    private Uri mUri;
    private String firstVideoKeyString;     //복수의 비디오들의 기준점 역할.

    //객체 생성은 싱글톤 패턴 적용.
    public static MovieVideoFragment newInstance(){
        return new MovieVideoFragment();
    }

    //Add 15.9.10 12:17
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        videoRecycler=new VideoRecycler(getActivity().getBaseContext(),null);
        View rootView=inflater.inflate(R.layout.fragment_movie_reviews, container, false);

        setHasOptionsMenu(true);

        /**
         * https://github.com/serso/android-linear-layout-manager 참조함.
         * 라이브러리에서 제공하는 리니어레아아웃 레이아웃 매니저를 활용하여 리사이클러뷰를 설정함.
         * Add 15.9.11 12:28
         */
        final LinearLayoutManager layoutManager=
            new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.scrollView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mRecyclerView.setAdapter(videoRecycler);

        //태블릿인 경우
        if(MainActivity.mTwoPane){
            ((TextView)rootView.findViewById(R.id.header)).setText(getString(R.string.title_detail_videos));
        }
        mUri=MainActivity.mUri;

        emptyView=(TextView)rootView.findViewById(R.id.no_items);
        cardView=(CardView)rootView.findViewById(R.id.no_items_view);

        return rootView;
    }

    //Add 15.9.14 10:52
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(VIDEO_LIST_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Add 15.9.14 10:53
        if(mUri !=null){
            return new CursorLoader(getActivity(),
                    MoviesContract.VideoEntry.CONTENT_URI,
                    null,
                    MoviesContract.VideoEntry.COLUMN_MOVIE_ID + " =?",
                    new String[]{
                       MoviesContract.MovieEntry.getMovieIdFromURI(mUri)
                    },
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Add 15.9.14 10:57
        videoRecycler.swapCursor(data);
        if(data ==null || data.getCount() ==0){
            emptyView.setText(getResources().getString(R.string.no_videos));
            cardView.setVisibility(View.VISIBLE);
        }else{
            data.moveToFirst();
            firstVideoKeyString=data.getString(data.getColumnIndex(MoviesContract.VideoEntry.COLUMN_KEY));
            cardView.setVisibility(View.GONE);      //확인 요망.

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_videos, menu);
        MenuItem menuItem=menu.findItem(R.id.share);
        mShareActionProvider=(ShareActionProvider)MenuItemCompat.getActionProvider(menuItem);
    }

    //Add 15.9.14 14:35
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.share){
            Log.d("유명 영화들","공유 선택");
            String message;
            if(firstVideoKeyString !=null){
                message=getString(R.string.base_movievideo_url, firstVideoKeyString);
            }else{
                message=getString(R.string.default_share_message);
            }
            Intent intent=
                new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,message);
            intent.setType("text/plain");
            startActivity(intent);
            return true;
        }

        return false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}



























