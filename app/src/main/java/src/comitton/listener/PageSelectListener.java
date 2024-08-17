package src.comitton.listener;

import java.util.EventListener;

public interface PageSelectListener extends EventListener {
	// メニュー選択通知用
	public void onSelectPage(int menuId);
}
