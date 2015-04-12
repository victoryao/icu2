
package com.xiaomi.xms.sales.model;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.Serializable;

public class Image implements Serializable{
  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mFileUrl;
    private ImageProcessor mImageProcesser;

    public Image(String fileUrl) {
        mFileUrl = fileUrl;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(mFileUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Image) {
            Image image = (Image) o;
            return TextUtils.equals(image.mFileUrl, mFileUrl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mFileUrl == null ? 0 : mFileUrl.hashCode();
    }

    @Override
    public String toString() {
        return " File url is:" + mFileUrl;
    }

    public Bitmap proccessImage(Bitmap originImage) {
        if (mImageProcesser != null) {
            return mImageProcesser.processImage(originImage);
        }
        return originImage;
    }

    public interface ImageProcessor {
        /**
         * process the image, the originImage will not be used and should be recycled
         */
        Bitmap processImage(Bitmap originImage);
    }

    public void setImageProcessor(ImageProcessor processor) {
        mImageProcesser = processor;
    }
}
