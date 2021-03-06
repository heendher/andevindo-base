package com.andevindo.andevindobase.Adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andevindo.andevindobase.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heendher on 8/7/2016.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder<T>> {

    private static int PROGRESS = 100;
    private List<T> mList;
    private Context mContext;
    private int mPage = 1;
    private boolean mIsLoading = true;
    private boolean mFirstLoad;
    private boolean mSomethingError;
    private int mVisibleThreshold = 5;
    private boolean mIsNull;
    private int mLastVisibleItem, mTotalItemCount, mFirstVisibleItem, mVisibleItemCount, mPreviousTotal = 0;
    private BaseAdapterListener<T> mPresenter;
    private BaseAdapterLoadMoreListener mLoadMoreListener;

    public BaseAdapter(Context context) {
        mContext = context;
    }

    public BaseAdapter(List<T> list, Context context) {
        mList = list;
        mContext = context;
    }

    public BaseAdapter(Context context, BaseAdapterLoadMoreListener listener, RecyclerView recyclerView) {
        mContext = context;
        mLoadMoreListener = listener;
        setLoadMorePresenter(recyclerView);
    }

    public void setBaseListener(BaseAdapterListener<T> presenter) {
        mPresenter = presenter;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public BaseViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == PROGRESS) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_load_more_progress, parent, false);
            return new ProgressViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(itemViewId(viewType), parent, false);
            return bindData(view);
        }


    }

    public static class ProgressViewHolder extends BaseViewHolder {

        public ProgressViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(Object o) {

        }
    }

    protected abstract BaseViewHolder bindData(View view);

    protected abstract int itemViewId(int viewType);

    protected BaseAdapterListener<T> getBaseAdapter() {
        return mPresenter;
    }

    public interface BaseAdapterListener<T> {
        void onClick(T t);

        void onLongClick(T t);
    }

    private void setLoadMorePresenter(RecyclerView recyclerView) {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalItemCount = linearLayoutManager.getItemCount();
                mVisibleItemCount = linearLayoutManager.getChildCount();
                mFirstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!mIsLoading && (mTotalItemCount - 1) <= lastVisibleItem && !mIsNull && mFirstLoad && !mSomethingError) {
                    mPage++;
                    mLoadMoreListener.onLoadMore(mPage);
                    mIsLoading = true;
                }

                if (lastVisibleItem == mTotalItemCount - 2) {
                    mSomethingError = false;
                }

            }
        });
    }

    public interface BaseAdapterLoadMoreListener {
        void onLoadMore(int page);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<T> holder, int position) {
        if (!(holder instanceof ProgressViewHolder)) {
            holder.bindData(mList.get(position));
            holder.setBasePresenter(mList.get(position), mPresenter);
        }

    }

    public void setData(List<T> list) {
        mPage = 1;
        mIsLoading = false;
        mIsNull = false;
        mFirstLoad = true;
        mSomethingError = false;
        mList = list;
        if (mLoadMoreListener != null && mList != null)
            mList.add(null);
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mList;
    }

    public void addData(T t) {
        if (mList == null) {
            mList = new ArrayList<>(1);
        }
        mList.add(t);
        int index = mList.indexOf(t);
        notifyItemInserted(index);
        mIsLoading = false;
    }

    public void addMoreData(List<T> list) {
        if (mList != null && list != null) {
            changeProgressData(list.get(0));
            list.remove(0);
            int startIndex = mList.size();
            if (list.size() != 0) {
                mList.addAll(list);
                mList.add(null);
                notifyItemInserted(startIndex);
            }

            mIsLoading = false;
        }
    }

    private void changeProgressData(T t) {
        if (mList != null) {
            if (mList.size() > 0) {
                int lastIndex = mList.size() - 1;
                mList.set(lastIndex, t);
                notifyItemChanged(lastIndex);
            }
        }
    }

    public void setIsNull() {
        if (mList != null && mLoadMoreListener != null && !mIsNull)
            removeProgress(true);
        mIsNull = true;
    }

    public void setSomethingError() {
        if (mList != null && mLoadMoreListener != null && !mSomethingError)
            removeProgress(true);
        mSomethingError = true;
    }

    public void addProgress() {
        if (mList != null)
            mList = new ArrayList<>(1);
        int lastIndex = mList.size();
        mList.add(null);
        notifyItemInserted(lastIndex);
    }

    public void removeProgress(boolean notify) {
        if (mList != null) {
            if (mList.size() > 0) {
                int lastIndex = mList.size() - 1;
                if (mList.get(lastIndex) == null){
                    mList.remove(lastIndex);
                    if (notify)
                        notifyItemRemoved(lastIndex);
                }
            }
        }
    }

    public void addData(List<T> list) {
        if (mList == null) {
            mList = new ArrayList<>(1);
        }
        int startIndex = mList.size();
        mList.addAll(list);
        notifyItemInserted(startIndex);
        mIsLoading = false;
    }

    public void removeDataById(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeData(T t) {
        int index = mList.indexOf(t);
        mList.remove(t);
        notifyItemRemoved(index);
    }

    public T getDataById(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        else
            return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position) == null)
            return PROGRESS;
        else
            return super.getItemViewType(position);
    }
}
