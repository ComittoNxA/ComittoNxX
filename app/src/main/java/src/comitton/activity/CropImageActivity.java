package src.comitton.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.stream.CallImgLibrary;
import src.comitton.stream.ImageManager;
import src.comitton.stream.WorkStream;
import src.comitton.view.image.CropImageView;
import src.comitton.data.FileData;


public class CropImageActivity extends Activity implements Runnable, TextWatcher, CropImageView.CropCallback{
    private String mFile;
    private String mPath;
    private String mUser;
    private String mPass;
    private String mCharset;
    private String mCropPath;

    private CropImageView mCropImageView;
    private ProgressDialog mProgress;
    private EditText mEditAspectRatio;
    float mAspectRatio;

    private Thread mThread;
    private Bitmap mBitmap;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void run() {
        Message message = new Message();
        message.what = DEF.HMSG_ERROR;
        ImageManager imageMgr = null;
        if(mCropPath == null) {
            imageMgr = new ImageManager(this, mPath, mFile, mUser, mPass, ImageManager.FILESORT_NAME_UP, handler, mCharset, true, ImageManager.OPENMODE_THUMBSORT, 1);
            imageMgr.LoadImageList(0, 0, 0);
            mCropPath = imageMgr.decompFile(0, "croptmp");
        }
        if(mCropPath != null) {
            try {
                Log.d("CropImageActivity", "run: イメージファイルを開きます. mUri=" + mPath + ", mFile=" + mFile);
                int ret;
                int width;
                int height;
                WorkStream ws = null;
                File fileObj;
                int extType = 0;
                long orglen = 0;

                String path = mCropPath.substring(0, mCropPath.lastIndexOf("/") + 1);
                String file = mCropPath.substring(mCropPath.lastIndexOf("/") + 1);

                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inJustDecodeBounds = true;

                Log.d("CropImageActivity", "run: サイズ取得(BitmapFactory)を実行します. pathname=" + mCropPath);
                BitmapFactory.decodeFile(mCropPath, option);
                width = option.outWidth;
                height = option.outHeight;

                if (width > 0 && height > 0) {
                    Log.d("CropImageActivity", "run: サイズ取得(BitmapFactory)に成功しました.");
                } else {
                    Log.d("CropImageActivity", "run: サイズ取得(BitmapFactory)に失敗しました.");
                    Log.d("CropImageActivity", "run: ImageManagerを作成します. path=" + path + ", cmpfile=" + file);
                    //imageMgr = new ImageManager(this, path, file, "", "", ImageManager.FILESORT_NAME_UP, handler, "", true, ImageManager.OPENMODE_THUMBSORT, 1);
                    Log.d("CropImageActivity", "run: WorkStreamを作成します. uri=" + path + ", path=" + file);
                    ws = new WorkStream("", mCropPath, "", "", false);
                    Log.d("CropImageActivity", "run: Fileオブジェクトを作成します. pathname=" + mPath + mFile);
                    try {
                        fileObj = new File(path + file);
                    } catch (Exception e) {
                        Log.e("CropImageActivity", "run: Fileオブジェクトを作成中にエラーが発生しました.");
                        if (e != null && e.getMessage() != null) {
                            Log.e("CropImageActivity", "run: エラーメッセージ. " + e.getMessage());
                        }
                        throw new RuntimeException(e);
                    }
                    Log.d("CropImageActivity", "run: ファイルサイズを取得します.");
                    try {
                        orglen = fileObj.length();
                    } catch (Exception e) {
                        Log.e("CropImageActivity", "run: ファイルサイズの取得中にエラーが発生しました.");
                        if (e != null && e.getMessage() != null) {
                            Log.e("CropImageActivity", "run: エラーメッセージ. " + e.getMessage());
                        }
                        throw new RuntimeException(e);
                    }
                    if (orglen == 0) {
                        Log.e("CropImageActivity", "run: ファイルサイズの取得に失敗しました.");
                    }
                    Log.d("CropImageActivity", "run: ファイルタイプを取得します.");
                    if (file.lastIndexOf(".") != -1) {
                        extType = FileData.getExtType(file.substring(file.lastIndexOf(".")));
                    } else {
                        Log.e("CropImageActivity", "run: 拡張子がありません.");
                    }
                    //Log.d("CropImageActivity", "run: イメージバッファを作成します.");
                    //ret = CallImgLibrary.ImageInitialize(orglen * 2, 0, 1, 1);
                    //if (ret < 0) {
                    //    Log.e("CropImageActivity", "run: イメージバッファの作成に失敗しました.");
                    //   return;
                    //}
                    int[] imagesize = new int[2];
                    Log.d("CropImageActivity", "run: サイズ取得(Native)を実行します. type=" + extType + ", orglen=" + orglen);
                    ret = ImageManager.sizeCheckNativeMain(ws, -1, extType, orglen, imagesize);
                    if (ret == 0 && imagesize[0] > 0 && imagesize[1] > 0) {
                        Log.d("CropImageActivity", "run: サイズ取得(Native)に成功しました.");
                        width = imagesize[0];
                        height = imagesize[1];
                    } else {
                        Log.e("CropImageActivity", "run: サイズ取得(Native)に失敗しました.");
                        return;
                    }
                }
                Log.d("CropImageActivity", "run: イメージファイルのサイズ. width=" + width + ", height=" + height);

                if (width > 0 && height > 0) {
                    // 縮小してファイル読込
                    Log.d("CropImageActivity", "run: イメージデータを取得します.");
                    option.inJustDecodeBounds = false;
                    option.inPreferredConfig = Bitmap.Config.RGB_565;
                    // 最低縦1000pxに縮小して画像読込
                    int sampleSize = DEF.calcThumbnailScale(option.outWidth, option.outHeight, 0, 1000);
                    option.inSampleSize = sampleSize;

                    Log.d("CropImageActivity", "run: イメージデータ取得(BitmapFactory)を実行します. pathname=" + mCropPath);
                    mBitmap = BitmapFactory.decodeFile(mCropPath, option);
                    if (mBitmap != null) {
                        message.what = DEF.HMSG_LOAD_END;
                    }
                    ws.seek(0);
                    try {
                        mBitmap = BitmapFactory.decodeFile(mCropPath, option);
                    } catch (Exception e) {
                        Log.e("CropImageActivity", "run: イメージデータ取得(BitmapFactory)でエラーが発生しました.");
                        if (e != null && e.getMessage() != null) {
                            Log.e("CropImageActivity", "run: エラーメッセージ. " + e.getMessage());
                            return;
                        }
                    }
                    if (mBitmap != null) {
                        Log.d("CropImageActivity", "run: イメージデータ取得(BitmapFactory)に成功しました.");
                    } else {
                        Log.d("CropImageActivity", "run: イメージデータ取得(BitmapFactory)に失敗しました.");
                        ws.seek(0);
                        mBitmap = ImageManager.getBitmapNativeMain(ws, -1, extType, sampleSize, orglen, width, height, mBitmap);
                        if (mBitmap != null) {
                            Log.d("CropImageActivity", "run: イメージデータ取得(Native)に成功しました.");
                        } else {
                            Log.e("CropImageActivity", "run: イメージデータ取得(Native)に失敗しました.");
                            return;
                        }
                    }
                    if (mBitmap == null) {
                        Log.d("CropImageActivity", "run: イメージファイルを取得できませんでした.");
                        // NoImageであればステータス設定
                        return;
                        //CallImgLibrary.ThumbnailSetNone(mID, index);
                    } else {
                        Log.d("CropImageActivity", "run: イメージファイルを取得できました.");
                        message.what = DEF.HMSG_LOAD_END;
                    }
                }
            } catch (Exception e) {
                Log.e("CropImageActivity", "run: エラーが発生しました.");
                if (e != null && e.getMessage() != null) {
                    Log.e("CropImageActivity", "run: エラーメッセージ. " + e.getMessage());
                }
                throw new RuntimeException(e);
            } finally {
                try {
                    imageMgr.close();
                } catch (Exception e) {
                    // なにもしない
                }
            }
        }
        mProgress.dismiss();
        handler.sendMessage(message);
    }

