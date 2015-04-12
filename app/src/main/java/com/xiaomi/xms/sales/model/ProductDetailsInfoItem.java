package com.xiaomi.xms.sales.model;

import java.io.Serializable;

public class ProductDetailsInfoItem implements Serializable{
  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image mProductDetailPhoto;

    public Image getImage() {
        return mProductDetailPhoto;
    }

    public void setImage(Image photo){
        mProductDetailPhoto = photo;
    }

}
