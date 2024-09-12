package src.comitton.listener;

import java.util.EventListener;

public interface EpubWebViewListener extends EventListener {
	// 総ページ数変更通知用
	public void onChangeMaxpage(int maxpage, int page);
}