    public void cropCallback(float aspectRatio){
        mEditAspectRatio.setText(String.format("%.3f", aspectRatio));
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message message){
            switch (message.what){
                case DEF.HMSG_LOADING: // ロード状況表示
                    mProgress.setMessage( String.valueOf(message.arg1 >> 24) + "%\n" + ((float) message.arg2 / 10) + "KB/S");
                    break;
                case DEF.HMSG_LOAD_END:
                    mCropImageView.setImageBitmap(mBitmap);
                    break;
                case DEF.HMSG_ERROR:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cropimage);

        Intent intent = getIntent();
        mCropPath = intent.getStringExtra("uri");
        if(mCropPath == null) {
            mPath = intent.getStringExtra("Path");
            mFile = intent.getStringExtra("File");
            mUser = intent.getStringExtra("User");
            mPass = intent.getStringExtra("Pass");
            mCharset = intent.getStringExtra("Charset");
        }
//        mUri = Uri.parse("file://" + uri);
        mAspectRatio = intent.getFloatExtra("aspectRatio", 3.0f / 4.0f);

        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);
        mCropImageView.setAspectRatio(mAspectRatio);
        mCropImageView.setCallback(this);

        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setMessage("Loading...");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.show();

        mThread = new Thread(this);
        mThread.start();


        // 以下はイベントハンドラ
        Button cropButton = (Button) findViewById(R.id.btn_ok);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // フレーム内をクロップ
                mCropImageView.Crop(mCropPath);
                Intent intent = new Intent();
                intent.setData(Uri.parse("file://" + mCropPath));
//                intent.putExtra("uri", mUri.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mEditAspectRatio = (EditText) findViewById(R.id.edit_aspect);
        mEditAspectRatio.setText(String.valueOf(mAspectRatio));
        mEditAspectRatio.addTextChangedListener(this);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        // アス比減少・増加ボタン
        Button btnMinus = (Button) findViewById(R.id.btn_minus);
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAspectRatio -= 0.01f;
                mEditAspectRatio.setText(String.format("%.3f", mAspectRatio));
            }
        });
        Button btnPlus = (Button) findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAspectRatio += 0.01f;
                mEditAspectRatio.setText(String.format("%.3f", mAspectRatio));
            }
        });

        // 左右移動ボタン
        Button btnLeft = (Button) findViewById(R.id.btn_left);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              mCropImageView.move(-0.01f);
            }
        });
        Button btnRight = (Button) findViewById(R.id.btn_right);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.move(0.01f);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mAspectRatio = Float.parseFloat(s.toString());
        if(mAspectRatio < 0.1f)
            mAspectRatio = 0.1f;
        mCropImageView.setAspectRatio(mAspectRatio);
    }
}