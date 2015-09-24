package com.nanodegree.android.popularmovies.sync.uiadapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nanodegree.android.popularmovies.R;
import com.nanodegree.android.popularmovies.data.MoviesContract;

/**
 * Created by hojin on 15. 9. 9.
 * Add 15.9.10 11:38
 */
public class VideoRecycler extends CursorRecyclerAdapter<VideoRecycler.ViewHolder> {
    private Context context;

    public VideoRecycler(Context context, Cursor cursor) {
        super(cursor);
        this.context=context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        String title=cursor.getString(cursor.getColumnIndex(MoviesContract.VideoEntry.COLUMN_NAME));
        String url=cursor.getString(cursor.getColumnIndex(MoviesContract.VideoEntry.COLUMN_KEY));
        holder.title.setText(title);
        holder.url.setText(url);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView url;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Uri uri=Uri.parse(v.getContext().getString(R.string.base_youtube_url, url.getText().toString()));
                    Intent intent=new Intent(Intent.ACTION_VIEW, uri);

                    if(intent.resolveActivity(v.getContext().getPackageManager()) !=null){
                        v.getContext().startActivity(intent);
                    }
                }
            });
            //바인딩 되는 뷰(onBindViewHolder()에서 역할함)에 타이틀과 url 값을 넘기기 위해 값을 할당함.
            title=(TextView)itemView.findViewById(R.id.title);
            url=(TextView)itemView.findViewById(R.id.url);
        }
    }
}
























