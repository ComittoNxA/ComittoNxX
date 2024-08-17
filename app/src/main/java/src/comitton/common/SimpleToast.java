package src.comitton.common;

/*
非UIスレッドからToastを表示します。
このクラスのインスタンスを作成してから，Toast表示を要求するように使います。

SimpleToast mSimpleToast;

ｍContextが決定してからインスタンスを作成します。
mSimpleToast = new SimpleToast(mContext);

Toast表示を要求する方法は2つあります。
(1)ダイレクト　通常のToastと同じように使います。
続けて要求するとキューに入って，順に表示されます。
mSimpleToast.displaySimpleToast_direct("Hello!",Toast.LENGTH_LONG);

(2)排他的に表示するときに使います。
前の表示が残っていると表示要求は無視されます。
mSimpleToast.displaySimpleToast("Hello!",Toast.LENGTH_LONG);
キャンセルができるので，表示要求直前にキャンセル要求すれば，前の表示を消して，新しい表示を行います。
このキャンセルは，displaySimpleToastに対してだけ効果があります。
mSimpleToast.cancelSimpleToast();
 */

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import static android.os.Looper.getMainLooper;

public class SimpleToast {
    private final String TAG = "SimpleToast";

    private static Toast singletoast;
    private static boolean inhibittime = false;
    private Context mContext;
    private int mstrID;
    private String mstr;
    private int mduration;

    public SimpleToast(Context con){
        mContext = con;
        inhibittime = false;
    }

    public void displaySimpleToast_direct(int strID, int duration) {
        String str;
        str=mContext.getString(strID);
        displaySimpleToast_direct(str,duration);
    }

    public void displaySimpleToast_direct(String str, int duration) {
        mstr=str;
        mduration=duration;
        final Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, mstr, mduration).show();
            }
        });
    }

    public void displaySimpleToast(int strID, int duration) {
        String str;
        str=mContext.getString(strID);
        displaySimpleToast(str,duration);
    }

    public void displaySimpleToast(String str, int duration) {
        mstr=str;
        mduration=duration;
        final Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                displayToast(mstr,mduration);
            }
        });
    }

    public void cancelSimpleToast() {
        final Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelToast();
            }
        });
    }

    private void displayToast(String str, int duration) {
        if (!inhibittime) {
            inhibittime = true;
            singletoast = Toast.makeText(mContext, str, duration);
            singletoast.show();
            Log.i(TAG, "Toast displayed.");
            int inhibitlength=(duration== Toast.LENGTH_SHORT)?3000:5000;

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    inhibittime=false;
                    Log.i(TAG, "inhibittime is false now");
                }
            }, inhibitlength);
        }
    }

    private void cancelToast() {
        if (singletoast!=null) {
            singletoast.cancel();
            inhibittime=false;
        }

    }
}
