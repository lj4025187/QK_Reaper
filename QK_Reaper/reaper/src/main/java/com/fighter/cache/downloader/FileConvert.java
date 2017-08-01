package com.fighter.cache.downloader;

import android.os.Environment;
import android.text.TextUtils;

import com.fighter.common.utils.CloseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Response;

public class FileConvert {
    private static final String DM_TARGET_FOLDER =
            File.separator + "download" + File.separator;   // 下载目标文件夹

    private static final long REFRESH_TIME = 200;

    private String mDestFileDir;    // 目标文件存储的文件夹路径
    private String mDestFileName;   // 目标文件存储的文件名
    private boolean mKeepExtension;  // 是否保留原扩展名

    public FileConvert() {
        this(null);
    }

    public FileConvert(String destFileName) {
        this(Environment.getExternalStorageDirectory() + DM_TARGET_FOLDER, destFileName);
    }

    public FileConvert(String destFileDir, String destFileName) {
        this(destFileDir, destFileName, false);
    }

    public FileConvert(String destFileDir, String destFileName, boolean keepExtension) {
        mDestFileDir = destFileDir;
        mDestFileName = destFileName;
        mKeepExtension = keepExtension;
    }

    public File convert(Response value) throws Exception {
        if (TextUtils.isEmpty(mDestFileDir)) {
            mDestFileDir = Environment.getExternalStorageDirectory() + DM_TARGET_FOLDER;
        }
        if (TextUtils.isEmpty(mDestFileName)) {
            mDestFileName = getNetFileName(value, value.request().url().toString());
        } else if (mKeepExtension) {
            String mimeType = value.header("Content-Type");
            mDestFileName = addExtension(mDestFileName, mimeType);
        }

        File dir = new File(mDestFileDir);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, mDestFileName);
        if (file.exists()) file.delete();

        long lastRefreshUiTime = 0;  //最后一次刷新的时间
        long lastWriteBytes = 0;     //最后一次写入字节数据

        InputStream is = null;
        byte[] buf = new byte[2048];
        FileOutputStream fos = null;
        try {
            is = value.body().byteStream();
            final long total = value.body().contentLength();
            long sum = 0;
            int len;
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
            }
            fos.flush();
            return file;
        } finally {
            CloseUtils.closeIOQuietly(is, fos);
        }
    }

    /**
     * 根据响应头或者url获取文件名
     */
    public static String getNetFileName(Response response, String url) {
        String fileName = getHeaderFileName(response);
        if (TextUtils.isEmpty(fileName)) fileName = getUrlFileName(url, response);
        if (TextUtils.isEmpty(fileName)) fileName = "nofilename";
        return fileName;
    }

    /**
     * 解析文件头 Content-Disposition:attachment;filename=FileName.txt
     */
    private static String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (dispositionHeader != null) {
            String split = "filename=";
            int indexOf = dispositionHeader.indexOf(split);
            if (indexOf != -1) {
                String fileName = dispositionHeader.substring(indexOf + split.length(), dispositionHeader.length());
                fileName = fileName.replaceAll("\"", "");   //文件名可能包含双引号,需要去除
                return fileName;
            }
        }
        return null;
    }

    /**
     * 通过 ‘？’ 和 ‘/’ 判断文件名
     */
    private static String getUrlFileName(String url, Response response) {
        int index = url.lastIndexOf('?');
        String fileName;
        if (index > 1) {
            fileName = url.substring(url.lastIndexOf('/') + 1, index);
        } else {
            fileName = url.substring(url.lastIndexOf('/') + 1);
        }

        if (!fileName.contains(".")) {
            String mimeType = response.header("Content-Type");
            fileName = addExtension(fileName, mimeType);
        }
        return fileName;
    }

    private static String addExtension(String fileName, String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return fileName;
        }
        MimeType type = MimeTypes.getInstance().getByType(mimeType);
        if (type != null) {
            String extension = type.getExtension();
            fileName = TextUtils.isEmpty(extension) ?
                    fileName : fileName + "." + extension;
        }
        return fileName;
    }
}
