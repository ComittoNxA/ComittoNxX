package src.comitton.fileview;

import java.util.EventListener;

public interface DrawNoticeListener extends EventListener {
	// 更新通知
	public void onUpdateArea(short areatype, boolean isRealtime);
}
