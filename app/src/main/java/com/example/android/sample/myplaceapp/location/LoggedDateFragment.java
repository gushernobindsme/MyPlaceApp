package com.example.android.sample.myplaceapp.location;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.sample.myplaceapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ナビゲーションドロワーのフラグメント。
 */
public class LoggedDateFragment extends ListFragment {

    /**
     * 記録をつけた日付を選択した際の処理を定義するリスナ。
     */
    public interface LoggedDateFragmentListener {

        /**
         * 日付を選択した時の処理。
         *
         * @param date
         */
        void onDateSelected(String date);
    }

    /**
     * 日付を読み込むLoader。
     */
    private static final int DATE_LOADER = 1;

    /**
     * リストのアダプタ。
     */
    private DateAdapter mAdaper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof LoggedDateFragmentListener)){
            throw new RuntimeException(context.getClass().getSimpleName() + " does not implement LoggedDateFragmentListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 空のアダプタをセットする
        mAdaper = new DateAdapter(getActivity());
        setListAdapter(mAdaper);

        // Loaderを初期化する
        getLoaderManager().restartLoader(DATE_LOADER,getArguments(),mLoaderCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Loaderを破棄
        getLoaderManager().destroyLoader(DATE_LOADER);
    }

    /**
     * コールバック。
     */
    private LoaderManager.LoaderCallbacks<List<String>> mLoaderCallback = new LoaderManager.LoaderCallbacks<List<String>>() {

        @Override
        public LoggedDateLoader onCreateLoader(int id, Bundle args) {
            if(id == DATE_LOADER){
                LoggedDateLoader loader = new LoggedDateLoader(getActivity());
                loader.forceLoad();
                return loader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
            if(loader.getId() == DATE_LOADER){
                mAdaper.addItems(data);
                mAdaper.notifyDataSetChanged();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {
            if(loader.getId() == DATE_LOADER){
                mAdaper.clearAll();
            }
        }
    };

    /**
     * 写真を撮った日付を取得するためのAsyncTaskLoader。
     */
    private static class LoggedDateLoader extends AsyncTaskLoader<List<String>>{

        /**
         * コンストラクタ。
         *
         * @param context
         */
        public LoggedDateLoader(Context context){
            super(context);
        }

        @Override
        public List<String> loadInBackground() {
            return PlaceRepository.getAllDateString(getContext());
        }
    }

    /**
     *
     */
    private static class DateAdapter extends BaseAdapter{

        private LayoutInflater mInflater;
        private List<String> mLoggedDates;

        /**
         * コンストラクタ。
         */
        public DateAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            this.mLoggedDates = new ArrayList<String>();
        }

        /**
         * データを追加する。
         *
         * @param items
         */
        public void addItems(List<String> items){
            mLoggedDates.addAll(items);
            notifyDataSetChanged();
        }

        /**
         * データを全件削除する。
         */
        public void clearAll(){
            mLoggedDates.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mLoggedDates.size();
        }

        @Override
        public Object getItem(int position) {
            return mLoggedDates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if(convertView == null){
                view = mInflater.inflate(R.layout.list_item_date,parent,false);
                view.setTag(new ViewHolder(view));
            }else{
                view = convertView;
            }

            ViewHolder holder = (ViewHolder) view.getTag();

            String dateString = mLoggedDates.get(position);

            holder.loggedDate.setText(dateString);

            return view;
        }

        /**
         * ViewHolder。
         */
        private static class ViewHolder{
            private TextView loggedDate;

            /**
             * コンストラクタ。
             *
             * @param view
             */
            ViewHolder(View view) {
                this.loggedDate = (TextView) view.findViewById(R.id.LoggedDate);
            }
        }

    }

}
