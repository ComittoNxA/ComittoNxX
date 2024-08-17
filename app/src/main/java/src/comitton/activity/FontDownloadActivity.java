package src.comitton.activity;

import src.comitton.dialog.FontDownloadDialog;

import jp.dip.muracoro.comittonx.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FontDownloadActivity extends Activity {
	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.fontdownload);

	    mContext = this;

	    Button btnLicense = (Button)this.findViewById(R.id.btn_lic1);	// ライセンス表示ボタン
	    Button btnMincho = (Button)this.findViewById(R.id.btn_mincho);	// IPA明朝ダウンロード
	    Button btnGothic = (Button)this.findViewById(R.id.btn_gothic);	// IPAゴシックダウンロード

	    btnLicense.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ライセンス表示
				Resources res = getResources();
				String url = res.getString(R.string.url_fontlicense);	// ライセンス
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	    btnMincho.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// IPA明朝ダウンロード
				Resources res = getResources();
				String url = res.getString(R.string.url_fontdownload1);
				FontDownloadDialog dlg = new FontDownloadDialog(mContext, url, "IPA_MINCHO.ttf", 8046712);
				dlg.show();
			}
		});
	    btnGothic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// IPAゴシックダウンロード
				Resources res = getResources();
				String url = res.getString(R.string.url_fontdownload2);
				FontDownloadDialog dlg = new FontDownloadDialog(mContext, url, "IPA_GOTHIC.ttf", 6235344);
				dlg.show();
			}
		});
	    return;
	}
}
