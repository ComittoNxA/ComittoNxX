package src.comitton.common;

import android.os.Build;
import android.os.DeadSystemException;
import android.util.Log;
import androidx.annotation.Nullable;

import java.net.UnknownHostException;
import java.util.Arrays;

public class Logcat {


    public static String Concat(String tag, String msg, Exception e) {

        String ret = "";
        if (tag != null) {
            ret += tag + ": ";
        }
        if (msg != null) {
            ret += msg + ": ";
        }
        if (e != null) {
            ret += e.toString();
        }
        return ret;
    }

    public static void v(@Nullable String tag, @Nullable String msg) {
        verbose(true, tag, msg, null, false);
    }

    public static void v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        verbose(true, tag, msg, tr,false);
    }

    public static void v(@Nullable String tag, @Nullable String msg, boolean stackTrace) {
        verbose(true, tag, msg, null, stackTrace);
    }

    public static void v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        verbose(true, tag, msg, tr, stackTrace);
    }

    public static void v(boolean enable, @Nullable String tag, @Nullable String msg) {
        if (enable) {
            verbose(true, tag, msg, null, false);
        }
    }

    public static void v(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enable) {
            verbose(true, tag, msg, tr, false);
        }
    }

    public static void v(boolean enable, @Nullable String tag, @Nullable String msg, boolean stackTrace) {
        if (enable) {
            verbose(true, tag, msg, null, stackTrace);
        }
    }

    public static void v(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            verbose(true, tag, msg, tr, stackTrace);
        }
    }

    private static void verbose(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("verbose");
            StackTraceElement[] ste = throwable.getStackTrace();
            String message = msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

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

    public static void d(@Nullable String tag, @Nullable String msg) {
        debug(true, tag, msg, null, false);
    }

    public static void d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        debug(true, tag, msg, tr,false);
    }

    public static void d(@Nullable String tag, @Nullable String msg, boolean stackTrace) {
        debug(true, tag, msg, null, stackTrace);
    }

    public static void d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        debug(true, tag, msg, tr, stackTrace);
    }

    public static void d(boolean enable, @Nullable String tag, @Nullable String msg) {
        if (enable) {
            debug(true, tag, msg, null, false);
        }
    }

    public static void d(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enable) {
            debug(true, tag, msg, tr, false);
        }
    }

    public static void d(boolean enable, @Nullable String tag, @Nullable String msg, boolean stackTrace) {
        if (enable) {
            debug(true, tag, msg, null, stackTrace);
        }
    }

    public static void d(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            debug(true, tag, msg, tr, stackTrace);
        }
    }

    private static void debug(boolean enable, @Nullable String tag1, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
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


    public static void i(@Nullable String tag, @Nullable String msg) {
        info(true, tag, msg, null, false);
    }

    public static void i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        info(true, tag, msg, tr,false);
    }

    public static void i(@Nullable String tag, @Nullable String msg, boolean stackTrace) {
        info(true, tag, msg, null, stackTrace);
    }

    public static void i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        info(true, tag, msg, tr, stackTrace);
    }

    public static void i(boolean enable, @Nullable String tag, @Nullable String msg) {
        if (enable) {
            info(true, tag, msg, null, false);
        }
    }

    public static void i(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enable) {
            info(true, tag, msg, tr, false);
        }
    }

    public static void i(boolean enable, @Nullable String tag, @Nullable String msg, boolean stackTrace) {
        if (enable) {
            info(true, tag, msg, null, stackTrace);
        }
    }

    public static void i(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            info(true, tag, msg, tr, stackTrace);
        }
    }

    private static void info(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("info");
            StackTraceElement[] ste = throwable.getStackTrace();
            String message = msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

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

    public static void w(@Nullable String tag, @Nullable String msg) {
        warn(true, tag, msg, null, false);
    }

    public static void w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        warn(true, tag, msg, tr,false);
    }

    public static void w(@Nullable String tag, @Nullable String msg, boolean stackTrace) {
        warn(true, tag, msg, null, stackTrace);
    }

    public static void w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        warn(true, tag, msg, tr, stackTrace);
    }

    public static void w(boolean enable, @Nullable String tag, @Nullable String msg) {
        if (enable) {
            warn(true, tag, msg, null, false);
        }
    }

    public static void w(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enable) {
            warn(true, tag, msg, tr, false);
        }
    }

    public static void w(boolean enable, @Nullable String tag, @Nullable String msg, boolean stackTrace) {
        if (enable) {
            warn(true, tag, msg, null, stackTrace);
        }
    }

    public static void w(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            warn(true, tag, msg, tr, stackTrace);
        }
    }

    private static void warn(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("warn");
            StackTraceElement[] ste = throwable.getStackTrace();
            String message = msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

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

    public static void e(@Nullable String tag, @Nullable String msg) {
        error(true, tag, msg, null, false);
    }

    public static void e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        error(true, tag, msg, tr,false);
    }

    public static void e(@Nullable String tag, @Nullable String msg, boolean stackTrace) {
        error(true, tag, msg, null, stackTrace);
    }

    public static void e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        error(true, tag, msg, tr, stackTrace);
    }

    public static void e(boolean enable, @Nullable String tag, @Nullable String msg) {
        if (enable) {
            error(true, tag, msg, null, false);
        }
    }

    public static void e(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (enable) {
            error(true, tag, msg, tr, false);
        }
    }

    public static void e(boolean enable, @Nullable String tag, @Nullable String msg, boolean stackTrace) {
        if (enable) {
            error(true, tag, msg, null, stackTrace);
        }
    }

    public static void e(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            error(true, tag, msg, tr, stackTrace);
        }
    }

    private static void error(boolean enable, @Nullable String tag, @Nullable String msg, @Nullable Throwable tr, boolean stackTrace) {
        if (enable) {
            // enable の時だけログを出力する
            Throwable throwable = new Throwable("error");
            StackTraceElement[] ste = throwable.getStackTrace();
            String message = msg + " (" + ste[2].getFileName() + ":" + ste[2].getLineNumber() + ")";

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
