package com.nanodegree.android.popularmovies.sync.uiadapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

/**
 * Created by hojin on 15. 9. 9.
 * 비디오와 리뷰들을 저장하기 위한 프래그먼트들이 상속받을 추상클래스
 * 이 추상클래스는 외부에서 관련 데이터(비디오,리뷰들) 받아올 자식 클래스들에 부모 클래스임.
 */
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected boolean mDataValid;
    protected Cursor mCursor;
    protected int mRowIDColumn;

    public CursorRecyclerAdapter(Cursor cursor){
        init(cursor);
    }

    void init(Cursor lCursor){
        boolean cursorPresent = lCursor != null;
        mCursor=lCursor;
        mDataValid=cursorPresent;
        mRowIDColumn = cursorPresent ? lCursor.getColumnIndexOrThrow("_id") : -1;
        setHasStableIds(true);
    }

    public abstract void onBindViewHolder(VH holder, Cursor cursor);

    //콜백 메서드임.
    @Override
    public void onBindViewHolder(VH holder, int position) {
        if(!mDataValid){
            throw new IllegalStateException("이 메서드는 오직 커서가 유효할때만 호출되는 것입니다!");
        }
        if(!mCursor.moveToPosition(position)){
            throw new IllegalStateException("커서가 해당 위치로 움직일수 없습니다. " +position);
        }
        onBindViewHolder(holder, mCursor);
    }

    public Cursor getmCursor(){
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if(mDataValid && mCursor !=null){
            return mCursor.getCount();
        } else{
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if(hasStableIds() && mDataValid && mCursor !=null){
            if(mCursor.moveToPosition(position)){
                return mCursor.getLong(mRowIDColumn);
            }else{
                return RecyclerView.NO_ID;
            }
        }else{
            return RecyclerView.NO_ID;
        }
    }



    /**
     * 현 존재하는 해당 데이터(리뷰,비디오)가 종료(삭제)되면=> 현존 커서가 새로운 커서로 변경한다.
     */

    /**
     * Add 15.9.14 11:01 예전가 리턴되면 새로운 커서로 교체한다.
     */
    public Cursor swapCursor(Cursor newCursor){
        if(newCursor==mCursor)
            return null;

        Cursor oldCursor=mCursor;
        int itemCount=getItemCount();
        mCursor=newCursor;

        if(newCursor !=null){
            mRowIDColumn=newCursor.getColumnIndexOrThrow("_id");
            mDataValid=true;
            // 새로운 커서에 대한 관찰자를 알려준다.
            notifyDataSetChanged();
        }else{
            mRowIDColumn=-1;
            mDataValid=false;
            //데이타 셋이 부족함에 대해 관찰자에게 알린다.
            notifyItemRangeRemoved(0,itemCount);
        }
        return oldCursor;
    }

}





























