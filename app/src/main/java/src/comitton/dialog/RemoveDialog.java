package src.comitton.dialog;

import java.util.ArrayList;
import java.util.EventListener;

import src.comitton.fileview.FileSelectActivity;
import src.comitton.fileaccess.FileAccess;

import jp.dip.muracoro.comittonx.R;
import src.comitton.fileview.data.FileData;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StyleRes;

public class RemoveDialog extends ImmersiveDialog implements Runnable, Handler.Callback, OnClickListener, OnDismissListener {
	public static final int MSG_MESSAGE = 1;
	public static final int MSG_ERRMSG = 2;

	private RemoveListener mListener = null;

	private FileSelectActivity mActivity;

	private String mFullPath;
	private String mUser;
	private String mPass;
	private String mItem;
	private Thread mThread;
	private boolean mBreak;
	private Handler mHandler;

	private TextView mMsgText;
	private Button mBtnCancel;

	public RemoveDialog(FileSelectActivity activity, @StyleRes int themeResId, String uri, String path, String user, String pass, String item, RemoveListener removeListener) {
		super(activity, themeResId);
		mActivity = activity;
		Window dlgWindow = getWindow();
		Log.d("RemoveDialog", "RemoveDialog uri=" + uri + ", path=" + path + ", user=" + user + ", pass=" + pass + ", item=" + item);

		// 画面をスリープ有効
		dlgWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// 背景を設定
		dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe_transparent);

		setCanceledOnTouchOutside(false);
		setOnDismissListener(this);

		mListener = removeListener;

		mFullPath = uri + path;
		mUser = user;
		mPass = pass;
		mBreak = false;

		mHandler = new Handler(Looper.getMainLooper());
	}

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		View mRootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
		int cx = mRootView.getWidth();
		int cy = mRootView.getHeight();
		int width = Math.min(cx, cy);

		setContentView(R.layout.removedialog);

		mMsgText = (TextView)this.findViewById(R.id.text_msg);
		mMsgText.setWidth(width);
		mBtnCancel  = (Button)this.findViewById(R.id.btn_cancel);

		// キャンセル
		mBtnCancel.setOnClickListener(this);

		mThread = new Thread(this);
		mThread.start();
	}

	public void run() {
		// コピー開始
		try {
			removeFile(mItem, "");
		}
		catch (Exception e) {
			String msg;
			if (e.getLocalizedMessage() != null) {
				msg = e.getLocalizedMessage();
			}
			else {
				msg = "File Access Error.";
			}
			sendMessage(MSG_ERRMSG, msg, 0, 0);
		}
		// プログレス終了
		this.dismiss();
	}

	public boolean removeFile(String path, String item) throws Exception {
		boolean isDirectory = FileAccess.isDirectory(mActivity, path, mUser, mPass);
		if (isDirectory) {
			// 再帰呼び出し
			ArrayList<FileData> lfiles = FileAccess.listFiles(mActivity, path, mUser, mPass, mHandler);

			int filenum = lfiles.size();
			if (filenum > 0) {
				// ファイルあり
				// ディレクトリ内のファイル
				for (int i = 0; i < filenum; i++) {
					if (lfiles.get(i).getName().equals("..")) {
						continue;
					}
					removeFile(lfiles.get(i).getURI(), item + lfiles.get(i).getName());
					if (mBreak) {
						// 中断
						break;
					}
				}
			}
			FileAccess.delete(mActivity, path, mUser , mPass);
		}
		else {
			// 削除ファイル表示
			sendMessage(MSG_MESSAGE, item, 0, 0);

			FileAccess.delete(mActivity, path, mUser , mPass);
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
			case MSG_ERRMSG:
				String msgstr = (String)msg.obj;
				Toast.makeText(mActivity, msgstr, Toast.LENGTH_LONG).show();
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
		mListener.onClose();
		mActivity.loadThumbnail();
	}

	public interface RemoveListener extends EventListener {
	    // 終了通知
	    public void onClose();
	}
}
