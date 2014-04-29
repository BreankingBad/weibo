package com.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * 异常信息和日志输出帮助类 将日志或异常打印到内部存储区/mnt/sdcard/mydebug中
 * 
 * @author qisk
 * @date 2012-1-30
 */
public class MyLogcatCapture
{
    final static String TAG = "MyLogcatCapture";

    final static String[] LOGCATCMD_PREFIX = new String[] { "logcat", "-d",
            "-v", "time", "-s" };

    final static String[] LOGCATERRORCMD = new String[] { "logcat", "-d", "-v",
            "time", "-s", "AndroidRuntime:E", "-p" };

    final static String LOGPATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/mydebug";

    final static String LOGCONFIGPATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/mydebug/config";

    // 连续打印到一个LOG文件的文件名，存为成员变量
    static String mSingleFileName = "";

    static boolean mLoopCaptureEnd = false;

    /**
     * TODO(这里用一句话描述这个方法的作用)
     * 
     * @param s
     * @param filePath
     * @param fileName
     * @return
     */
    private static boolean WriteLogToFile(String s, String filePath,
            String fileName)
    {
        boolean res = false;

        File logDir = new File(filePath);
        if (!logDir.exists())
        {
            logDir.mkdirs();
        }

        File f = new File(filePath + "/" + fileName);
        try
        {// 如果目录下没有这个文件则新建一个
            if (!f.exists())
            {
                f.createNewFile();
                Log.d(TAG, "create new file:" + filePath + "/" + fileName);
            }
            // 中文编码方式打开文件
            FileOutputStream fos = new FileOutputStream(f, true);
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fos, "Unicode"));
            bufferedWriter.write(s);
            bufferedWriter.flush();
            bufferedWriter.close();
            fos.close();
            res = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("创建文件失败");
        }

        return res;
    }

    /**
     * 捕获异常信息，存储位置/mnt/sdcard/mydebug，文件名以log+日期时间.log命名。
     * 
     * @param context 应用程序上下文对象
     * @return
     */
    public static boolean CaptureException(final Context context)
    {
        new Thread() {
            @Override
            public void run()
            {
                super.run();
                CaptureAppException(context);
            }
        }.start();
        return true;
    }

    /**
     * 将异常打印到文件中，存储位置/mnt/sdcard/mydebug，文件名以log+日期时间.log命名。
     * 
     * @param context 应用程序上下文(暂时未使用)
     * 
     * @return 是否打印成功
     */
    public static boolean CaptureAppException(Context context)
    {
        Process logCatProc = null;
        BufferedReader reader = null;
        boolean res = false;

        try
        {
            Log.d(TAG, "CaptureAppException Runtime.getRuntime().exec:"
                    + LOGCATERRORCMD);
            logCatProc = Runtime.getRuntime().exec(LOGCATERRORCMD);

            Log.d(TAG, "CaptureAppException waitFor Begin");
            logCatProc.waitFor();
            Log.d(TAG, "CaptureAppException waitFor End");
            reader = new BufferedReader(new InputStreamReader(
                    logCatProc.getInputStream()));
            String line;
            int lineCount = 0;
            final StringBuilder log = new StringBuilder();

            // 获取默认的换行符 "line.separator" 等于“/n”
            String separator = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null)
            {
                log.append(line);
                log.append(separator);
                lineCount++;
            }
            Log.d(TAG, "CaptureAppException get exception log,count="
                    + lineCount);
            // 如果捕获到错误信息，将信息写入文件
            // 如果没有捕获到异常，log.toString()应该为两行：
            // --------- beginning of /dev/log/system
            // --------- beginning of /dev/log/main
            if (lineCount > 2)
            {
                Calendar c1 = Calendar.getInstance();
                String suffix = getCurTimeToString(c1, 0, 0);
                Log.d(TAG, "CaptureAppException WriteLogToFile");
                if (WriteLogToFile(log.toString(), LOGPATH, "log_" + suffix
                        + ".log"))
                {
                    /*
                     * 写入log文件成功后，才清理LogCat，避免写入同一个错误信息到多个日志文件
                     */
                    Log.d(TAG,
                            "CaptureAppException Runtime.getRuntime().exec logcat -c");
                    Runtime.getRuntime().exec("logcat -c");
                    Log.d(TAG, "CaptureAppException res=true");
                    res = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i("MyLogcatCapture",
                    "CaptureAppException exception=" + e.toString());
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    Log.d(TAG, "CaptureAppException reader close");
                    reader.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    Log.i(TAG, "reader close thread Exception.......");
                }
            }
        }
        return res;
    }

    /**
     * TODO(这里用一句话描述这个方法的作用)
     * 
     * @param tagFilePath
     * @param tagList
     * @return
     */
    private static boolean getTagListFromFile(String tagFilePath,
            List<String> tagList)
    {
        boolean res = false;
        tagList.clear();
        try
        {
            FileReader in = new FileReader(tagFilePath);
            BufferedReader bufferedReader = new BufferedReader(in);
            String line;
            while (bufferedReader.ready())
            {
                line = bufferedReader.readLine();
                if (line.length() > 0)
                {
                    tagList.add(line);
                }
            }
            in.close();
            if (tagList.size() > 0)
            {
                res = true;
            }
        }
        catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }

        return res;
    }

    /**
     * 把日志信息保存到文件当中
     * 
     * @param context 应用程序上下文对象
     * @param packageName 应用程序包名
     */
    public static void captureLogToFile(Context context, String packageName)
    {
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);

        Log.e(TAG, "appName== " + appName);
        // 判断TAG配置文件是否存在
        File tagFile = new File(LOGCONFIGPATH + "/" + appName);
        if (!tagFile.exists())
        {
            Log.w(TAG, "no tag config file found !");
            return;
        }
        List<String> tagList = new ArrayList<String>();
        if (!getTagListFromFile(LOGCONFIGPATH + "/" + appName, tagList))
        {
            Log.w(TAG, "get tag config file failed !");
            return;
        }
        String[] LOGCAT_PREFIX = new String[] { "logcat", "-v", "time", "-s" };
        String[] cmdArray = new String[LOGCAT_PREFIX.length + tagList.size()];
        for (int i = 0; i < LOGCAT_PREFIX.length; i++)
        {
            cmdArray[i] = LOGCAT_PREFIX[i];
        }
        for (int j = 0; j < tagList.size(); j++)
        {
            cmdArray[LOGCAT_PREFIX.length + j] = tagList.get(j);
        }
        String logCmd = "";
        for (int k = 0; k < cmdArray.length; k++)
        {
            logCmd = logCmd + cmdArray[k] + " ";
        }
        // create log file
        Calendar c1 = Calendar.getInstance();
        String suffix = getCurTimeToString(c1, 1, 0);
        suffix = suffix.replace(" ", "_").replace(":", ".");
        String logFileName = appName + "_" + suffix + ".log";
        File flog = new File(LOGPATH + "/" + logFileName);
        // start write log file
        String param = logCmd + " -p > " + flog.toString();
        String[] comdline = { "/system/bin/sh", "-c", param };
        String cmd = "pkill logcat";
        Log.w(TAG, "log cmd : " + param);
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.write(cmd.getBytes());
            os.flush();
            os.close();
            // clear the logcat first
            Runtime.getRuntime().exec("logcat -c");
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            Log.e(TAG, "log comdline : " + Arrays.toString(comdline) + "");
            Runtime.getRuntime().exec(comdline);
            Log.e(TAG, "log param : " + param);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 将包含相关TAG的LOG打印到文件中，存储位置/mnt/sdcard/mydebug，文件名以log+日期时间.log命名
     * 
     * @param context 应用程序上下文(暂时未使用)
     * @param packageName 应用程序包名，TAG配置文件存放在/mnt/sdcard/mydebug文件夹中，以包名命名
     * @param fAppendLog 是否需要追加打印
     * @return LOG是否捕获成功的标识
     */
    public static boolean CaptureLogByTag(Context context, String packageName,
            boolean fAppendLog)
    {
        Process logCatProc = null;
        BufferedReader reader = null;
        boolean res = false;
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);
        // 判断TAG配置文件是否存在
        File tagFile = new File(LOGCONFIGPATH + "/" + appName);
        if (!tagFile.exists())
        {
            return false;
        }
        List<String> tagList = new ArrayList<String>();
        if (!getTagListFromFile(LOGCONFIGPATH + "/" + appName, tagList))
        {
            return false;
        }
        try
        {
            String[] cmdArray = new String[LOGCATCMD_PREFIX.length
                    + tagList.size()];
            for (int i = 0; i < LOGCATCMD_PREFIX.length; i++)
            {
                cmdArray[i] = LOGCATCMD_PREFIX[i];
            }
            for (int j = 0; j < tagList.size(); j++)
            {
                cmdArray[LOGCATCMD_PREFIX.length + j] = tagList.get(j);
            }
            String logCmd = "";
            for (int k = 0; k < cmdArray.length; k++)
            {
                logCmd = logCmd + cmdArray[k] + " ";
            }
            logCatProc = Runtime.getRuntime().exec(cmdArray);

            logCatProc.waitFor();

            // 清理LogCat，避免写入同一个信息多次写入日志文件
            Log.d(TAG, "CaptureLogByTag: exec logcat -c");
            Runtime.getRuntime().exec("logcat -c");
            Log.d(TAG, "CaptureLogByTag: finsh!");

            // 获取输入流，将log写入文件
            Log.d(TAG, "CaptureLogByTag:read input stream to log");
            reader = new BufferedReader(new InputStreamReader(
                    logCatProc.getInputStream()));
            String line;
            int lineCount = 0;
            final StringBuilder log = new StringBuilder();

            // 获取默认的换行符 "line.separator" 等于“/n”
            String separator = System.getProperty("line.separator");
            // 读取输出LOG时，去掉如下这两行，减少每次打印重复的信息：
            // --------- beginning of /dev/log/system
            // --------- beginning of /dev/log/main
            while ((line = reader.readLine()) != null)
            {
                if (line.contains("/dev/log/system")
                        || line.contains("/dev/log/main"))
                {
                    continue;
                }
                log.append(line);
                log.append(separator);
                lineCount++;
            }
            Log.d(TAG, "CaptureLogByTag:read input stream, line=" + lineCount);
            if (lineCount > 0)
            {
                Calendar c1 = Calendar.getInstance();
                String suffix = getCurTimeToString(c1, 1, 0);
                suffix = suffix.replace(" ", "_").replace(":", ".");
                String logFileName = "";
                if (fAppendLog)
                {
                    if (mSingleFileName.length() == 0)
                    {
                        mSingleFileName = appName + "_" + suffix + ".log";
                    }
                    logFileName = mSingleFileName;
                }
                else
                {
                    logFileName = appName + "_" + suffix + ".log";
                }
                Log.d(TAG, "CaptureLogByTag: WriteLogToFile=" + logFileName);

                // 将LOG信息写入文件
                if (WriteLogToFile(log.toString(), LOGPATH, logFileName))
                {
                    res = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i("MyLogcatCapture",
                    "CaptureLogByTag Exception=" + e.toString());
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.i(TAG, "reader close thread Exception.......");
                }
            }
            tagList.clear();
        }
        return res;
    }

    /**
     * 开始循环打印LOG内容到单个文件中
     * 
     * @param context 应用程序上下文(暂时未使用)
     * @param packageName 应用程序包名，TAG配置文件存放在/mnt/sdcard/mydebug文件夹中，以包名命名
     * @param time 每次打印的间隔时间
     */
    public static void startLoopCapture(final Context context,
            final String packageName, final int time)
    {
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);
        // 判断TAG配置文件是否存在
        File tagFile = new File(LOGCONFIGPATH + "/" + appName);
        if (!tagFile.exists())
        {
            return;
        }
        mLoopCaptureEnd = false;
        // 先清除LOG buffer,避免保留上一次的LOG信息
        try
        {
            Runtime.getRuntime().exec("logcat -c");
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        new Thread() {
            @Override
            public void run()
            {
                super.run();
                while (mLoopCaptureEnd == false)
                {
                    CaptureLogByTag(context, packageName, true);
                    try
                    {
                        sleep(time);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 将Calendar对象格式的时间日期，进行加减天数处理，返回新的时间日期字符串
     * 
     * @param c 转化为日程对象时间值(通过Calendar.setTimeInMillis进行转换)
     * @param i i=0表示返回去除- ：的时间字符串形式， i=1表示返回带有- ：的时间字符串形式
     * @param n 正数表示当前时间的后n天，负数表示当前时间的前n天
     * @return 转换后的时间字符串
     */
    public static String getCurTimeToString(Calendar c, int i, int n)
    {
        // 空值判断
        if (c == null)
        {
            return null;
        }
        // i=1表示带有- ：的时间字符串形式
        // n=7表示当前时间的后七天
        String time;
        String s1 = "-", s2 = ":", s3 = " ";
        c.add(Calendar.DATE, n);// 增加n天
        int mYear = c.get(Calendar.YEAR); // 获取当前年份
        int mMonth = c.get(Calendar.MONTH);// 获取当前月份
        int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当前月份的日期号码
        int mHour = c.get(Calendar.HOUR_OF_DAY);// 获取当前的小时数
        int mMinute = c.get(Calendar.MINUTE);// 获取当前的分钟数
        int mSecond = c.get(Calendar.SECOND);// 获取当前的秒数

        // 年份
        time = "" + mYear;
        // "-"
        if (1 == i)
        {
            time += s1;
        }
        // 月份
        int mon = mMonth + 1;
        if (mon < 10)
        {
            time = time + 0 + mon;
        }
        else
        {
            time += mon;
        }
        // "-"
        if (1 == i)
        {
            time += s1;
        }
        // 天数
        if (mDay < 10)
        {
            time = time + 0 + mDay;
        }
        else
        {
            time += mDay;
        }
        // " "
        if (1 == i)
        {
            time += s3;
        }
        // 小时
        if (mHour < 10)
        {
            time = time + 0 + mHour;
        }
        else
        {
            time += mHour;
        }
        // ":"
        if (1 == i)
        {
            time += s2;
        }
        // 分钟
        if (mMinute < 10)
        {
            time = time + 0 + mMinute;
        }
        else
        {
            time += mMinute;
        }
        // ":"
        if (1 == i)
        {
            time += s2;
        }
        // 秒数
        if (mSecond < 10)
        {
            time = time + 0 + mSecond;
        }
        else
        {
            time += mSecond;
        }
        return time;
    }

    /**
     * 停止循环打印
     * 
     */
    public static void endLoopCapture()
    {
        mLoopCaptureEnd = true;
    }
}
