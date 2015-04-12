package com.xiaomi.xms.sales.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

public class LogcatHelper {
	    
	    private static LogcatHelper INSTANCE = null;   
	    private static String PATH_LOGCAT;   
	    private LogDumper mLogDumper = null;   
	    private int mPId;   
	    private static String serviceIP = "10.236.121.5";  //上传文件的服务器
	    private static int servicePort = 8080;
	    /**  
	     *   
	     * 初始化目录  
	     *   
	     * */  
	    public void init(Context context) {   
	        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中    
	            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sales";   
	        } else {// 如果SD卡不存在，就保存到本应用的目录下    
	            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()+ File.separator + "sales";   
	        } 
	          
	    	//PATH_LOGCAT = context.getFilesDir().getAbsolutePath()+ File.separator + "sales";     
	        File file = new File(PATH_LOGCAT);
	        if (!file.exists()) {   
	            file.mkdirs();   
	        }   
	    }   
	    
	    public static LogcatHelper getInstance(Context context) {   
	        if (INSTANCE == null) {   
	            INSTANCE = new LogcatHelper(context);   
	        }   
	        return INSTANCE;   
	    }   
	    
	    private LogcatHelper(Context context) {   
	        init(context);   
	        mPId = android.os.Process.myPid();   
	    }   
	    
	    public void start() {   
	        if(mLogDumper != null && mLogDumper.isAlive()){
	        	stop();
	        }
	        if (mLogDumper == null)   
	            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
	        mLogDumper.start(); 
	    }   
	    
	    public void stop() {   
	        if (mLogDumper != null) {   
	            mLogDumper.stopLogs();   
	            mLogDumper = null;   
	        }   
	    }   
	    
	    private class LogDumper extends Thread {   
	    
	        private Process logcatProc;   
	        private BufferedReader mReader = null;   
	        private boolean mRunning = true;   
	        String cmds = null;   
	        private String mPID;   
	        private FileOutputStream out = null;   
	        File file = null;
	        public LogDumper(String pid, String dir) {   
	            mPID = pid;   
	            try {   
	            	file = new File(dir, "sales-" + getNowTime() + ".log");
	                out = new FileOutputStream(file);   
	            } catch (FileNotFoundException e) {
	                e.printStackTrace();   
	            }   
	    
	            /**  
	             *   
	             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s  
	             *   
	             * 显示当前mPID程序的 E和W等级的日志.  
	             *   
	             * */  
	    
	            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";    
	             cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息    
	            // cmds = "logcat -s way";//打印标签过滤信息    
	            //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
	           // cmds = "logcat -s System.out";
	    
	        }   
	    
	        public void stopLogs() {   
	            mRunning = false;   
	        }   
	    
	        @Override  
	        public void run() {   
	            try {   
	                logcatProc = Runtime.getRuntime().exec(cmds);   
	                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);   
	                String line = null;   
	                while (mRunning && (line = mReader.readLine()) != null) {   
	                    if (!mRunning) {   
	                        break;   
	                    }   
	                    if (line.length() == 0) {   
	                        continue;   
	                    }   
	                    if (out != null && line.contains(mPID)) {   
	                        out.write((getNowTime() + "  " + line + "\n").getBytes());   
	                    }   
	                }   
	                uploadFile(file);   //上传日志文件
	            } catch (IOException e) {   
	                e.printStackTrace();   
	            }
	            finally {   
	                if (logcatProc != null) {   
	                    logcatProc.destroy();   
	                    logcatProc = null;   
	                }   
	                if (mReader != null) {   
	                    try {   
	                        mReader.close();   
	                        mReader = null;   
	                    } catch (IOException e) {   
	                        e.printStackTrace();   
	                    }   
	                }   
	                if (out != null) {   
	                    try {   
	                        out.close();   
	                    } catch (IOException e) {   
	                        e.printStackTrace();   
	                    }   
	                    out = null;   
	                }  
	                mRunning = false;   
	                
	            } 
	        } 
	    }
	    
	    public String getNowTime(){
	    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");   
	        String date = format.format(new Date(System.currentTimeMillis()));   
	        return date;
	    }
		
	    /**  
	     * 上传文件  
	     * @param uploadFile  
	     */    
	    public void uploadFile(final File uploadFile) {    
	        
	                try {    
	                    System.out.println("file path:"+uploadFile.getAbsolutePath());   
	                    String head = "Content-Length="+ uploadFile.length() + ";filename="+ uploadFile.getName()+"\n";    
	                    Socket socket = new Socket(serviceIP,servicePort);    
	                    OutputStream outStream = socket.getOutputStream();    
	                    outStream.write(head.getBytes());    
	                        
//	                    PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());        
//	                    String response = readLine(inStream);    
//	                    String[] items = response.split(";");    
//	                    String responseid = items[0].substring(items[0].indexOf("=")+1);    
//	                    String position = items[1].substring(items[1].indexOf("=")+1);    
	                      
	                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(uploadFile)), 1024);   
		                String line = null;   
		                while ((line = br.readLine()) != null) {   
		           
		                    if (line.length() == 0) {   
		                        continue;   
		                    }  
		                    outStream.write((getNowTime() + "  " + line + "\n").getBytes());  
		                }   
	                    //fileOutStream.close();    
	                    outStream.close();    
	                    //inStream.close();    
	                    socket.close();    
	                    br.close();
	                    //if(length==uploadFile.length()){
	                    	uploadFile.delete();    
	                   // }
	                } catch (Exception e) {    
	                    e.printStackTrace();    
	                }   
	    }    
	    
	    public static void save(File file, byte[] data) throws Exception {  
	         FileOutputStream outStream = new FileOutputStream(file);  
	         outStream.write(data);  
	         outStream.close();  
	     }  
	       
	     public static String readLine(PushbackInputStream in) throws IOException {  
	            char buf[] = new char[128];  
	            int room = buf.length;  
	            int offset = 0;  
	            int c;  
	         loop: while (true) {  
	                switch (c = in.read()) {  
	                    case -1:  
	                    case '\n':  
	                        break loop;  
	                    case '\r':  
	                        int c2 = in.read();  
	                        if ((c2 != '\n') && (c2 != -1)) in.unread(c2);  
	                        break loop;  
	                    default:  
	                        if (--room < 0) {  
	                            char[] lineBuffer = buf;  
	                            buf = new char[offset + 128];  
	                            room = buf.length - offset - 1;  
	                            System.arraycopy(lineBuffer, 0, buf, 0, offset);  
	                             
	                        }  
	                        buf[offset++] = (char) c;  
	                        break;  
	                }  
	            }  
	            if ((c == -1) && (offset == 0)) return null;  
	            return String.copyValueOf(buf, 0, offset);  
	    }  
	       
	    /** 
	    * 读取流 
	    * @param inStream 
	    * @return 字节数组 
	    * @throws Exception 
	    */  
	    public static byte[] readStream(InputStream inStream) throws Exception{  
	            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();  
	            byte[] buffer = new byte[1024];  
	            int len = -1;  
	            while( (len=inStream.read(buffer)) != -1){  
	                outSteam.write(buffer, 0, len);  
	            }  
	            outSteam.close();  
	            inStream.close();  
	            return outSteam.toByteArray();  
	    }  
}
