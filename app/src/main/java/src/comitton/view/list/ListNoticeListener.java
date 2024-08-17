package src.comitton.view.list;

import java.util.EventListener;

import android.graphics.Point;

public interface ListNoticeListener extends EventListener {
	// リスト選択通知
	public void onItemClick(int listtype, int index, Point point);
	// リスト長押し選択通知
	public void onItemLongClick(int listtype, int index);
	// スクロール変更通知
	public void onScrollChanged(int listtype, int firstindex, int lastindex);
	// リスト更新依頼
	public void onRequestUpdate(RecordListArea recordlist, int listtype);
}
