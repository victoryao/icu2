package com.xiaomi.xms.sales.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.model.Order.ProductBrief;

public class PrinterService {
	//private static String PRINT_IP = "10.236.247.133";
	private static int PRINT_PORT = 9100;
	Socket socket = null;
	OutputStream socketOut = null;

	public PrinterService(String PRINT_IP) throws IOException {
		socket = new Socket(PRINT_IP, PRINT_PORT);
		System.err.println("host:"+PRINT_IP+":"+PRINT_PORT);
		socketOut = socket.getOutputStream();
	}
	
	/**
	 * 打印购物小票
	 * @param order
	 * @param context
	 */
	public void print(Order order,Context context) {
		System.err.println("print start ...");
		try {
			String charSet = "gbk";
			int length = 0;   //发送数据总长度
			byte[] logoinit = new byte[]{0x1B,0x40,0x1B,0x1C,0x70,0x01,0x00};  //初始化打印机,打印LOGO，倒数参数是logo编号
			int logoLength = logoinit.length;
			length += logoLength;
	        
			String mihomeInfo = order.getOrgName()+"\n"+
		                         order.getOrgAddress()+"\n"+
							     "电话："+order.getOrgTel()+"\n"+
					             "\n"+
					             "http://www.mi.com"+"\n"+
					             "\n";
			byte[] mihomeInfoByte = mihomeInfo.getBytes(charSet);
			int mihomeInfoLength = mihomeInfoByte.length;
			length += mihomeInfoLength;
			
			byte[] bigLine = new byte[]{0x1B,0x1C,0x70,0x02,0x00};  //打印粗线，第四个参数是线条编号
			int bigLineLength = bigLine.length;
			length += bigLineLength;
			
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date addTime = new Date(Long.parseLong(order.getAddTime()));
			String addTimeStr = sf.format(addTime);

			String userInfo = addTimeStr+"\n"+
				     order.getOrderUserName()+"\n"+
				     order.getOrderUserEmail()+"\n";
			byte[] userInfoByte = userInfo.getBytes(charSet);
			int userInfoLength = userInfoByte.length;
			length += userInfoLength;
			
            byte[] smallLine = new byte[]{0x1B,0x1C,0x70,0x03,0x00};  //打印细线，第四个参数是线条编号
            int smallLineLength = smallLine.length;
            length += smallLineLength;
			
			ArrayList<ProductBrief> pList = order.getProductList();
			for(ProductBrief p : pList){
				String pInfo = p.mProductName+"  "+
					           "x"+p.mProductCount+"  "+
					           "￥"+p.mProductPrice+
					           "\n";
				int pInfoLength =  pInfo.getBytes(charSet).length;
				length += pInfoLength;
			}
			
			String refundDate = "";
			if(addTime != null){
				Calendar c = Calendar.getInstance();
				c.setTime(addTime);
				c.add(Calendar.DATE, Constants.REFUND_DAY);
				Date refundTime = c.getTime();
				SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
				refundDate = sf2.format(refundTime);
			}
			String info = "\n"+"退货日期："+refundDate+"\n"+
						     "支持与客户服务："+"\n"+
						     "http://fuwu.mi.com"+"\n"+
						     "\n";
			byte[] infoByte = info.getBytes(charSet);
			int infoLength = infoByte.length;
			length += infoLength;
			length += smallLineLength;
			
		    int payId = order.getPayid();
		    String payType = Constants.POS_PAY_NAME;
		    if(payId == Constants.CASH_PAY){
		    	payType = Constants.CASH_PAY_NAME;
		    }
		    String payInfo = "                           总计   ￥"+order.getFee()+"\n"+
				     "            经"+payType+"支付的金额   ￥"+order.getFee()+"\n"+
				     "                       应找金额   ￥"+0.0+"\n";
		    byte[] payInfoByte = payInfo.getBytes(charSet);
		    int payInfoLength = payInfoByte.length;
		    length += payInfoLength;
			length += bigLineLength;
	        
			int nLength = "\n".getBytes(charSet).length;
			length += nLength;
			
			//打印条形码
			byte[] center = new byte[]{0x1B,0x1d,0x61,0x01};   //居中命令，最后一个参数1：居中
			int centerLength = center.length;
			length += centerLength;
			
			byte[] code = new byte[]{0x1B,0x62,0x06,0x02,0x02,0x3C};  //打印条形码命令
			int codeLength = code.length;
			length += codeLength;
			byte[] orderId = order.getOrderId().getBytes();
			int idLength = orderId.length;
			length += idLength; 
			length += 2;
            
            byte[] left = new byte[]{0x1B,0x1d,0x61,0x00};  //向左对齐，最后一个参数0：向左对齐
            int leftLength = left.length;
            length += leftLength;
            
            String footerInfo = "\n\n\n\n"+order.getMituShuo()+"\n"+
							     "请告诉我们您在小米零售店的购物体验\n"+
							     "访问 http://weibo.com/xiaomikeji\n";
            byte[] footer = footerInfo.getBytes(charSet);
            int footerLength = footer.length;
            length += footerLength;
			
			// 下面指令为打印完成后自动走纸
			byte[] cutPaper = new byte[]{27,100,2,10};
			int cutLength = cutPaper.length;
			length += cutLength;
			
			byte[] allByte = new byte[length];
			int startIndex = 0;
		
			
			startIndex = addInfo(allByte, startIndex, logoinit);
			startIndex = addInfo(allByte, startIndex, mihomeInfoByte);
			startIndex = addInfo(allByte, startIndex, bigLine);
			startIndex = addInfo(allByte, startIndex, userInfoByte);
			startIndex = addInfo(allByte, startIndex, smallLine);
			for(ProductBrief p : pList){
				String pInfo = p.mProductName+"  "+
							     "x"+p.mProductCount+"  "+
							     "￥"+p.mProductPrice+
							     "\n";
				byte[] pByte = pInfo.getBytes(charSet);
				startIndex = addInfo(allByte, startIndex, pByte);
			}
			startIndex = addInfo(allByte, startIndex, infoByte);
			startIndex = addInfo(allByte, startIndex, smallLine);
			startIndex = addInfo(allByte, startIndex, payInfoByte);
			startIndex = addInfo(allByte, startIndex, bigLine);
			startIndex = addInfo(allByte, startIndex, "\n".getBytes(charSet));
			startIndex = addInfo(allByte, startIndex, center);
			startIndex = addInfo(allByte, startIndex, code);
			startIndex = addInfo(allByte, startIndex, orderId);
			allByte[startIndex++] = 0x1E;
			allByte[startIndex++] = 0x0A;
			startIndex = addInfo(allByte, startIndex, left);
			startIndex = addInfo(allByte, startIndex, footer);
			startIndex = addInfo(allByte, startIndex, cutPaper);
			
			
			socketOut.write(allByte);
			socketOut.flush();
			
			socketOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    public int addInfo(byte[] allByte,int startIndex,byte[] addInfo){
    	for(int i=0;i<addInfo.length;i++){
			allByte[startIndex++] = addInfo[i];
		}
    	return startIndex;
    }
}