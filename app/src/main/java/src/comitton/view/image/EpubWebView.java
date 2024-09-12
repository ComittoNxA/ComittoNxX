package src.comitton.view.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubReader;
import src.comitton.activity.EpubActivity;
import src.comitton.common.DEF;
import src.comitton.listener.ChapterPageSelectListener;
import src.comitton.listener.EpubWebViewListener;

public class EpubWebView extends WebView {

    private static final Logger log = LoggerFactory.getLogger(EpubWebView.class);
    private Context mContext = null;
    private Handler mHandler = null;
    private EpubView mEpubView = null;
    private EpubWebViewListener mListener = null;

    private int mTextColor;
    private int mFontSize;
    private String mFontFile;
    private boolean mEffect;

    private EpubReader mEpubReader = null;
    private Book mEpubBook;
    private String mTitle;
    private String mChapterTitle;
    private List<Resource> mChapterResources;
    private Resource mChapterResource;

    private boolean mChapterLoaded = false;
    private boolean mVisible = false;
    private boolean mVertical = false;
    private float mDencity;
    private float mWidth;
    private float mHeight;
    private float mPrevWidth;
    private float mPrevHeight;
    private int mContentWidth;
    private int mContentHeight;

    private int mChapterCount = -1;
    private int mCurrentChapter = -1;
    private float mPageRate = -1;
    private int mPageCount = -1;
    private int mCurrentPage = -1;

    public EpubWebView(@NonNull Context context) {
        super(context, null);
    }

    public EpubWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d("EpubWebView", "コンストラクタ: 開始します.");

        mContext = context;

        // 非表示にする
        mChapterLoaded = false;
        mVisible = false;
        setVisibility(View.INVISIBLE);
        // JavaScriptを有効にする
        getSettings().setJavaScriptEnabled(true);
        // デフォルトEncodingはUTF-8
        getSettings().setDefaultTextEncodingName("UTF-8");
        // User-Agentを設定
        getSettings().setUserAgentString("ComittoNxX");
        // 背景色を設定する
        setBackgroundColor(Color.TRANSPARENT);

