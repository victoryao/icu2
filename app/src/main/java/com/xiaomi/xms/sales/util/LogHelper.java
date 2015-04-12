package com.xiaomi.xms.sales.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;


public class LogHelper {

	
	 	private static LogHelper INSTANCE = null;   
	    private static String PATH_LOGCAT;   
	    static File file;
	    /**  
	     *   
	     * 初始化目录  
	     *   
	     * */  
	    public static File init(Context context) {   
	        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中    
	            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sales";   
	        } else {// 如果SD卡不存在，就保存到本应用的目录下    
	            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()+ File.separator + "sales";   
	        } 
	          
	    	//PATH_LOGCAT = context.getFilesDir().getAbsolutePath()+ File.separator + "sales";     
	        file = new File(PATH_LOGCAT);
	        if (!file.exists()) {   
	            file.mkdirs();   
	        }   
	        Utils.Preference.setStringPref(context,Constants.Prefence.PREF_LOG_FILE_PATH,PATH_LOGCAT);
	        file = new File(PATH_LOGCAT, "sales1.log");
	        return file;
	    }   
	    
	    public static LogHelper getInstance(Context context) {   
	        if (INSTANCE == null) {   
	            INSTANCE = new LogHelper(context);   
	        }   
	        return INSTANCE;   
	    }   
	    
	    private LogHelper(Context context) {   
	        init(context);   
	    }   
	    
	 public static void save(byte[] data) throws Exception {
		 FileOutputStream outStream = new FileOutputStream(file,true);
		 outStream.write(data);
		 String lineSeparator = ";";//System.getProperty("line.separator", "\n");
		 outStream.write(lineSeparator.getBytes());
		 outStream.close();
	 }
	 
	 public static void save(String orderId,String type,String input,String output) throws Exception {
		 String content = System.currentTimeMillis()+","+orderId+","+type+","+input+","+output+";";
		 FileOutputStream outStream = new FileOutputStream(file,true);
		 outStream.write(content.getBytes());
		 outStream.close();
	 }
	 
	 public static String readLine(File file) throws IOException {
		 if(file == null){
			return null;
		 }
		 StringBuffer sb = new StringBuffer();
		 try {
             if(file.isFile() && file.exists()){ //判断文件是否存在
                 InputStreamReader read = new InputStreamReader(
                 new FileInputStream(file));//考虑到编码格式
                 BufferedReader bufferedReader = new BufferedReader(read);
                 
                 String lineTxt = null;
                 while((lineTxt = bufferedReader.readLine()) != null){
                	 sb.append(lineTxt);
                 }
                 read.close();
     }else{
         System.out.println("找不到指定的文件");
     }
     } catch (Exception e) {
         System.out.println("读取文件内容出错");
         e.printStackTrace();
     }
		return sb.toString();
	}
	 
	 public static void cleanFile(File file) throws IOException {
		 FileOutputStream outStream = new FileOutputStream(file);
		 outStream.write("".getBytes());
		 outStream.close();
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

