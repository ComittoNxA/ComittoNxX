package src.comitton.listener;

import java.util.EventListener;

public interface ChapterPageSelectListener extends EventListener {
	// メニュー選択通知用
	public void onSelectChapter(int menuId);
	public void onSelectPage(int menuId);
	public void onSelectPageRate(float menuId);
	public void onChapterSearch();
}
