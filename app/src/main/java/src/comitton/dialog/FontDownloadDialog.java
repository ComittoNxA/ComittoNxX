package src.comitton.dialog;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import src.comitton.common.DEF;

import jp.dip.muracoro.comittonx.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FontDownloadDialog extends Dialog implements Runnable, Handler.Callback, OnClickListener, OnDismissListener {
	public static final int MSG_MESSAGE = 1;
	public static final int MSG_SETMAX = 2;
	public static final int MSG_PROGRESS = 3;
	public static final int MSG_ERRMSG = 4;

	private Thread mThread;
	private boolean mBreak;
	private Handler mHandler;
	private Context mContext;

	private TextView mMsgText;
	private TextView mProgressText;
	private ProgressBar mProgress;
	private Button mBtnCancel;

	private String mURL;
	private String mFileName;
	private int mFileSize;

	public FontDownloadDialog(Context context, String url, String filename, int filesize) {
		super(context);
		Window dlgWindow = getWindow();

		// 画面をスリープ有効
		dlgWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// タイトルなし
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Activityを暗くしない
		dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		// 背景を透明に
		PaintDrawable paintDrawable = new PaintDrawable(0xC0000000);
		dlgWindow.setBackgroundDrawable(paintDrawable);

		setCanceledOnTouchOutside(false);
		setOnDismissListener(this);

		mURL = url;
		mFileName = filename;
		mFileSize = filesize;

		mContext = context.getApplicationContext();
		mBreak = false;

		mHandler = new Handler(this);
		return;
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		// ディスプレイのインスタンス生成
		Display disp = wm.getDefaultDisplay();
		int cx = disp.getWidth();
		int cy = disp.getHeight();
		int width = Math.min(cx, cy);

		setContentView(R.layout.downloaddialog);

		mMsgText = (TextView)this.findViewById(R.id.text_msg);
		mMsgText.setWidth(width);
		mProgressText = (TextView)this.findViewById(R.id.text_progress);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);
		mProgress = (ProgressBar)this.findViewById(R.id.progress);
		// 最大値/初期値
		mProgress.setMax(1);
		mProgress.incrementProgressBy(0);

		// キャンセル
		mBtnCancel.setOnClickListener(this);

		mThread = new Thread(this);
		mThread.start();
	}

	public void run() {
		// コピー開始
		try {
			downloadFontFile(mURL, mFileName, mFileSize);
		}
		catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null) {
				msg = "Download Error.";
			}
			sendMessage(MSG_ERRMSG, msg, 0, 0);
		}
		// プログレス終了
		this.dismiss();
	}


	/**
	 * フォントファイルをダウンロードします
	 * @param fonturl フォントのURL
	 * @param filename 作成するファイル名
	 * @throws Exception
	 */
	private boolean downloadFontFile(String fonturl, String filename, int fileSize) throws Exception {
		// メッセージを設定
		sendMessage(MSG_MESSAGE, filename, 0, 0);
		sendMessage(MSG_SETMAX, null, 0, (int)fileSize);

		String fontPath = DEF.getFontDirectory();
        File ff = new File(fontPath);
        ff.mkdir();
        fontPath += filename;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor ed = sp.edit();
    	// 既存フォントファイルを削除
        ff = new File(fontPath);
        ff.delete();
		ed.remove(DEF.KEY_TX_FONTNAME);
		ed.commit();

		try {
            int httpStatusCode;
            HttpURLConnection conn;
            while(true) {
                URL url = new URL(fonturl);

                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("GET");
                conn.connect();

                httpStatusCode = conn.getResponseCode();
                if (httpStatusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
        			String location = conn.getHeaderField("Location");
        			if (location != null && location.length() > 0) {
                    	// リダイレクト先設定
        				fonturl = location;
        			}
                }
                else if(httpStatusCode != HttpURLConnection.HTTP_OK){
                	throw new Exception();
                }
                else {
                	break;
                }
            }

            // Input Stream
            DataInputStream dataInStream = new DataInputStream(conn.getInputStream());

            // Output Stream
            DataOutputStream dataOutStream = new DataOutputStream(
            		new BufferedOutputStream(
            				new FileOutputStream(fontPath)));

            // Read Data
            byte[] b = new byte[4096];
            int readByte = 0;
            int total = 0;
            int count = 0;

            while(mBreak == false && -1 != (readByte = dataInStream.read(b))){
            	dataOutStream.write(b, 0, readByte);
            	total += readByte;
            	count ++;
            	if (count % 20 == 0) {
            		sendMessage(MSG_PROGRESS, null, (int)total, (int)fileSize);
            	}
            }

            // Close Stream
            dataInStream.close();
            dataOutStream.close();

        } catch (Exception e) {
        	throw e;
        }

        // 設定する
		ed = sp.edit();
    	ed.putString(DEF.KEY_TX_FONTNAME, filename);
		ed.commit();
		return true;
	}

	private void sendMessage(int msg_id, String obj, int arg1, int arg2) {
		Message message = new Message();
		message.what = msg_id;
		message.arg1 = arg1;
		message.arg2 = arg2;
		message.obj = obj;
		mHandler.sendMessage(message);
	}

	public boolean handleMessage(Message msg) {
		// 受信
		switch (msg.what) {
			case MSG_MESSAGE:
				mMsgText.setText((String) msg.obj);
				return true;
			case MSG_SETMAX:
				mProgress.setMax(msg.arg2);
			case MSG_PROGRESS:
				mProgress.setProgress(msg.arg1);
				mProgressText.setText(msg.arg1 + " / " + msg.arg2);
				return true;
			case MSG_ERRMSG:
				String msgstr = (String)msg.obj;
				Toast.makeText(mContext, msgstr, Toast.LENGTH_LONG).show();
				return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// キャンセルクリック
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// 画面をスリープ有効
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mBreak = true;
	}
}
