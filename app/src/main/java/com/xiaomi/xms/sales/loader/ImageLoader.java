
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.request.RequestStream;
import com.xiaomi.xms.sales.util.Coder;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.widget.SelfBindView;
import com.xiaomi.xms.sales.widget.gallery.ZoomImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Encapsulate an image and all its binding views. NOTE: Different views maybe
 * display the same image.
 */
class ImageLoaderItem {
    public Image image;
    public ArrayList<ImageView> views;

    public ImageLoaderItem(Image image, ArrayList<ImageView> views) {
        this.image = image;
        this.views = views;
    }

    /**
     * Add a view into binding array. If the view is already in the array, do
     * nothing.
     */
    public void add(ImageView view) {
        for (ImageView v : views) {
            if (v == view)
                return;
        }
        views.add(view);
    }

    /**
     * Only for debug.
     */
    @Override
    public String toString() {
        String to = image.toString();
        to += "=>[";
        for (ImageView v : views) {
            to += "" + v.hashCode() + ",";
        }
        to += "]";
        return to;
    }
}

/**
 * A hash map of loading items.
 */
class ImageLoaderItemMap {
    private ConcurrentHashMap<Image, ImageLoaderItem> mData;

    public ImageLoaderItemMap() {
        mData = new ConcurrentHashMap<Image, ImageLoaderItem>();
    }

    /**
     * Add an image and its binding view into database.
     */
    public void add(Image image, ImageView view) {
        if (!mData.containsKey(image)) {
            mData.put(image, new ImageLoaderItem(image, new ArrayList<ImageView>()));
        }
        mData.get(image).add(view);
    }

    public void remove(Image image) {
        if (image == null)
            return;
        mData.remove(image);
    }

    public ImageLoaderItem get(Image image) {
        return mData.get(image);
    }

    public Iterator<Image> iterator() {
        return mData.keySet().iterator();
    }

    public boolean contains(Image image) {
        return mData.contains(image);
    }

    /**
     * Only for debug.
     */
    @Override
    public String toString() {
        String to = new String();
        Iterator<Image> it = iterator();
        while (it.hasNext()) {
            Image image = it.next();
            ImageLoaderItem item = get(image);
            to += item.toString();
            to += ", ";
        }
        return to;
    }
}

public class ImageLoader implements Handler.Callback {
    private static final String TAG = "ImageLoader";

    private static ImageLoader sLoader;
    private Context mContext;

    // loading images thread pool
    private ExecutorService mExecutor;
    private static final int THREAD_POOL_COUNT = 6;
    private volatile boolean mPauseLoading;

    private Handler mUIHandler;
    private static final int MESSAGE_REQUEST_LOADED = 1;

    // LRU cache size
    private static final int BITMAP_CACHE_SIZE = 10 * 1024 * 1024;

    // LRU cache 中缓存的Bitmap信息
    private static class BitmapHolder {
        private static final int NEEDED = 0;
        private static final int LOADED = 1;
        private static final int LOADING = 2;

        int mState; // 当前的加载状态
        Reference<Bitmap> mBitmapRef; // 缓存的Bitmap信息
        byte[] mBytes;
    }

    // 缓存图片的LRU cache
    private final LruCache<Image, BitmapHolder> mBitmapCache;
    private Byte mBitmapCacheLock = new Byte((byte) 0);

    // 请求加载数据的Request
    private final ImageLoaderItemMap mPendingRequest;

    /**
     * Record the latest image that a view is bound to. NOTE: A view may be
     * bound to different images in time line. Only the latest image is bound to
     * the view. This is usually happened at view-reuse in list view.
     */
    private final ConcurrentHashMap<ImageView, Image> mLatestRequest;

    /**
     * Must be called when the application is launched.
     */
    public static void init(Context context) {
        if (sLoader == null) {
            sLoader = new ImageLoader(context);
        }
    }

    public synchronized static ImageLoader getInstance() {
        return sLoader;
    }

    /**
     * Load the image from URL and bind to the specified view.
     * 
     * @param defaultImageRes A local resource ID as the default image. The
     *            default image will be shown before the actual image is loaded.
     */
    public void loadImage(ImageView view, Image image, int defaultImageRes) {
        loadImage(view, image, null, defaultImageRes);
    }

    public void loadImage(ImageView view, Image image, Bitmap defaultBitmap) {
        loadImage(view, image, defaultBitmap, 0);
    }

    /**
     * Load image from local cache. If the image is in local cache, return the
     * image. Otherwise, return null. The method is time consuming, so do NOT
     * call it from synchronized methods.
     */
    public Bitmap syncLoadLocalImage(Image image, boolean fetchRemote) {
        return decodeBitmap(loadImage(image, fetchRemote));
    }

