package src.comitton.dialog;

import java.io.OutputStream;
import java.util.ArrayList;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;
import src.comitton.fileaccess.FileAccess;
import src.comitton.fileview.data.FileData;
import src.comitton.fileaccess.WorkStream;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

public class DownloadDialog extends ImmersiveDialog implements Runnable, Handler.Callback, OnClickListener, OnDismissListener {
	private static final String TAG = "DownloadDialog";

	public static final int MSG_MESSAGE = 1;
	public static final int MSG_SETMAX = 2;
	public static final int MSG_PROGRESS = 3;
	public static final int MSG_ERRMSG = 4;

	private String mURI;
	private String mPath;
	private String mUser;
	private String mPass;
	private String mItem;
	private String mLocal;
	private Thread mThread;
	private boolean mBreak;
	private Handler mHandler;

	private TextView mMsgText;
	private TextView mProgressText;
	private ProgressBar mProgress;
	private Button mBtnCancel;

	public DownloadDialog(AppCompatActivity activity, @StyleRes int themeResId, String uri, String path, String user, String pass, String item, String local) {
		super(activity, themeResId);
		boolean debug = false;
		if(debug) {Log.d(TAG, "DownloadDialog: 開始します. uri=" + uri + ", path=" + path + ", item=" + item + ", local=" + local);}

		setCanceledOnTouchOutside(false);
		setOnDismissListener(this);

		mURI = uri;
		mPath = path;
		mUser = user;
		mPass = pass;
		mItem = item;
		mLocal = local;
		mBreak = false;

		mHandler = new Handler(this);
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		WindowManager wm = (WindowManager)mActivity.getSystemService(Context.WINDOW_SERVICE);
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
			downloadFile("", mItem);
		}
		catch (Exception e) {
			String msg = e.getLocalizedMessage();
			if (msg == null) {
				msg = "Download Error.";
			}
			sendMessage(MSG_ERRMSG, msg, 0, 0);
		}
		// プログレス終了
		this.dismiss();
	}

	// path = ローカルのパス
	// name = リモートのファイル名
	public boolean downloadFile(String path, String item) throws Exception {
		boolean debug = false;
		if (debug) {Log.d(TAG, "downloadFile: 開始します. path=" + path + ", item=" + item);}

		String ServerFileUri = DEF.relativePath(mActivity, mURI, mPath, path, item);
		boolean exists = FileAccess.exists(mActivity, ServerFileUri, mUser, mPass);
		if (!exists) {
			// リモートのファイルが存在しない
			throw new Exception("File not found.");
		}

		boolean isDirectory = FileAccess.isDirectory(mActivity, ServerFileUri, mUser, mPass);
		if (isDirectory) {
			// ローカルにディレクトリ作成
			boolean ret = FileAccess.mkdir(mActivity, DEF.relativePath(mActivity, mLocal, path), item, mUser, mPass);
			if (!ret) {
				// ディレクトリ作成に失敗
				return false;
			}

			// 再帰呼び出し
			String childpath = DEF.relativePath(mActivity, path, item);
			ArrayList<FileData> sfiles = FileAccess.listFiles(mActivity, DEF.relativePath(mActivity, mURI, mPath, childpath), mUser, mPass, mHandler);

			int filenum = sfiles.size();
			if (filenum <= 0) {
				// ファイルなし
				return true;
			}
			// ディレクトリ内のファイル
			for (int i = 0; i < filenum; i++) {
				downloadFile(childpath, sfiles.get(i).getName());
				if (mBreak) {
					// 中断
					break;
				}
			}
		}
		else {
			// ダウンロード実行
			try {
				String tmpfile = item + "._dl";
				String tmpFileUri = DEF.relativePath(mActivity, mLocal, path, tmpfile);
				if (!FileAccess.createFile(mActivity, DEF.relativePath(mActivity, mLocal, path), tmpfile, "", "")) {
					sendMessage(MSG_ERRMSG, mActivity.getString(R.string.downErrorMsg), 0, 0);
					Log.e(TAG, "downloadFile: ファイルの作成に失敗しました. path=" + DEF.relativePath(mActivity, mLocal, path) + ", tmpfile=" + tmpfile);
					return false;
				}
				OutputStream localFile = FileAccess.getOutputStream(mActivity, tmpFileUri, "", "");
				WorkStream workStream = new WorkStream(mActivity, ServerFileUri, mUser, mPass, mHandler);

				// ファイルサイズ取得
				long fileSize = workStream.length();
				if ((fileSize & 0xFFFFFFFF00000000L) != 0 && (fileSize & 0x00000000FFFFFFFFL) == 0) {
					fileSize >>= 32;
				}

				// メッセージを設定
				sendMessage(MSG_MESSAGE, path + item, 0, 0);
				sendMessage(MSG_SETMAX, null, 0, (int)fileSize);

				byte[] buff = new byte[1024 * 16];
				int size;
				long total = 0;
				while (true) {
					// 読み込み
					size = workStream.read(buff, 0, buff.length);
					if (mBreak) {
						// 中断
						localFile = null;
						FileAccess.delete(mActivity, tmpFileUri, mUser, mPass);
						return false;
					}
					if (size <= 0) {
						break;
					}
					try {
						// 書き込み
						localFile.write(buff, 0, size);
					}
					catch (Exception e) {
						sendMessage(MSG_ERRMSG, mActivity.getString(R.string.downErrorMsg), 0, 0);
						Log.e(TAG, "downloadFile: Exception1: " + e.getLocalizedMessage());
						return false;
					}
					total += size;
					sendMessage(MSG_PROGRESS, null, (int)total, (int)fileSize);
				}
				// クローズ
				localFile.close();
				localFile = null;
				workStream.close();
				workStream = null;

				// リネーム
				String dstfile = null;
				int idx = item.lastIndexOf(".");
				String filename = item.substring(0, idx);
				String extname = item.substring(idx);

				for (int i = 0; i < 10; i++) {
					if (i == 0) {
						dstfile = item;
					}
					else {
						dstfile = filename + "(" + i + ")" + extname;
					}
					exists = FileAccess.exists(mActivity, DEF.relativePath(mActivity, mLocal, path, dstfile), mUser, mPass);
					if (!exists) {
						break;
					}
				}
				if (!FileAccess.renameTo(mActivity, DEF.relativePath(mActivity, mLocal, path), tmpfile, dstfile, mUser, mPass)) {
					// リネーム失敗ならダウンロードしたファイルを削除
					Log.e(TAG, "downloadFile: ファイル名の変更に失敗しました. path=" + DEF.relativePath(mActivity, mLocal, path) + ", tmpfile=" + tmpfile + ", item=" + item);
					FileAccess.delete(mActivity, tmpFileUri, mUser, mPass);
				}
			}
			catch (Exception e) {
				sendMessage(MSG_ERRMSG, mActivity.getString(R.string.downErrorMsg), 0, 0);
				Log.e(TAG, "downloadFile: Exception2: " + e.getLocalizedMessage());
				return false;
			}
		}
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
				Toast.makeText(mActivity, msgstr, Toast.LENGTH_LONG).show();
				return true;
			case DEF.HMSG_WORKSTREAM:
				// ファイルアクセスの表示
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
