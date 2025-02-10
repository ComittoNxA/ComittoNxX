package src.comitton.imageview;

import java.util.EventListener;

public interface PageSelectListener extends EventListener {
	// ページ選択通知用
	public void onSelectPage(int page);
	// メニュー選択通知用
	public void onSelectPageSelectDialog(int menuId);
}
