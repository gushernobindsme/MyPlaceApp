package com.example.android.sample.myplaceapp.camera;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.sample.myplaceapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 画像をリスト表示するフラグメント。
 */
public class PictureFragment extends Fragment {
    private static final int DB_LOADER = 1;
    private static final String ARGS_DATE = "date";
    private GalleryAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private ImageCache mCache = new ImageCache();

    /**
     * インスタンスを生成する。
     *
     * @return
     */
    public static PictureFragment newInstance(String dateStr) {
        PictureFragment fragment = new PictureFragment();

        // 日付の指定をBundleに詰める
        Bundle args = new Bundle();
        args.putString(ARGS_DATE,dateStr);

        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.Gallery);
        mRecyclerView.setLayoutManager(new GridLayoutManager(view.getContext(),2));
    }

    public void setDate(String dateStr) {
        getArguments().putString(ARGS_DATE, dateStr);
        // Loaderを初期化する
        getLoaderManager().restartLoader(DB_LOADER, getArguments(), mDBLoaderCallback);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().restartLoader(DB_LOADER,getArguments(),mDBLoaderCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // キャッシュをクリア
        mCache.clear();

        // Loaderを破棄
        getLoaderManager().destroyLoader(DB_LOADER);
    }

    /**
     * DBを検索するLoaderのコールバック。
     */
    private LoaderManager.LoaderCallbacks<List<String>> mDBLoaderCallback = new LoaderManager.LoaderCallbacks<List<String>>() {

        @Override
        public Loader<List<String>> onCreateLoader(int id, Bundle args) {
            DatabaseLoader loader = new DatabaseLoader(getActivity(),args.getString(ARGS_DATE));
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
            mAdapter = new GalleryAdapter(getActivity(),data);
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {
            mAdapter.clear();
        }
    };

    private static class DatabaseLoader extends AsyncTaskLoader<List<String>>{

        String dateStr;

        /**
         * コンストラクタ。
         *
         * @param context
         * @param dateStr
         */
        public DatabaseLoader(Context context, String dateStr) {
            super(context);
            this.dateStr = dateStr;
        }

        @Override
        public List<String> loadInBackground() {
            // 画像ファイル用のデータベース
            PictureDBHelper helper = new PictureDBHelper(getContext());
            SQLiteDatabase database = helper.getReadableDatabase();

            List<String> filePaths = new ArrayList<String>();

            // 指定した日付の画像を検索する
            Cursor cursor = database.query(PictureDBHelper.TABLE_NAME,
                    null,
                    PictureDBHelper.COLUMN_DATE_STR + " like ?",
                    new String[]{dateStr},
                    null,
                    null,
                    PictureDBHelper.COLUMN_REGISTER_TIME + " DESC");

            if(cursor == null) return filePaths;

            // Cursorからファイルパスをリストに詰める
            while(cursor.moveToNext()){
                String path = cursor.getString(cursor.getColumnIndex(PictureDBHelper.COLUMN_FILE_PATH));
                filePaths.add(path);
            }

            cursor.close();
            database.close();

            return filePaths;

        }
    }

    /**
     * 画像を表示するためのアダプタ。
     */
    private class GalleryAdapter extends RecyclerView.Adapter<GalleryVH> {

        private List<String> files = new ArrayList<String>();
        private LayoutInflater inflater;
        private int viewHeight;

        /**
         * コンストラクタ。
         *
         * @param context
         * @param files
         */
        public GalleryAdapter(Context context,List<String> files) {
            this.inflater = LayoutInflater.from(context);
            this.files = files;
            this.viewHeight = (int) context.getResources().getDimension(R.dimen.grid_item_height);
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        /**
         * 画像をクリアする。
         */
        public void clear(){
            files.clear();
            notifyDataSetChanged();
        }

        @Override
        public GalleryVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.grid_item_picture,parent,false);
            return new GalleryVH(view);
        }

        @Override
        public void onBindViewHolder(GalleryVH holder, int position) {
            String file = files.get(position);

            // キャッシュからBitmapを探す
            Bitmap bitmap = mCache.get(file);

            if(bitmap == null){
                // 見つからない場合は、非同期的に取得する
                new ImageLoadTask(position,file,viewHeight)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,(Void[])null);
                holder.imageView.setImageDrawable(null);
            } else{
                // 見つかった場合は、表示する
                holder.imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 画像を非同期にロードするためのAsyncTask。
     */
    private class ImageLoadTask extends AsyncTask<Void,Void,Boolean>{
        private int position;
        private int viewHeight;
        private String filePath;

        /**
         * コンストラクタ。
         *
         * @param position
         * @param url
         * @param viewHeight
         */
        public ImageLoadTask(int position, String url,int viewHeight) {
            this.position = position;
            this.filePath = url;
            this.viewHeight = viewHeight;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            File file = new File(filePath);

            if(!file.exists()) return false;

            // ファイル読み込み時のオプション
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 画像サイズだけを読み込む設定
            options.inJustDecodeBounds = true;

            // 指定されたファイルから、画像サイズだけを読み込む
            BitmapFactory.decodeFile(filePath,options);
            // 画像の高さ
            int imageHeight = options.outHeight;

            // 縮小率を計算する。1:等倍、2:1辺が1/2になる
            // 2の累乗以外の数字を指定した場合には、その値以下の2の累乗にまとめられる
            // 3の場合には2、6の場合には4など
            int inSampleSize = 1;
            if (imageHeight > viewHeight){
                inSampleSize = Math.round(imageHeight / viewHeight);
            }

            // 画像サイズだけを読み込む設定を解除
            options.inJustDecodeBounds = false;
            // 縮小率を設定
            options.inSampleSize = inSampleSize;

            // 画像を読み込む
            Bitmap bitmap = BitmapFactory.decodeFile(filePath,options);
            // キャッシュに入れる
            mCache.put(filePath,bitmap);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result && mAdapter != null) {
                // 指定された位置の画像が変化したことを知らせて、再描画させる
                mAdapter.notifyItemChanged(position);
            }
        }
    }

    /**
     * 画像を表示するためのViewHolder。
     */
    private static class GalleryVH extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public GalleryVH(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.Picture);
        }
    }
}
