
package com.xiaomi.xms.sales.request;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.util.Coder;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

public class RequestStream extends Request {
    private static final String TAG = "RequestStream";

    private static final int BUFFER_SIZE = 1024;
    private static final String TEMP_DOWNLOADED_FILE_NAME_FORMAT = "%s_temp_downloaded";

    private static final String CONN_RANGE_VALUE_FORMAT = "bytes=%d-";
    private static final String CONN_RANGE_PROPERTY = "RANGE";

    public RequestStream(String url) {
        super(url);
        setHttpMethod(HttpGet.METHOD_NAME);
    }

    public int requestStream(OutputStream outPutStream) {
        if (outPutStream == null) {
            return STATUS_PARAM_ERROR;
        }

        if (!Utils.Network.isNetWorkConnected(ShopApp.getContext())) {
            return STATUS_NETWORK_UNAVAILABLE;
        }

        int status = STATUS_CLIENT_ERROR;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;

        try {
            conn = super.getConn();
            if (conn != null) {
                baos = null; // getTempDownloadData();
                // 如果该文件下载过，那么进行断点下载，从已下载文件的末尾继续下载
                if (baos != null) {
                    conn.addRequestProperty(CONN_RANGE_PROPERTY,
                            String.format(CONN_RANGE_VALUE_FORMAT, baos.size()));
                } else {
                    baos = new ByteArrayOutputStream();
                }

                conn.connect();
                int responseCode = conn.getResponseCode();
                LogUtil.d(TAG, "The response code is:" + responseCode);
                if (responseCode == HttpStatus.SC_OK
                        || responseCode == HttpStatus.SC_PARTIAL_CONTENT) {
                    inputStream = conn.getInputStream();

                    byte[] buff = new byte[BUFFER_SIZE];
                    int len = 0;
                    while ((len = inputStream.read(buff)) != -1) {
                        baos.write(buff, 0, len);
                    }
                    outPutStream.write(baos.toByteArray());
                    status = STATUS_OK;
                } else if (isServerError(responseCode)) {
                    if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                        LoginManager.getInstance().invalidAuthToken();
                        status = STATUS_AUTH_ERROR;
                    } else {
                        status = STATUS_SERVER_ERROR;
                    }
                } else {
                    status = STATUS_UNKNOWN_ERROR;
                }
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            status = STATUS_SERVICE_UNAVAILABLE;
        } catch (IOException e) {
            e.printStackTrace();
            if (!Utils.Network.isNetWorkConnected(ShopApp.getContext())) {
                status = STATUS_NETWORK_UNAVAILABLE;
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (status != STATUS_OK && baos != null && baos.size() > 0) {
                saveTemporaryDownloadedData(baos);
            }

            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return status;
    }

    /**
     * 将已经下载的文件存储到Cache中供再次下载使用。文件名以File的SHA1命名。
     */
    private void saveTemporaryDownloadedData(ByteArrayOutputStream out) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(ShopApp.getContext().getCacheDir()
                    + File.separator
                    + String.format(TEMP_DOWNLOADED_FILE_NAME_FORMAT,
                            Coder.encodeSHA(getRequestUrl())));
            outputStream.write(out.toByteArray());
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
    }

    /**
     * 检查Cache中是否存在待下载文件的临时文件，该文件可能是某次下载已完成的部分，以文件的SHA1命名。
     * 这个文件用来支持断点续传。为了减少Cache的占用，每次读取后删除该文件。
     */
    private ByteArrayOutputStream getTempDownloadData() {
        File file = new File(ShopApp.getContext().getCacheDir() + File.separator
                + String.format(TEMP_DOWNLOADED_FILE_NAME_FORMAT, Coder.encodeSHA(getRequestUrl())));

        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
                outputStream = new ByteArrayOutputStream();
                byte[] buff = new byte[BUFFER_SIZE];
                int len = 0;
                while ((len = inputStream.read(buff)) != -1) {
                    outputStream.write(buff, 0, len);
                }
                outputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 删除临时文件
                file.delete();
            }
        }
        return outputStream;
    }
}
