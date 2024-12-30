package src.comitton.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import jp.dip.muracoro.comittonx.R;
import src.comitton.view.MenuItemView;

public class TabDialogFragment extends ImmersiveDialogFragment implements View.OnTouchListener {

    protected View mView;
    protected LinearLayout mHeader;
    protected TabLayout mTabLayout;
    protected ViewPager2 mViewPager;
    protected LinearLayout mFooter;

    private MenuDialog.MenuSelectListener mListener = null;
    private FragmentActivity mActivity;
    private Context mContext;

    protected ArrayList<String> mTitleArray = new ArrayList<String>(0);
    protected ArrayList<View> mViewArray = new ArrayList<View>(0);
    private DialogAdapter mAdapter;

    private boolean mTop;
    private boolean mHalfView;
    protected int mWidth;
    protected int mHeight;
    private float mScale;
    private boolean mSelected;
    private boolean mIsClose;

    private static int curcolor = 0x90008000;
    private static int title_txtcolor = 0xFFFFFFFF;
    private static int title_bakcolor = 0xA0000080;
    private int title_txtsize;
    private static int tab_txtcolor_selected = 0xFFFFFFFF;
    private static int tab_txtcolor = 0xBBFFFFFF;
    private static int tab_bakcolor = 0xA0000080;
    private static int item_txtcolor = 0xFFFFFFFF;
    private static int item_bakcolor = 0x00000000;
    private static int separater_txtcolor = 0xBBFFFFFF;
    private static int separater_bakcolor = 0xBBFFFFFF;
    private int item_txtsize;

    public TabDialogFragment(FragmentActivity activity, int cx, int cy, boolean isclose, MenuDialog.MenuSelectListener listener) {
        TabDialogFragmentProc(activity, cx, cy, isclose, false, false, false, listener);
    }

    public TabDialogFragment(FragmentActivity activity, int cx, int cy, boolean isclose, boolean halfview, MenuDialog.MenuSelectListener listener) {
        TabDialogFragmentProc(activity, cx, cy, isclose, halfview, false, false, listener);
    }

    public TabDialogFragment(FragmentActivity activity, int cx, int cy, boolean isclose, boolean halfview, boolean top, MenuDialog.MenuSelectListener listener) {
        TabDialogFragmentProc(activity, cx, cy, isclose, halfview, top, false, listener);
    }

    public TabDialogFragment(FragmentActivity activity, int cx, int cy, boolean isclose, boolean halfview, boolean top, boolean wide, MenuDialog.MenuSelectListener listener) {
        TabDialogFragmentProc(activity, cx, cy, isclose, halfview, top, wide, listener);
    }

    private void TabDialogFragmentProc(FragmentActivity activity, int cx, int cy, boolean isclose, boolean halfview, boolean top, boolean wide, MenuDialog.MenuSelectListener listener) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mScale = mContext.getResources().getDisplayMetrics().scaledDensity;

        mTop = top;
        mHalfView = halfview;

        if (mHalfView) {
            mWidth = Math.min(cx, cy) * 20 / 100;
        } else {
            mWidth = Math.min(cx, cy) * 80 / 100;
        }
        int maxWidth = (int) (20 * mScale * 16);
        if (!wide) {
            mWidth = Math.max(mWidth, maxWidth);
        }
        mHeight = cy * 80 / 100;

        mScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        title_txtsize = (int) (18 * mScale);
        item_txtsize = (int) (20 * mScale);

        mSelected = false;
        mIsClose = isclose;

        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boolean debug = false;
        Dialog dialog = getDialog();
        Window dlgWindow = dialog.getWindow();

        // タイトルなし
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Activityを暗くしない
        dlgWindow.setFlags(0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // ダイアログの背景を設定
        dlgWindow.setBackgroundDrawableResource(R.drawable.dialogframe_transparent);

        // ソフトウェアキーボードを隠す
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // 画面下に表示
        WindowManager.LayoutParams wmlp=dlgWindow.getAttributes();
        wmlp.gravity =(mTop ? Gravity.TOP : Gravity.CENTER) | (mHalfView ? Gravity.RIGHT : 0);
        dlgWindow.setAttributes(wmlp);

        dialog.getWindow().setLayout(mWidth, mHeight);

        ViewGroup.LayoutParams layoutParams;

        mView = inflater.inflate(R.layout.tabdialog, container);
        layoutParams = new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        mView.setLayoutParams(layoutParams);

        mHeader = (LinearLayout) mView.findViewById(R.id.header);
        mFooter = (LinearLayout) mView.findViewById(R.id.footer);

        mTabLayout = (TabLayout) mView.findViewById(R.id.tablayout);
        layoutParams = mTabLayout.getLayoutParams();
        layoutParams.width = mWidth;
        mTabLayout.setLayoutParams(layoutParams);
        mTabLayout.setBackgroundColor(tab_bakcolor);
        mTabLayout.setTabTextColors(tab_txtcolor, tab_txtcolor_selected);

        mViewPager = (ViewPager2) mView.findViewById(R.id.viewpager);
        layoutParams = mViewPager.getLayoutParams();
        layoutParams.width = mWidth;
        layoutParams.height = mHeight - mTabLayout.getHeight();
        mViewPager.setLayoutParams(layoutParams);

        mAdapter = new DialogAdapter(mActivity, this, mViewArray);
        mViewPager.setAdapter(mAdapter);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(mTitleArray.get(position))
        ).attach();

