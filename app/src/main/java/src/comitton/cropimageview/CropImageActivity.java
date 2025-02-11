package src.comitton.cropimageview;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.appcompat.app.AppCompatActivity;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.imageview.ImageManager;


public class CropImageActivity extends AppCompatActivity implements Runnable, TextWatcher, CropImageView.CropCallback{
    private String mFile;	        	// ZIPファイル名
    private String mPath;				// ベースuriからのパス名 (SAF未対応 (ComittoNxX v3.0.0 未満))
    private String mURI;		            // ベースディレクトリのuri
    private String mUser;			    // SMB認証用
    private String mPass;			    // SMB認証用
    private String mCropPath;           // 画像を切り取るファイル名
    private Handler mHandler;

    private CropImageView mCropImageView;
    private ProgressDialog mProgress;
    private EditText mEditAspectRatio;
    float mAspectRatio;

    private Thread mThread;
    private Bitmap mBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cropimage);

        Intent intent = getIntent();
        mCropPath = intent.getStringExtra("uri");   // 画像を切り取るファイル名
        if(mCropPath == null) {
            mURI = intent.getStringExtra("Uri");    // ベースディレクトリのURI
            mPath = intent.getStringExtra("Path");  // ベースURIからの相対パス名
            mFile = intent.getStringExtra("File");  // ZIPファイル名
            mUser = intent.getStringExtra("User");  // SMB認証用
            mPass = intent.getStringExtra("Pass");  // SMB認証用
        }
//        mUri = Uri.parse("file://" + uri);
        mAspectRatio = intent.getFloatExtra("aspectRatio", 3.0f / 4.0f);

        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);
        mCropImageView.setAspectRatio(mAspectRatio);
        mCropImageView.setCallback(this);

        mProgress = new ProgressDialog(this, R.style.MyDialog);
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void run() {
        boolean debug = false;
        Message message = new Message();
        message.what = DEF.HMSG_ERROR;
        ImageManager imageMgr = null;
        if(mCropPath == null) {
            imageMgr = new ImageManager(this, mPath, mFile, mUser, mPass, ImageManager.FILESORT_NAME_UP, handler,true, ImageManager.OPENMODE_THUMBSORT, 1);
            imageMgr.LoadImageList(0, 0, 0);
            mCropPath = imageMgr.decompFile(0, null);
        }
        if(mCropPath != null) {
            try {
                if(debug) {Log.d("CropImageActivity", "run: イメージファイルを開きます. mURI=" + mURI);}
                if(debug) {Log.d("CropImageActivity", "run: mCropPath=" + mCropPath);}
                mBitmap = imageMgr.GetBitmapFromPath(this, mCropPath, null);
                if (mBitmap == null) {
                    Log.e("CropImageActivity", "run: ビットマップ取得に失敗しました.");
                }
                else {
                    if(debug) {Log.d("CropImageActivity", "run: ビットマップ取得に成功しました.");}
                    message.what = DEF.HMSG_LOAD_END;
                }
                if (mBitmap.getHeight() > 1000) {
                    Log.e("CropImageActivity", "run: ビットマップを縮小します.");
                    float dsH = (float)1000 / (float)mBitmap.getHeight();
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(mBitmap.getWidth() * dsH), (int)(mBitmap.getHeight() * dsH), true);
                }
            } catch (Exception e) {
                Log.e("CropImageActivity", "run: エラーが発生しました.");
                if (e.getLocalizedMessage() != null) {
                    Log.e("CropImageActivity", "run: エラーメッセージ. " + e.getLocalizedMessage());
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

    private final Handler handler = new Handler() {
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
                case DEF.HMSG_PROGRESS:
                    if (message.obj != null) {
                        mProgress.setMessage(message.obj.toString());
                    }
                    break;
            }
        }
    };

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