    private ImageLoader(Context context) {
        mBitmapCache = new LruCache<Image, ImageLoader.BitmapHolder>(BITMAP_CACHE_SIZE);
        mPendingRequest = new ImageLoaderItemMap();
        mLatestRequest = new ConcurrentHashMap<ImageView, Image>();
        mUIHandler = new Handler(this);
        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_COUNT);
        mContext = context;
    }

    private void loadImage(ImageView view, Image image, Bitmap defaultBitmap, int defaultImageRes) {
        if (image != null && image.isValid()) {
            mLatestRequest.put(view, image);
            int loadState = loadCachedPhoto(view, image);
            LogUtil.d(TAG, "loadImage " + image.toString() + ", state:" + loadState);
            if (loadState == BitmapHolder.LOADED) {
                mLatestRequest.remove(view);
                return;
            }
            // if not loaded, bind to the default image first
            bindDefaultImage(view, defaultBitmap, defaultImageRes);
            mPendingRequest.add(image, view);
            // if needed, load now
            if (loadState == BitmapHolder.NEEDED) {
                requestLoading(image);
            }
        } else {
            bindDefaultImage(view, defaultBitmap, defaultImageRes);
        }
    }

    /**
     * Look up image in local cache. If find it, bind it to the view right now.
     * Otherwise, tell the caller that further loading is needed.
     */
    private int loadCachedPhoto(ImageView view, Image image) {
        synchronized (mBitmapCacheLock) {
            BitmapHolder holder = null;
            holder = mBitmapCache.get(image);
            if (holder != null && holder.mState == BitmapHolder.LOADED) {
                if (holder.mBitmapRef.get() == null) {
                    inflateBitmap(holder);
                }
                bindImage(view, holder.mBitmapRef.get(), image);
                return BitmapHolder.LOADED;
            } else if (holder != null) {
                return holder.mState;
            }
        }
        return BitmapHolder.NEEDED;
    }

    private void inflateBitmap(BitmapHolder holder) {
        try {
            if (holder.mBytes != null) {
                Bitmap bitmap = decodeBitmap(holder.mBytes);
                holder.mBitmapRef = new SoftReference<Bitmap>(bitmap);
                bitmap = null;
            } else {
                LogUtil.e(TAG, "The holder's bytes should not be null");
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (holder.mBitmapRef == null) {
                holder.mBitmapRef = new SoftReference<Bitmap>(null);
            }
        }
    }

    private Bitmap decodeBitmap(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, ops);

            // Calculate inSampleSize
            ops.inSampleSize = calculateInSampleSize(ops, Device.DISPLAY_WIDTH,
                    Device.DISPLAY_HEIGHT);

            // Decode bitmap with inSampleSize set
            ops.inJustDecodeBounds = false;
            ops.inPurgeable = true;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, ops);
        }
        return null;
    }

    private int calculateInSampleSize(BitmapFactory.Options opt, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = opt.outHeight;
        final int width = opt.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = height / reqHeight;
            final int widthRatio = width / reqWidth;
            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee a final image with both dimensions larger than or equal
            // to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        return inSampleSize;
    }

    private boolean requestLoading(Image image) {
        if (!mPauseLoading) {
            mExecutor.execute(new LoadImageRunnable(image));
            return true;
        }
        return false;
    }

    public void pauseLoading() {
        mPauseLoading = true;
    }

    public void resumeLoading() {
        mPauseLoading = false;
        Iterator<Image> iterator = mPendingRequest.iterator();
        while (iterator.hasNext()) {
            requestLoading(iterator.next());
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOADED: {
                final Image image = (Image) msg.obj;
                if (image == null)
                    return false;
                synchronized (mBitmapCacheLock) {
                    BitmapHolder holder = mBitmapCache.get(image);
                    // Check whether the holder is in the cache since the
                    // cached data maybe garbaged.
                    if (holder != null && holder.mState == BitmapHolder.LOADED) {
                        if (holder.mBitmapRef.get() == null) {
                            inflateBitmap(holder);
                        }

                        // find the views which need the image
                        ImageLoaderItem item = mPendingRequest.get(image);
                        if (item == null)
                            return true;
                        for (ImageView v : item.views) {
                            // image should be the latest request of the view
                            if (image.equals(mLatestRequest.get(v))) {
                                bindImage(v, holder.mBitmapRef.get(), image);
                                mLatestRequest.remove(v);
                                LogUtil.d(TAG, "handleMessage: view " + v.hashCode() +
                                        " bind to " + image.toString());
                            }
                        }
                        mPendingRequest.remove(image);
                    } else {
                        // If the holder object is garbaged, reload the image.
                        LogUtil.d(TAG, "handleMessage:image " + image + " was garbaged");
                        requestLoading(image);
                    }
                }
                break;
            }
            default:
                break;
        }
        return false;
    }

    private class LoadImageRunnable implements Runnable {
        private Image image;

        public LoadImageRunnable(Image image) {
            this.image = image;
        }

        @Override
        public void run() {
            synchronized (mBitmapCacheLock) {
                // 如果该图片已经在加载中或者加载完毕，那么直接返回
                BitmapHolder holder = mBitmapCache.get(image);
                if (holder != null && holder.mState != BitmapHolder.NEEDED) {
                    return;
                }

                // 如果该图片尚未开始加载，那么把它加入正在加载队列
                if (holder == null) {
                    holder = new BitmapHolder();
                }
                holder.mState = BitmapHolder.LOADING;
                mBitmapCache.put(image, holder);
                LogUtil.d(TAG, "RunnableLoadImage:" + image.toString() + " cached to be loaded");
            }

            // 加载图片或到Cache中取，或到网络上抓取
            byte[] bitmapData = loadImage(image, true);
            synchronized (mBitmapCacheLock) {
                BitmapHolder holder = mBitmapCache.get(image);
                if (holder != null) {
                    if (bitmapData != null) {
                        holder.mState = BitmapHolder.LOADED;
                        holder.mBytes = bitmapData;
                        inflateBitmap(holder);
                        LogUtil.d(TAG, "LoadImageRunnable:" + image.toString() + " was loaded");
                    } else {
                        holder.mState = BitmapHolder.NEEDED;
                        LogUtil.e(TAG, "LoadImageRunnable:" + image.toString() + " load error");
                    }
                } else {
                    holder = new BitmapHolder();
                    if (bitmapData != null) {
                        holder.mState = BitmapHolder.LOADED;
                        holder.mBytes = bitmapData;
                        inflateBitmap(holder);
                        LogUtil.d(TAG, "LoadImageRunnable:" + image.toString() + " was loaded");
                    } else {
                        holder.mState = BitmapHolder.NEEDED;
                        LogUtil.e(TAG, "LoadImageRunnable:" + image.toString() + " load error");
                    }
                }
            }

            // 加载完毕，通知UI线程绑定与imageToLoad图片相关的View
            if (bitmapData != null) {
                Message msg = mUIHandler.obtainMessage(MESSAGE_REQUEST_LOADED);
                msg.obj = image;
                mUIHandler.sendMessage(msg);
            }
        }
    };

    private void bindImage(ImageView view, Bitmap bitmap, Image image) {
        if (view != null && bitmap != null && image != null) {
            if (view instanceof SelfBindView) {
                ((SelfBindView) view).SelfBindViewCallBack.bindView(view, bitmap, image);
            } else {
                if (view instanceof ZoomImageView) {
                    ((ZoomImageView) view).setImageBitmapResetBase(bitmap, true);
                } else {
                    view.setImageBitmap(image.proccessImage(bitmap));
                }
            }
        }
    }

    private void bindDefaultImage(ImageView view, Bitmap defaultBitmap, int defaultResId) {
        if (view instanceof ZoomImageView) {
            Bitmap bitmap = defaultBitmap;
            if (bitmap == null && defaultResId != 0) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), defaultResId);
            }
            ((ZoomImageView) view).setImageBitmapResetBase(bitmap, true);
        } else {
            if (defaultBitmap != null) {
                view.setImageBitmap(defaultBitmap);
            } else if (defaultResId != 0) {
                view.setImageResource(defaultResId);
            } else {
                view.setImageBitmap(null);
            }
        }
    }

    private byte[] loadImage(Image image, boolean fetchRemote) {
        final String cacheFileName = Coder.encodeSHA(image.getFileUrl());
        final File file = new File(mContext.getCacheDir() + File.separator
                + cacheFileName);
        FileOutputStream os = null;
        ByteArrayOutputStream bos = null;
        FileInputStream is = null;

        try {
            bos = new ByteArrayOutputStream();
            if (!file.exists() && fetchRemote) {
                RequestStream rs = new RequestStream(image.getFileUrl());
                if (rs.requestStream(bos) != Request.STATUS_OK) {
                    return null;
                }

                byte[] buffer = bos.toByteArray();
                if (buffer != null && buffer.length > 0) {
                    os = new FileOutputStream(file);
                    os.write(buffer);
                    os.flush();
                    return buffer;
                }
            } else if (file.exists()) {
                is = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }

                if (bos.size() > 0) {
                    return bos.toByteArray();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