        return mView;
    }

    private MenuItemView mSelectView;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean debug = false;
        // タッチイベント
        int action = event.getAction();

        String eventName[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
                "POINTER_DOWN" , "POINTER_UP" , "HOVER_MOVE" , "SCROLL" , "HOVER_ENTER" ,
                "HOVER_EXIT" , "BUTTON_PRESS" , "BUTTON_RELEASE" };
        if (debug) {Log.d("TabDialogFragment", "onTouch: view=" + v + ", action=" + eventName[action]);}

        if (action == MotionEvent.ACTION_DOWN) {
            mSelectView = (MenuItemView)v;
            mSelectView.setSelect(true);
        }
        else if (action == MotionEvent.ACTION_CANCEL) {
            if (mSelectView != null) {
                mSelectView.setSelect(false);
                mSelectView = null;
            }
        }
        else if (action == MotionEvent.ACTION_UP) {
            if (mSelectView != null) {
                if (mSelected == false) {
                    mListener.onSelectMenuDialog(mSelectView.getMenuId());
                }
                mSelectView.setSelect(false);
                if (mIsClose) {
                    mSelectView = null;
                    mSelected = true;
                    this.dismiss();
                }
            }
        }
        return true;
    }

    public void addHeader(String text) {
        AppCompatTextView textView = new AppCompatTextView(mActivity);
        textView.setText(text);
        textView.setPadding(0,16,0,0);
        textView.setTextSize(20);
        textView.setTextColor(title_txtcolor);
        textView.setBackgroundColor(0x00000000);
        mHeader.setBackgroundColor(title_bakcolor);
        mHeader.addView(textView);
    }

    public void addFooter(View view) {
        mFooter.addView(view);
    }

    public void addSection(String text) {
        mTitleArray.add(text);

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(0x00000000);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        mViewArray.add(linearLayout);
    }

    public void addItem(View view) {
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(view);
    }

    public void addItem(int id, String text) {
        MenuItemView itemview = new MenuItemView(mContext, MenuItemView.TYPE_ITEM, MenuItemView.SUBTYPE_STRING, text, null, null, 0, id, item_txtsize, mWidth, item_txtcolor, item_bakcolor, curcolor);
        itemview.setOnTouchListener(this);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(itemview);
        MenuItemView sepview = new MenuItemView(mContext, mWidth, separater_txtcolor, separater_bakcolor);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(sepview);
    }

    public void addItem(int id, String text, String sub1) {
        MenuItemView itemview = new MenuItemView(mContext, MenuItemView.TYPE_ITEM, MenuItemView.SUBTYPE_STRING, text, sub1, null, 0, id, item_txtsize, mWidth, item_txtcolor, item_bakcolor, curcolor);
        itemview.setOnTouchListener(this);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(itemview);
        MenuItemView sepview = new MenuItemView(mContext, mWidth, separater_txtcolor, separater_bakcolor);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(sepview);
    }

    public void addItem(int id, String text, boolean flag) {
        MenuItemView itemview = new MenuItemView(mContext, MenuItemView.TYPE_ITEM, MenuItemView.SUBTYPE_CHECK, text, null, null, (flag ? 1 : 0), id, item_txtsize, mWidth, item_txtcolor, item_bakcolor, curcolor);
        itemview.setOnTouchListener(this);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(itemview);
        MenuItemView sepview = new MenuItemView(mContext, mWidth, separater_txtcolor, separater_bakcolor);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(sepview);
    }

    public void addItem(int id, String text, String sub1, String sub2, int index) {
        MenuItemView itemview = new MenuItemView(mContext, MenuItemView.TYPE_ITEM, MenuItemView.SUBTYPE_RADIO, text, sub1, sub2, index, id, item_txtsize, mWidth, item_txtcolor, item_bakcolor, curcolor);
        itemview.setOnTouchListener(this);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(itemview);
        MenuItemView sepview = new MenuItemView(mContext, mWidth, separater_txtcolor, separater_bakcolor);
        ((LinearLayout)mViewArray.get(mViewArray.size() - 1)).addView(sepview);
    }

    private class DialogAdapter extends FragmentStateAdapter {

        private final FragmentActivity mActivity;
        private final View.OnTouchListener mListener;
        private final ArrayList<View> mViewArray;
        private ArrayList<ScrollView> mScrollView = new ArrayList<ScrollView>(0);

        public DialogAdapter(FragmentActivity activity, View.OnTouchListener listener, ArrayList<View> viewArray) {
            super(activity);
            mActivity = activity;
            mListener = listener;
            mViewArray = viewArray;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            DialogFragment fragment = new DialogFragment(mActivity);
            ScrollView scrlView = fragment.getScrollView();
            scrlView.addView(mViewArray.get(position));
            mScrollView.add(scrlView);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return mViewArray.size();
        }
    }

    public static class DialogFragment extends Fragment {
        private ScrollView mScrollView;

        private static int scrl_bakcolor = 0x80000000;

        public DialogFragment(FragmentActivity activity) {
            mScrollView = new ScrollView(activity);
            mScrollView.setBackgroundColor(scrl_bakcolor);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return mScrollView;
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }

        public ScrollView getScrollView() {
            return mScrollView;
        }
    }
}
