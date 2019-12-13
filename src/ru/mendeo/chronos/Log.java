package ru.mendeo.chronos;

public class Log 
{
    private static boolean logEnabled = false;

    public static void i(String tag, String string)
    {
        if (logEnabled) android.util.Log.i(tag, string);
    }
    public static void e(String tag, String string)
    {
        if (logEnabled) android.util.Log.e(tag, string);
    }
    public static void d(String tag, String string)
    {
        if (logEnabled) android.util.Log.d(tag, string);
    }
    public static void v(String tag, String string)
    {
        if (logEnabled) android.util.Log.v(tag, string);
    }
    public static void w(String tag, String string)
    {
        if (logEnabled) android.util.Log.w(tag, string);
    }
}
