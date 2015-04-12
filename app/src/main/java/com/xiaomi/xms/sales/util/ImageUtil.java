
package com.xiaomi.xms.sales.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.xiaomi.xms.sales.model.Image.ImageProcessor;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    private static RoundImageProcessor sCategoryImageProcessor;

    public static void initProcessor(Context context) {
    }

    public static RoundImageProcessor getCategoryImageProcessor() {
        return sCategoryImageProcessor;
    }

    public static final class RoundImageProcessor implements ImageProcessor {
        private int mHeight;
        private int mWidth;
        private Paint mPaint;
        private Rect mRect;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private int mForegroundRes;
        private int mBackgroundRes;
        private int mMaskRes;
        private Context mContext;

        public RoundImageProcessor(Context context, int height, int width, int forgroundRes,
                int backgroundRes, int maskRes) {
            mContext = context;
            mHeight = height;
            mWidth = width;
            mForegroundRes = forgroundRes;
            mBackgroundRes = backgroundRes;
            mMaskRes = maskRes;
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPaint = new Paint();
            mPaint.setFilterBitmap(true);
            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            mRect = new Rect(0, 0, mWidth, mHeight);
        }

        @Override
        public Bitmap processImage(Bitmap originImage) {
            return createPhoto(mContext.getResources(), originImage);
        }

        private synchronized Bitmap createPhoto(Resources res, Bitmap src) {
            if (src == null) {
                return null;
            }
            Bitmap photo = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Drawable bgDrawable = res.getDrawable(mBackgroundRes);
            bgDrawable.setBounds(mRect);
            Drawable fgDrawable = res.getDrawable(mForegroundRes);
            fgDrawable.setBounds(mRect);
            Canvas canvas = new Canvas(photo);
            bgDrawable.draw(canvas);
            cutBitmap(res, src, mMaskRes);
            canvas.drawBitmap(mBitmap, mRect, mRect, null);
            fgDrawable.draw(canvas);
            return photo;
        }

        private void cutBitmap(Resources res, Bitmap src, int maskDrawableId) {
            mCanvas.drawColor(0, Mode.CLEAR);
            Drawable dst = res.getDrawable(maskDrawableId);
            dst.setBounds(mRect);
            dst.draw(mCanvas);
            int width = src.getWidth();
            int height = src.getHeight();
            int left = 0, top = 0;
            if (width > height) {
                left = (width - height) / 2;
                width = height;
            } else if (width < height) {
                top = (height - width) / 2;
                height = width;
            }
            mCanvas.drawBitmap(src, new Rect(left, top, left + width, top + height), mRect, mPaint);
        }
    }

    public static boolean saveToFile(Bitmap bitmap, String path) {
        return saveToFile(bitmap, path, false);
    }

    public static boolean checkDirExists(File file) {
        if (file.exists()) {
            return true;
        }
        File parentFile = file.getParentFile();
        if (checkDirExists(parentFile)) {
            file.mkdir();
        }
        return true;
    }

    public static boolean saveToFile(Bitmap bitmap, String path, boolean saveToPng) {
        FileOutputStream outputStream = null;
        try {
            if (bitmap != null) {
                File file = new File(path);
                if (!file.exists()) {
                    file = file.getParentFile();
                    checkDirExists(file);
                }
                outputStream = new FileOutputStream(path);
                bitmap.compress(saveToPng ? CompressFormat.PNG : CompressFormat.JPEG, 100,
                            outputStream);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    /**  
     * @param 将图片内容解析成字节数组  
     * @param inStream  
     * @return byte[]  
     * @throws Exception  
     */    
    public static byte[] readStream(InputStream inStream) throws Exception {    
        byte[] buffer = new byte[1024];    
        int len = -1;    
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
        while ((len = inStream.read(buffer)) != -1) {    
            outStream.write(buffer, 0, len);    
        }    
        byte[] data = outStream.toByteArray();    
        outStream.close();    
        inStream.close();    
        return data;    
    
    }    
    /**  
     * @param 将字节数组转换为ImageView可调用的Bitmap对象  
     * @param bytes  
     * @param opts  
     * @return Bitmap  
     */    
    public static Bitmap getPicFromBytes(byte[] bytes,    
            BitmapFactory.Options opts) {    
        if (bytes != null)    
            if (opts != null)    
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,    
                        opts);    
            else    
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);    
        return null;    
    }    
    /**  
     * @param 图片缩放  
     * @param bitmap 对象  
     * @param w 要缩放的宽度  
     * @param h 要缩放的高度  
     * @return newBmp 新 Bitmap对象  
     */    
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h){    
        int width = bitmap.getWidth();    
        int height = bitmap.getHeight();    
        Matrix matrix = new Matrix();    
        float scaleWidth = ((float) w / width);    
        float scaleHeight = ((float) h / height);    
        matrix.postScale(scaleWidth, scaleHeight);    
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,    
                matrix, true);    
        return newBmp;    
    }    
        
    /**  
     * 把Bitmap转Byte  
     */    
    public static byte[] Bitmap2Bytes(Bitmap bm){    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();    
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);    
        return baos.toByteArray();    
    }    
    /**  
     * 把字节数组保存为一个文件  
     */    
    public static File getFileFromBytes(byte[] b, String outputFile) {    
        BufferedOutputStream stream = null;    
        File file = null;    
        try {    
            file = new File(outputFile);    
            FileOutputStream fstream = new FileOutputStream(file);    
            stream = new BufferedOutputStream(fstream);    
            stream.write(b);    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally {    
            if (stream != null) {    
                try {    
                    stream.close();    
                } catch (IOException e1) {    
                    e1.printStackTrace();    
                }    
            }    
        }    
        return file;    
    }    
}
