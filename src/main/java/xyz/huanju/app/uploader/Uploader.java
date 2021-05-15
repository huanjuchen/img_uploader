package xyz.huanju.app.uploader;

/**
 * @author HuanJu
 * @date 2020/8/5 21:10
 */
public interface Uploader {


    /**
     * 上传
     *
     * @param fileUrl 文件路径
     * @return download_url
     */
    String upload(String fileUrl);
}