        WebSettings settings = getSettings();
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);

        // Densityに合わせてスケーリング
        mDencity = mContext.getResources().getDisplayMetrics().density;
        setInitialScale((int)mDencity * 100);

        // PCのchromeブラウザに接続して画面のソースを検証
        // 接続先： chrome://inspect/#devices
        // 使い方： https://zenn.dev/aldagram_tech/articles/bbb12025b9747d
        if (DEF.DEBUG) setWebContentsDebuggingEnabled(true);


        try {
            // WebViewClientの設定
            setWebViewClient(new WebViewClient() {

                // ページの遷移時の処理を記述
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.d("EpubWebView", "shouldOverrideUrlLoading: 開始します. url=" + url);
                    // 別のActivityやアプリを起動する場合はtrueを返す
                    // WebView内で表示する場合はfalseを返す
                    return false;
                }

                // Webリソースの読み込み時の処理を記述
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    Uri url = request.getUrl();
                    Log.d("EpubWebView", "shouldInterceptRequest: 開始します. request.url=" + url);

                    String href = url.toString().replace("file:///", "");
                    Resource content = mEpubBook.getResources().getByHref(href);
                    if (content == null) {
                        Log.d("EpubWebView", "shouldInterceptRequest: リソースが見つかりませんでした. href=" + href);
                        // WebViewに任せる
                        return super.shouldInterceptRequest(view, request);
                    }
                    Log.d("EpubWebView", "shouldInterceptRequest: リソースが見つかりました. href=" + content.getHref());

                    InputStream is = null;
                    try {
                        Log.d("EpubWebView", "shouldInterceptRequest: リソースの内容を返却します. mimeType=" + content.getMediaType().toString() + ", encoding=" + content.getInputEncoding());
                        return new WebResourceResponse(content.getMediaType().toString(), content.getInputEncoding(), content.getInputStream());
                    } catch (IOException e) {
                        Log.e("EpubWebView", "shouldInterceptRequest: エラーが発生しました.");
                        if (e != null && e.getMessage() != null) {
                            Log.e("EpubWebView", "shouldInterceptRequest: エラーメッセージ. " + e.getMessage());
                        }
                    }
                    Log.d("EpubWebView", "shouldInterceptRequest: 正常に終了しませんでした.");
                    // WebViewに任せる
                    return super.shouldInterceptRequest(view, request);
                }

                // Webリソースの読み込み時
                @Override
                public void onLoadResource(WebView view, String url) {
                    //Log.d("EpubWebView", "onLoadResource: 開始します. url=" + url);
                    super.onLoadResource(view, url);
                }

                // ページ読み込み開始時
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    //Log.d("EpubWebView", "onPageStarted: 開始します. url=" + url);
                    super.onPageStarted(view, url, favicon);
                }

                // ページ読み込み完了時
                @Override
                public void onPageFinished(WebView view, String url) {
                    //Log.d("EpubWebView", "onPageFinished: 開始します. url=" + url);
                    super.onPageFinished(view, url);
                    injectJavascript();
                }

                // アクセスエラーなどのWebリソースの読み込みエラー発生時の処理
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    Uri url = request.getUrl();
                    Log.e("EpubWebView", "onReceivedError: 開始します. url=" + url);
                    super.onReceivedError(view, request, error);
                }

                // SSLエラー時
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    Log.e("EpubWebView", "onReceivedSslError: 開始します. error=" + error);
                    super.onReceivedSslError(view, handler, error);
                    handler.cancel();
                }
            });

            setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    // Javascript 内で return した内容を受け取る
                    Log.d("EpubWebView", "onJsAlert: 開始します. url=" + url + ", message=" + message + ", result=" + result);

                    result.confirm();

                    // 終了通知
                    Message msg = new Message();
                    msg.what = ((EpubActivity)mContext).MSG_LOAD_END;
                    mHandler.sendMessage(msg);

                    // 'isVertical='+isVertical+
                    // ',width='+document.documentElement.clientWidth+
                    // ',height='+document.documentElement.clientHeight;\n" +

                    if (message.length() > 0) {

                        try {
                            String[] str = message.split(",");
                            for (int i = 0; i < str.length; i++) {
                                str[i] = str[i].trim();
                                if (str[i].startsWith("isVertical=")) {
                                    str[i] = str[i].substring(11);
                                    Log.d("EpubWebView", "onJsAlert: str[i]=" + str[i]);
                                    if (str[i].equals("true") || str[i].equals("false")) {
                                        mVertical = Boolean.valueOf(str[i]);
                                    }
                                } else if (str[i].startsWith("width=")) {
                                    str[i] = str[i].substring(6);
                                    mWidth = Integer.parseInt(str[i]) * mDencity;
                                } else if (str[i].startsWith("height=")) {
                                    str[i] = str[i].substring(7);
                                    mHeight = Integer.parseInt(str[i]) * mDencity;
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.e("EpubWebView", "onJsAlert: エラーが発生しました.");
                            if (e != null && e.getMessage() != null) {
                                Log.e("EpubWebView", "onJsAlert: エラーメッセージ. " + e.getMessage());
                            }
                        }
                        Log.d("EpubWebView", "onJsAlert: mVertical=" + mVertical + ", mWidth=" + mWidth + ", mHeight=" + mHeight);

                    }

                    mChapterLoaded = true;
                    Log.d("EpubWebView", "onJsAlert: getContentWidth()=" + getContentWidth() + ", getWidth()=" + getWidth() + ", pageCount=" + Math.round((double)getContentWidth() / getWidth()));
                    Log.d("EpubWebView", "onJsAlert: getContentWidth()=" + getContentWidth() + ", mWidth()=" + mWidth + ", pageCount=" + Math.round((double)getContentWidth() / mWidth));
                    Log.d("EpubWebView", "onJsAlert: getContentHeight()=" + getContentHeight() + ", getHeight()=" + getHeight() + ", pageCount=" + Math.round((double)getContentHeight() / getHeight()));
                    Log.d("EpubWebView", "onJsAlert: getContentHeight()=" + getContentHeight() + ", mHeight()=" + mHeight + ", pageCount=" + Math.round((double)getContentHeight() / mHeight));
                    return true;
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage cm) {
                    // Javascript 内で console.log で出力した内容を受け取る
                    Log.d("EpubWebView", "onConsoleMessage: " + cm.message() + " -- line=" + cm.lineNumber() + ", source=" + cm.sourceId());
                    return true;
                }
            });

        } catch (Exception e) {
            Log.e("EpubWebView", "onConsoleMessage: エラーが発生しました.");
            if (e != null && e.getMessage() != null) {
                Log.e("EpubWebView", "onConsoleMessage: エラーメッセージ. " + e.getMessage());
            }
        }
    }

    @Override
    public void invalidate() {
        Log.d("EpubWebView", "invalidate: 開始します.");
        super.invalidate();

        if (mVertical && (getContentHeight() != mContentHeight || mPrevHeight != mHeight)) {
            mContentHeight = getContentHeight();
            mPrevHeight = mHeight;
            if (mHeight != 0f) {
                mPageCount = (int) Math.round((float) mContentHeight / mHeight);
            }
            Log.d("EpubWebView", "invalidate: mContentHeight=" + mContentHeight + ", mHeight=" + mHeight);
            Log.d("EpubWebView", "invalidate: (mContentHeight / mHeight)=" + ((double) mContentHeight / mHeight) + ", pageCount=" + mPageCount);
        }
        if (!mVertical && (getContentWidth() != mContentWidth || mPrevWidth != mWidth)) {
            mContentWidth = getContentWidth();
            mPrevWidth = mWidth;
            if (mWidth != 0f) {
                mPageCount = (int) Math.round((float) mContentWidth / mWidth);
            }
            Log.d("EpubWebView", "invalidate: mContentWidth=" + mContentWidth + ", mWidth=" + mWidth);
            Log.d("EpubWebView", "invalidate: (mContentWidth / mWidth)=" + ((double) mContentWidth / mWidth) + ", pageCount=" + mPageCount);
        }

        mCurrentPage = (int) Math.round(mPageCount * mPageRate);
        if (mCurrentPage > mPageCount - 1) {
            mCurrentPage = mPageCount - 1;
        }
        Log.d("EpubWebView", "invalidate: mPageRate=" + mPageRate + ", (mPageCount * mPageRate)=" + (mPageCount * mPageRate) + ", mCurrentPage=" + mCurrentPage);

        mListener.onChangeMaxpage(mPageCount, mCurrentPage);
        mEpubView.loadPage();

        if (mChapterLoaded && !mVisible) {
            mVisible = true;
            // 表示する
            setVisibility(View.VISIBLE);
        }
    }

    public void setStream(InputStream inputStream) {
        Log.d("EpubWebView", "setStream: 開始します.");

        try {
            mEpubReader = new EpubReader();
            mEpubBook = mEpubReader.readEpub(inputStream);

            mChapterResources = mEpubBook.getContents();
            mChapterCount = mChapterResources.size();

            mTitle = mEpubBook.getTitle();
            setTitle(mTitle);
            mEpubView.loadPage();

        } catch (Exception e) {
            Log.e("EpubWebView", "setStream: エラーが発生しました.");
            if (e != null && e.getMessage() != null) {
                Log.e("EpubWebView", "setStream: エラーメッセージ. " + e.getMessage());
            }
        }
        Log.d("EpubWebView", "setStream: 終了します.");
    }

    public void setTitle(String title) {
        Log.d("EpubWebView", "setTitle: 開始します. title=" + title);
        mTitle = title;
    }


    // 余白色を設定
    public boolean setConfig(int textColor, int fontSize, String fontFile, boolean effect) {
        Log.d("EpubWebView", "setConfig: 開始します. fontSize=" + fontSize + ", fontFile=" + fontFile);
        boolean result = true;

        mTextColor = textColor;
        mFontSize = fontSize;
        mFontFile = fontFile;
        mEffect = effect;

        runJavascript();

        return result;
    }

    private void injectJavascript() {
        Log.d("EpubWebView", "injectJavascript: 開始します.");

        String font = "storage/emulated/0/comittona/font/HGRME.TTC";

        String js = "function comitton(textColor, fontSize, fontFile){\n" +
                "    var html = document.html;\n" +
                "    var head = document.head;\n" +
                "    var body = document.body;\n" +

                // フォントの色を設定
                "    body.style.color = textColor;\n" +
                // フォントの基準サイズを設定
                "    body.style.fontSize = fontSize+'px';\n" +

                // 文章の方向と縦書きか横書きかを取得
                "    var isVertical = false;\n" +
                "    let {direction, \"writing-mode\": writing_mode} = getComputedStyle(document.body);" +
                "    if (writing_mode.startsWith('vertical-')) { isVertical = true }" +
                "    console.log('文章の方向と縦書き横書き : direction=' + direction + ', writing_mode=' + writing_mode + ', isVertical=' + isVertical);" +

                // デバイスの大きさに合わせてズーム倍率を設定
                "    var meta = document.createElement('meta');\n" +
                "    meta.setAttribute('name','viewport');\n" +
                "    meta.setAttribute('content','width=device-width, initial-scale=1.0');\n" +
                "    head.appendChild(meta);\n" +

                // スクロール可能な全画面サイズを設定
                "    if (isVertical) {" +
                "        body.style.width = '100vw';\n" +
                "        body.style.height = 'auto';\n" +
                "    } else {" +
                "        body.style.width = 'auto';\n" +
                "        body.style.height = '100vh';\n" +
                "    }" +
                "    body.style.margin = '0';\n" +

                // 段組みの設定(1ページに相当)
                "    body.style.columnWidth = '100vw';\n" +
                "    body.style.columnHeight = '100vh';\n" +
                "    body.style.columnGap = '0';\n" +
                "    body.style.columnFill = 'auto';\n" +
                "    body.style.columnCount = 'auto';\n" +

                // ルビが段組みの境界で分割されるのを防ぐ
                "    body.style.lineHeight = '2';\n" +
                // line-stacking-ruby は段組みには効かない模様
                "    body.style.lineStackingRuby = 'include-ruby';\n" +

                "    console.log('lineHeight=' + body.style.lineHeight +', lineStackingRuby='+ body.style.lineStackingRuby);\n" +
                "    console.log('textHeight=' + body.style.textHeight +', lineStackingStrategy='+ body.style.lineStackingStrategy);\n" +

                // ページにフォントを登録
                "    if ('' != fontFile) {\n" +
                "        var font = new FontFace('MyFace', 'url(file:///'+fontFile+')');\n" +
                "        font.load();\n" +
                "        document.fonts.add(font);\n" +
                "    }\n" +

                // すべての要素に実行
                "    var elements = document.querySelectorAll('*');\n" +
                "    elements.forEach( function( element ){\n" +
                // 段組みの高さがずれるのを抑制
                "        element.style.boxSizing = 'border-box';\n" +
                // Fontを設定
                "        if ('' != fontFile) {\n" +
                "            element.style.fontFamily = 'MyFace';\n" +
                "        }\n" +
                "    });\n" +

                // 画像の要素に実行
                "    var images = document.getElementsByTagName('img');\n" +
                "    for(let image of images) {\n" +
                // 幅の最大値をページの幅までとする
                "        image.style.maxWidth = '100vw';\n" +
                "        image.style.maxHeight = '100vh';\n" +
                "    };\n" +

                "    window.resizeTo(window.outerWidth, window.outerHeight);" +

        // 取得可能な高さや幅をログ出力する
                "    console.log('幅 : screen.width=' + screen.width +', screen.availWidth='+ screen.availWidth);\n" +
                "    console.log('幅 : window.outerWidth=' + window.outerWidth  + ', window.innerWidth=' + window.innerWidth);\n" +
                "    console.log('幅 : body.offsetWidth=' + body.offsetWidth + ', document.documentElement.clientWidth=' + document.documentElement.clientWidth);\n" +
                "    console.log('幅 : document.body.scrollWidth=' + document.body.scrollWidth + ', document.documentElement.scrollWidth=' + document.documentElement.scrollWidth);\n" +
                "    console.log('高さ : screen.height=' + screen.height +', screen.availHeight='+ screen.availHeight);\n" +
                "    console.log('高さ : window.outerHeight=' + window.outerHeight  + ', window.innerHeight=' + window.innerHeight);\n" +
                "    console.log('高さ : body.offsetHeight=' + body.offsetHeight + ', document.documentElement.clientHeight=' + document.documentElement.clientHeight);\n" +
                "    console.log('高さ : document.body.scrollHeight=' + document.body.scrollHeight + ', document.documentElement.scrollHeight=' + document.documentElement.scrollHeight);\n" +

                "    return 'isVertical='+isVertical+',width='+document.documentElement.clientWidth+',height='+document.documentElement.clientHeight;\n" +
                "}";

        // 第二引数はコールバック
        evaluateJavascript("javascript:" + js, null);
        runJavascript();
    }

    private void runJavascript() {
        Log.d("EpubWebView", "runJavascript: 開始します.");

        evaluateJavascript("javascript:alert(comitton('#" + Integer.toHexString(mTextColor).substring(2) + "', '" + mFontSize + "', '"+ mFontFile +"'))", null);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getChapterTitle(int chapter) {
        Log.d("EpubView", "getChapterTitle: 開始します.");
        String text = "";
        try {
            text = new String(mChapterResources.get(chapter).getData());
            if (text == null) {
                return "";
            }
            int pattern = Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES;
            text = Pattern.compile("^.*?<title.*?>", pattern).matcher(text).replaceAll("");
            text = Pattern.compile("</title>.*$", pattern).matcher(text).replaceAll("");
            return text;
        }
        catch (Exception e) {
            Log.e("EpubWebView", "getChapterTitle: エラーが発生しました.");
            if (e != null && e.getMessage() != null) {
                Log.e("EpubWebView", "getChapterTitle: エラーメッセージ. " + e.getMessage());
            }
        }
        return text;
    }

    public String getChapterText(int chapter) {
        boolean debug = true;
        try {
            String text = new String(mChapterResources.get(chapter).getData());
            if (text == null) {
                text = "";
            }
            else {
                int pattern = Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES;
                text = Pattern.compile("\r", pattern).matcher(text).replaceAll("\n");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.1 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("^.*?<body.*?>", pattern).matcher(text).replaceAll("");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.2 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("<img.*?alt=[\"']([\"']+?)[\"'].*?>", pattern).matcher(text).replaceAll("<>$1<>");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.3 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("<(img|image).*?>", pattern).matcher(text).replaceAll("<>image<>");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.4 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("<rt>.*?</rt>", pattern).matcher(text).replaceAll("");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.5 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("<rb>(.*?)</rb>", pattern).matcher(text).replaceAll("$1");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.6 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("<ruby>|</ruby>", pattern).matcher(text).replaceAll("");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.7 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("<.*?>", pattern).matcher(text).replaceAll("\n");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.8 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("[\n\s]+", pattern).matcher(text).replaceAll("\n");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.9 text=" + text.substring(0, Math.min(text.length(), 500)));
                text = Pattern.compile("^\n", pattern).matcher(text).replaceAll("");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.10 text=" + text.substring(0, Math.min(text.length(), 500)));
                String[] texts = text.split("\n");
                text = texts[0];
                if (text.equals("image")) {
                    for (int i = 0; i < texts.length; i++) {
                        if (texts[i].length() != 0 && !texts[i].equals("image")) {
                            text = texts[i];
                            break;
                        }
                    }
                }
                //text = Pattern.compile("\n.*$", pattern).matcher(text).replaceAll("");
                if (debug) Log.d("EpubView", "getChapterText: テキストを解析します.11 text=" + text.substring(0, Math.min(text.length(), 500)));
            }
            return text;
        }
        catch (Exception e) {
            Log.e("EpubWebView", "getChapterText: エラーが発生しました.");
            if (e != null && e.getMessage() != null) {
                Log.e("EpubWebView", "getChapterText: エラーメッセージ. " + e.getMessage());
            }
        }
        return "";
    }

    public void setChapter(int chapter) {
        Log.d("EpubWebView", "setChapter: 開始します. chapter=" + chapter);
        if (chapter == -2 || chapter >= mChapterCount) {
            mCurrentChapter = mChapterCount - 1;
        }
        else if (chapter < 0) {
            mCurrentChapter = 0;
        }
        else {
            mCurrentChapter = chapter;
        }

        if (mChapterResources != null) {
            mChapterResource = mChapterResources.get(mCurrentChapter);

            try {
                mChapterTitle = mChapterResource.getTitle();
                if (mChapterTitle == null) {
                    mChapterTitle = "";
                }
                String mimeType = mChapterResource.getMediaType().toString();
                String epubHtml = new String(mChapterResource.getData());
                String baseUrl = "file:///" + mChapterResource.getHref();
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);

                ((EpubActivity) mContext).startDialogTimer(100);

                int pattern = Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES;
                epubHtml = Pattern.compile("<script[^>]*></script>", pattern).matcher(epubHtml).replaceAll("");
                epubHtml = Pattern.compile("<script[^>]*>", pattern).matcher(epubHtml).replaceAll("");

                // 非表示にする
                mChapterLoaded = false;
                mVisible = false;
                setVisibility(View.INVISIBLE);

                loadDataWithBaseURL(baseUrl, epubHtml, "text/html", "UTF-8", null);
            } catch (Exception e) {
                Log.e("EpubWebView", "setChapter: エラーが発生しました.");
                if (e != null && e.getMessage() != null) {
                    Log.e("EpubWebView", "setChapter: エラーメッセージ. " + e.getMessage());
                }
            }
        }
    }

    public int getChapterCount() {
        return mChapterCount;
    }

    public int getChapter() {
        return mCurrentChapter;
    }

    private void initialize() {
        Log.d("EpubWebView", "initialize: 開始します.");
        mChapterLoaded = true;
    }

    public void setView(Handler handler, EpubView epubView) {
        Log.d("EpubWebView", "setView: 開始します.");
        mHandler = handler;
        mEpubView = epubView;
    }

    public int getContentWidth() {
        return computeHorizontalScrollRange();
    }

    public int getContentHeight() {
        return computeVerticalScrollRange();
    }

    public void setPageRate(float pageRate) {
        Log.d("EpubWebView", "setPageRate: 開始します. pageRate=" + pageRate);
        if (pageRate == -2f || pageRate > 1f) {
            mPageRate = 1f;
        }
        else if (pageRate < 0f) {
            mPageRate = 0f;
        }
        else {
            mPageRate = pageRate;
        }
        invalidate();
    }

    private void setPageRate() {
        mPageRate = (float) mCurrentPage / mPageCount;
        if (mPageRate > 1f) {
            mPageRate = 1f;
        }
        else if (mPageRate < 0f) {
            mPageRate = 0f;
        }
    }

    public float getPageRate() {
        return mPageRate;
    }

    public void setPage(int page) {
        mCurrentPage = page;
        setPageRate();
        loadPage();
    }

    public int getPage() {
        return mCurrentPage;
    }

    public int getPageCount() {
        return mPageCount;
    }

    public boolean nextPage() {
        Log.d("EpubWebView", "nextPage: 開始します.");
        if (mCurrentPage >= mPageCount - 1) {
            // 最終ページ
            if (mCurrentChapter >= mChapterCount -1) {
                // 最終セクション
                return false;
            }
            else {
                setChapter(mCurrentChapter + 1);
                setPageRate(0f);

                if (mEffect) {
                    TranslateAnimation ta = new TranslateAnimation(-getWidth(), 0, 0, 0);
                    ta.setDuration(500);
                    ta.setFillAfter(true);
                    startAnimation(ta);
                }

                return true;
            }
        }
        mCurrentPage++;
        setPageRate();

        if (mEffect) {
            TranslateAnimation ta = new TranslateAnimation(-getWidth(), 0, 0, 0);
            ta.setDuration(500);
            ta.setFillAfter(true);
            startAnimation(ta);
        }

        loadPage();
        return true;
    }

    public boolean prevPage() {
        Log.d("EpubWebView", "prevPage: 開始します.");
        if (mCurrentPage <= 0) {
            // 最初のページ
            if (mCurrentChapter <= 0) {
                // 最初のセクション
                return false;
            }
            else {
                setChapter(mCurrentChapter - 1);
                setPageRate(1f);

                if (mEffect) {
                    TranslateAnimation ta = new TranslateAnimation(getWidth(), 0, 0, 0);
                    ta.setDuration(500);
                    ta.setFillAfter(true);
                    startAnimation(ta);
                }

                return true;
            }
        }
        mCurrentPage--;
        setPageRate();

        if (mEffect) {
            TranslateAnimation ta = new TranslateAnimation(getWidth(), 0, 0, 0);
            ta.setDuration(500);
            ta.setFillAfter(true);
            startAnimation(ta);
        }

        loadPage();
        return true;
    }

    public void loadPage() {
        Log.d("EpubWebView", "loadPage: 開始します.");
        String js;
        if (mVertical) {
            scrollTo(0, (int) (mCurrentPage * mHeight));
        }
        else {
            scrollTo((int) (mCurrentPage * mWidth), 0);
        }
        mEpubView.updateGuide();

    }

    public void setEpubWebViewListenar(EpubWebViewListener listener) {
        mListener = listener;
    }
}
