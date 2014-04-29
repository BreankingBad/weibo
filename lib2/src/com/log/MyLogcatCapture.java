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
 * �쳣��Ϣ����־��������� ����־���쳣��ӡ���ڲ��洢��/mnt/sdcard/mydebug��
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

    // ������ӡ��һ��LOG�ļ����ļ�������Ϊ��Ա����
    static String mSingleFileName = "";

    static boolean mLoopCaptureEnd = false;

    /**
     * TODO(������һ�仰�����������������)
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
        {// ���Ŀ¼��û������ļ����½�һ��
            if (!f.exists())
            {
                f.createNewFile();
                Log.d(TAG, "create new file:" + filePath + "/" + fileName);
            }
            // ���ı��뷽ʽ���ļ�
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
            System.out.println("�����ļ�ʧ��");
        }

        return res;
    }

    /**
     * �����쳣��Ϣ���洢λ��/mnt/sdcard/mydebug���ļ�����log+����ʱ��.log������
     * 
     * @param context Ӧ�ó��������Ķ���
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
     * ���쳣��ӡ���ļ��У��洢λ��/mnt/sdcard/mydebug���ļ�����log+����ʱ��.log������
     * 
     * @param context Ӧ�ó���������(��ʱδʹ��)
     * 
     * @return �Ƿ��ӡ�ɹ�
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

            // ��ȡĬ�ϵĻ��з� "line.separator" ���ڡ�/n��
            String separator = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null)
            {
                log.append(line);
                log.append(separator);
                lineCount++;
            }
            Log.d(TAG, "CaptureAppException get exception log,count="
                    + lineCount);
            // ������񵽴�����Ϣ������Ϣд���ļ�
            // ���û�в����쳣��log.toString()Ӧ��Ϊ���У�
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
                     * д��log�ļ��ɹ��󣬲�����LogCat������д��ͬһ��������Ϣ�������־�ļ�
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
     * TODO(������һ�仰�����������������)
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
     * ����־��Ϣ���浽�ļ�����
     * 
     * @param context Ӧ�ó��������Ķ���
     * @param packageName Ӧ�ó������
     */
    public static void captureLogToFile(Context context, String packageName)
    {
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);

        Log.e(TAG, "appName== " + appName);
        // �ж�TAG�����ļ��Ƿ����
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
     * ���������TAG��LOG��ӡ���ļ��У��洢λ��/mnt/sdcard/mydebug���ļ�����log+����ʱ��.log����
     * 
     * @param context Ӧ�ó���������(��ʱδʹ��)
     * @param packageName Ӧ�ó��������TAG�����ļ������/mnt/sdcard/mydebug�ļ����У��԰�������
     * @param fAppendLog �Ƿ���Ҫ׷�Ӵ�ӡ
     * @return LOG�Ƿ񲶻�ɹ��ı�ʶ
     */
    public static boolean CaptureLogByTag(Context context, String packageName,
            boolean fAppendLog)
    {
        Process logCatProc = null;
        BufferedReader reader = null;
        boolean res = false;
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);
        // �ж�TAG�����ļ��Ƿ����
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

            // ����LogCat������д��ͬһ����Ϣ���д����־�ļ�
            Log.d(TAG, "CaptureLogByTag: exec logcat -c");
            Runtime.getRuntime().exec("logcat -c");
            Log.d(TAG, "CaptureLogByTag: finsh!");

            // ��ȡ����������logд���ļ�
            Log.d(TAG, "CaptureLogByTag:read input stream to log");
            reader = new BufferedReader(new InputStreamReader(
                    logCatProc.getInputStream()));
            String line;
            int lineCount = 0;
            final StringBuilder log = new StringBuilder();

            // ��ȡĬ�ϵĻ��з� "line.separator" ���ڡ�/n��
            String separator = System.getProperty("line.separator");
            // ��ȡ���LOGʱ��ȥ�����������У�����ÿ�δ�ӡ�ظ�����Ϣ��
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

                // ��LOG��Ϣд���ļ�
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
     * ��ʼѭ����ӡLOG���ݵ������ļ���
     * 
     * @param context Ӧ�ó���������(��ʱδʹ��)
     * @param packageName Ӧ�ó��������TAG�����ļ������/mnt/sdcard/mydebug�ļ����У��԰�������
     * @param time ÿ�δ�ӡ�ļ��ʱ��
     */
    public static void startLoopCapture(final Context context,
            final String packageName, final int time)
    {
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);
        // �ж�TAG�����ļ��Ƿ����
        File tagFile = new File(LOGCONFIGPATH + "/" + appName);
        if (!tagFile.exists())
        {
            return;
        }
        mLoopCaptureEnd = false;
        // �����LOG buffer,���Ᵽ����һ�ε�LOG��Ϣ
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
     * ��Calendar�����ʽ��ʱ�����ڣ����мӼ��������������µ�ʱ�������ַ���
     * 
     * @param c ת��Ϊ�ճ̶���ʱ��ֵ(ͨ��Calendar.setTimeInMillis����ת��)
     * @param i i=0��ʾ����ȥ��- ����ʱ���ַ�����ʽ�� i=1��ʾ���ش���- ����ʱ���ַ�����ʽ
     * @param n ������ʾ��ǰʱ��ĺ�n�죬������ʾ��ǰʱ���ǰn��
     * @return ת�����ʱ���ַ���
     */
    public static String getCurTimeToString(Calendar c, int i, int n)
    {
        // ��ֵ�ж�
        if (c == null)
        {
            return null;
        }
        // i=1��ʾ����- ����ʱ���ַ�����ʽ
        // n=7��ʾ��ǰʱ��ĺ�����
        String time;
        String s1 = "-", s2 = ":", s3 = " ";
        c.add(Calendar.DATE, n);// ����n��
        int mYear = c.get(Calendar.YEAR); // ��ȡ��ǰ���
        int mMonth = c.get(Calendar.MONTH);// ��ȡ��ǰ�·�
        int mDay = c.get(Calendar.DAY_OF_MONTH);// ��ȡ��ǰ�·ݵ����ں���
        int mHour = c.get(Calendar.HOUR_OF_DAY);// ��ȡ��ǰ��Сʱ��
        int mMinute = c.get(Calendar.MINUTE);// ��ȡ��ǰ�ķ�����
        int mSecond = c.get(Calendar.SECOND);// ��ȡ��ǰ������

        // ���
        time = "" + mYear;
        // "-"
        if (1 == i)
        {
            time += s1;
        }
        // �·�
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
        // ����
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
        // Сʱ
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
        // ����
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
        // ����
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
     * ֹͣѭ����ӡ
     * 
     */
    public static void endLoopCapture()
    {
        mLoopCaptureEnd = true;
    }
}
