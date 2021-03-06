package com.example.android.sample.myplaceapp.camera;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.example.android.sample.myplaceapp.R;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * API Level21未満用のカメラ機能を実装するFragment。
 */
public class CameraLegacyFragment extends Fragment implements OnClickListener{

    /**
     * パーミッションのリクエストコード。
     */
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    /**
     * カメラオブジェクト。
     */
    private android.hardware.Camera mCamera;

    /**
     * プレビューを表示するテクスチャビュー。
     */
    private TextureView mTextureView;

    /**
     * 写真撮影後、ファイルに保存したり、DBに保存するためのスレッド。
     */
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.ShutterButton).setOnClickListener(this);
        mTextureView = (TextureView) view.findViewById(R.id.PreviewTexture);
    }

    @Override
    public void onResume() {
        super.onResume();
        // スレッドを開始する
        startThread();
        // カメラを開く
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // スレッドを停止する
        stopThread();
    }

    /**
     * 画像処理を行うためのスレッドを立てる。
     */
    private void startThread(){
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * スレッドを止める。
     */
    private void stopThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * カメラを起動する。
     */
    private void startCamera(){
        if(mTextureView.isAvailable()){
            // TextureViewはすでに使用可能なので、カメラを開く
            openCamera(mTextureView.getSurfaceTexture(),
                    mTextureView.getWidth(),
                    mTextureView.getHeight());
        }else{
            mTextureView.setSurfaceTextureListener(mTextureListener);
        }
    }

    /**
     * プレビューを表示するテクスチャビューのリスナ。
     */
    private final TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // TextureViewと関連づけられているSurfaceTextureが使用可能になったカメラデバイスへの接続を開始する
            openCamera(surface,width,height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // do nothing.
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // SurfaceTextureが破棄されたので、カメラを解放する
            stopCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // do nothing.
        }
    };

    /**
     * カメラを終了する。
     */
    private void stopCamera(){
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * カメラを起動する。
     *
     * @param surface
     * @param width
     * @param height
     */
    private void openCamera(SurfaceTexture surface,int width,int height){
        // パーミッションチェック
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            // パーミッションを求めるダイアログを表示する
            FragmentCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);

            return;
        }
        // Cameraへのアクセスを取得する
        mCamera = Camera.open();

        try {
            // カメラプレビューの角度を調整する
            setDisplayOrientation();
            // カメラのプレビュー表示用のTextureを設定する
            mCamera.setPreviewTexture(surface);
            // プレビューの表示を開始する
            mCamera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * 端末の向きに合わせてカメラの角度を調整する
     */
    public void setDisplayOrientation(){
        // カメラ情報は、値を格納するためのオブジェクトを先に作成し、Camera#getCameraInfo()でオブジェクトに値を設定してもらう
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0,info);
        // 端末の方向を取得する
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        // 端末の方向に合わせて、調整する値を決定する
        int degrees = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        // 最終的なカメラの角度を計算する
        // 0〜360度に収まるように、360を足した上で、360で割ったあまりを計算する
        int result = ( info.orientation - degrees + 360 ) %360;
        mCamera.setDisplayOrientation(result);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ShutterButton){
            if(mCamera != null){
                mCamera.takePicture(mShutterCallback,
                        null,
                        mPictureCallback);
            }
        }
    }

    /**
     * シャッターボタンを押した時のコールバック。
     */
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // do nothing.
        }
    };

    /**
     * 画像保存時のコールバック。
     */
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // dataはJPEG画像のバイトデータ
            mBackgroundHandler.post(new PictureSaver(getActivity(),data));
            // プレビュー再開
            mCamera.startPreview();
        }
    };

}
