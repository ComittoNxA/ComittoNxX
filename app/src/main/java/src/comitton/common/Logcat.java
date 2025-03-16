package src.comitton.common;

import android.util.Log;
import androidx.annotation.Nullable;

public class Logcat {

    public static final int LOG_LEVEL_VERBOSE = 1;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_WARN = 4;
    public static final int LOG_LEVEL_ERROR = 5;
    public static final int LOG_LEVEL_NONE = 6;

    public static int global_log_level = LOG_LEVEL_NONE;

    /**
     * {@code Exception} に入れる文字列を返す<br>
     * @param msg ログメッセージ
     * @return クラス名: メソッド名: {@code msg} (ファイル名:行番号) エラー名: エラーメッセージ
     */
    public static String msg(@Nullable String msg, @Nullable Throwable e) {
        Throwable throwable = new Throwable("msg");
        StackTraceElement[] ste = throwable.getStackTrace();
        String[] className = ste[1].getClassName().split("\\.");
        String tag = className[className.length - 1];
        String message = tag + ": " + ste[1].getMethodName() + ": " + msg + " (" + ste[1].getFileName() + ":" + ste[1].getLineNumber() + ") " + e;
        return message;
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(@Nullable String msg) {
        verbose(LOG_LEVEL_NONE, true, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(@Nullable String msg, @Nullable Throwable tr) {
        verbose(LOG_LEVEL_NONE, true, msg, tr,false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(@Nullable String msg, boolean stackTrace) {
        verbose(LOG_LEVEL_NONE, true, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(@Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        verbose(LOG_LEVEL_NONE, true, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(boolean enable, @Nullable String msg) {
        verbose(LOG_LEVEL_NONE, enable, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(boolean enable, @Nullable String msg, @Nullable Throwable tr) {
        verbose(LOG_LEVEL_NONE, enable, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(boolean enable, @Nullable String msg, boolean stackTrace) {
        verbose(LOG_LEVEL_NONE, enable, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        verbose(LOG_LEVEL_NONE, enable, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(int logLevel, @Nullable String msg) {
        verbose(logLevel, false, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(int logLevel, @Nullable String msg, @Nullable Throwable tr) {
        verbose(logLevel, false, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(int logLevel, @Nullable String msg, boolean stackTrace) {
        verbose(logLevel, false, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void v(int logLevel, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        verbose(logLevel, false, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    private static void verbose(int logLevel, boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (global_log_level <= LOG_LEVEL_VERBOSE || (global_log_level == LOG_LEVEL_NONE && (enable || logLevel <= LOG_LEVEL_VERBOSE))) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("verbose");
            StackTraceElement[] ste = throwable.getStackTrace();
            String[] className = ste[2].getClassName().split("\\.");
            String tag = className[className.length - 1];
            String message = ste[2].getMethodName() + ": " + msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

            if (!stackTrace) {
                if (tr != null) {
                    message += "\n" + tr;
                    Log.v(tag, message);
                }
                else {
                    Log.v(tag, message);
                }
            }
            else {
                if (tr != null) {
                    Log.v(tag, message, tr);
                }
                else {
                    message += "\nStackTrace:";
                    for (int i = 3; i < ste.length; i++) {
                        message += "\n    at " + ste[i];
                    }
                    Log.v(tag, message);
                }
            }
        }
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(@Nullable String msg) {
        debug(LOG_LEVEL_NONE, true, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(@Nullable String msg, @Nullable Throwable tr) {
        debug(LOG_LEVEL_NONE, true, msg, tr,false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(@Nullable String msg, boolean stackTrace) {
        debug(LOG_LEVEL_NONE, true, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(@Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        debug(LOG_LEVEL_NONE, true, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(boolean enable, @Nullable String msg) {
        debug(LOG_LEVEL_NONE, enable, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(boolean enable, @Nullable String msg, @Nullable Throwable tr) {
        debug(LOG_LEVEL_NONE, enable, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(boolean enable, @Nullable String msg, boolean stackTrace) {
        debug(LOG_LEVEL_NONE, enable, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        debug(LOG_LEVEL_NONE, enable, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(int logLevel, @Nullable String msg) {
        debug(logLevel, false, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(int logLevel, @Nullable String msg, @Nullable Throwable tr) {
        debug(logLevel, false, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(int logLevel, @Nullable String msg, boolean stackTrace) {
        debug(logLevel, false, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void d(int logLevel, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        debug(logLevel, false, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    private static void debug(int logLevel, boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (global_log_level <= LOG_LEVEL_DEBUG || (global_log_level == LOG_LEVEL_NONE && (enable || logLevel <= LOG_LEVEL_DEBUG))) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("debug");
            StackTraceElement[] ste = throwable.getStackTrace();
            String[] className = ste[2].getClassName().split("\\.");
            String tag = className[className.length - 1];
            String message = ste[2].getMethodName() + ": " + msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

            if (!stackTrace) {
                if (tr != null) {
                    message += "\n" + tr;
                    Log.d(tag, message);
                }
                else {
                    Log.d(tag, message);
                }
            }
            else {
                if (tr != null) {
                    Log.d(tag, message, tr);
                }
                else {
                    message += "\nStackTrace:";
                    for (int i = 3; i < ste.length; i++) {
                        message += "\n    at " + ste[i];
                    }
                    Log.d(tag, message);
                }
            }
        }
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(@Nullable String msg) {
        info(LOG_LEVEL_NONE, true, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(@Nullable String msg, @Nullable Throwable tr) {
        info(LOG_LEVEL_NONE, true, msg, tr,false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(@Nullable String msg, boolean stackTrace) {
        info(LOG_LEVEL_NONE, true, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(@Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        info(LOG_LEVEL_NONE, true, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(boolean enable, @Nullable String msg) {
        info(LOG_LEVEL_NONE, enable, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(boolean enable, @Nullable String msg, @Nullable Throwable tr) {
        info(LOG_LEVEL_NONE, enable, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(boolean enable, @Nullable String msg, boolean stackTrace) {
        info(LOG_LEVEL_NONE, enable, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        info(LOG_LEVEL_NONE, enable, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(int logLevel, @Nullable String msg) {
        info(logLevel, false, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(int logLevel, @Nullable String msg, @Nullable Throwable tr) {
        info(logLevel, false, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(int logLevel, @Nullable String msg, boolean stackTrace) {
        info(logLevel, false, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void i(int logLevel, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        info(logLevel, false, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    private static void info(int logLevel, boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (global_log_level <= LOG_LEVEL_INFO || (global_log_level == LOG_LEVEL_NONE && (enable || logLevel <= LOG_LEVEL_INFO))) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("debug");
            StackTraceElement[] ste = throwable.getStackTrace();
            String[] className = ste[2].getClassName().split("\\.");
            String tag = className[className.length - 1];
            String message = ste[2].getMethodName() + ": " + msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

            if (!stackTrace) {
                if (tr != null) {
                    message += "\n" + tr;
                    Log.i(tag, message);
                }
                else {
                    Log.i(tag, message);
                }
            }
            else {
                if (tr != null) {
                    Log.i(tag, message, tr);
                }
                else {
                    message += "\nStackTrace:";
                    for (int i = 3; i < ste.length; i++) {
                        message += "\n    at " + ste[i];
                    }
                    Log.i(tag, message);
                }
            }
        }
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(@Nullable String msg) {
        warn(LOG_LEVEL_NONE, true, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(@Nullable String msg, @Nullable Throwable tr) {
        warn(LOG_LEVEL_NONE, true, msg, tr,false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(@Nullable String msg, boolean stackTrace) {
        warn(LOG_LEVEL_NONE, true, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(@Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        warn(LOG_LEVEL_NONE, true, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(boolean enable, @Nullable String msg) {
        warn(LOG_LEVEL_NONE, enable, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(boolean enable, @Nullable String msg, @Nullable Throwable tr) {
        warn(LOG_LEVEL_NONE, enable, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(boolean enable, @Nullable String msg, boolean stackTrace) {
        warn(LOG_LEVEL_NONE, enable, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        warn(LOG_LEVEL_NONE, enable, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(int logLevel, @Nullable String msg) {
        warn(logLevel, false, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(int logLevel, @Nullable String msg, @Nullable Throwable tr) {
        warn(logLevel, false, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(int logLevel, @Nullable String msg, boolean stackTrace) {
        warn(logLevel, false, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void w(int logLevel, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        warn(logLevel, false, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    private static void warn(int logLevel, boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (global_log_level <= LOG_LEVEL_WARN || (global_log_level == LOG_LEVEL_NONE && (enable || logLevel <= LOG_LEVEL_WARN))) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("debug");
            StackTraceElement[] ste = throwable.getStackTrace();
            String[] className = ste[2].getClassName().split("\\.");
            String tag = className[className.length - 1];
            String message = ste[2].getMethodName() + ": " + msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

            if (!stackTrace) {
                if (tr != null) {
                    message += "\n" + tr;
                    Log.w(tag, message);
                }
                else {
                    Log.w(tag, message);
                }
            }
            else {
                if (tr != null) {
                    Log.w(tag, message, tr);
                }
                else {
                    message += "\nStackTrace:";
                    for (int i = 3; i < ste.length; i++) {
                        message += "\n    at " + ste[i];
                    }
                    Log.w(tag, message);
                }
            }
        }
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(@Nullable String msg) {
        error(LOG_LEVEL_NONE, true, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(@Nullable String msg, @Nullable Throwable tr) {
        error(LOG_LEVEL_NONE, true, msg, tr,false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(@Nullable String msg, boolean stackTrace) {
        error(LOG_LEVEL_NONE, true, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(@Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        error(LOG_LEVEL_NONE, true, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(boolean enable, @Nullable String msg) {
        error(LOG_LEVEL_NONE, enable, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(boolean enable, @Nullable String msg, @Nullable Throwable tr) {
        error(LOG_LEVEL_NONE, enable, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(boolean enable, @Nullable String msg, boolean stackTrace) {
        error(LOG_LEVEL_NONE, enable, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        error(LOG_LEVEL_NONE, enable, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(int logLevel, @Nullable String msg) {
        error(logLevel, false, msg, null, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(int logLevel, @Nullable String msg, @Nullable Throwable tr) {
        error(logLevel, false, msg, tr, false);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(int logLevel, @Nullable String msg, boolean stackTrace) {
        error(logLevel, false, msg, null, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    public static void e(int logLevel, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        error(logLevel, false, msg, tr, stackTrace);
    }

    /**
     * Logcat にログを出力する<br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_VERBOSE} から {@code LOG_LEVEL_ERROR} までの値のとき、
     * {@code global_log_level} より重要度が高いログを出力する.<br><br>
     * クラス変数 {@code int global_log_level} が {@code LOG_LEVEL_NONE} のとき、
     * 引数 {@code int logLevel} より重要度が高いログまたは 引数 {@code boolean enable} が {@code true} のときログを出力する.<br><br>
     * タグには自動的にクラス名がセットされる<br>
     * メッセージは自動的に「メソッド名: {@code msg} (ファイル名:行番号) エラーメッセージ」がセットされる<br>
     * 引数 {@code boolean stackTrace} が {@code true} のときスタックトレースを出力する.
     * @param msg ログメッセージ
     */
    private static void error(int logLevel, boolean enable, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (global_log_level <= LOG_LEVEL_ERROR || (global_log_level == LOG_LEVEL_NONE && (enable || logLevel <= LOG_LEVEL_ERROR))) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("debug");
            StackTraceElement[] ste = throwable.getStackTrace();
            String[] className = ste[2].getClassName().split("\\.");
            String tag = className[className.length - 1];
            String message = ste[2].getMethodName() + ": " + msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

            if (!stackTrace) {
                if (tr != null) {
                    message += "\n" + tr;
                    Log.e(tag, message);
                }
                else {
                    Log.e(tag, message);
                }
            }
            else {
                if (tr != null) {
                    Log.e(tag, message, tr);
                }
                else {
                    message += "\nStackTrace:";
                    for (int i = 3; i < ste.length; i++) {
                        message += "\n    at " + ste[i];
                    }
                    Log.e(tag, message);
                }
            }
        }
    }
}
