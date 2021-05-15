package xyz.huanju.app.uploader.impl;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.huanju.app.domain.GithubConfig;
import xyz.huanju.app.domain.GithubRespBody;
import xyz.huanju.app.domain.UploadData;
import xyz.huanju.app.exception.UploaderException;
import xyz.huanju.app.uploader.Uploader;
import xyz.huanju.app.utils.JsonUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

/**
 * Github上传器
 * <p>
 * 配置名：github.properties
 * 路径: {user.home}/imgUploader/github.properties
 *
 * @author HuanJu
 * @date 2020/8/6 1:45
 */
@Slf4j
public class GithubUploaderImpl implements Uploader {

    private final OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * Git配置
     */
    private final GithubConfig config = new GithubConfig();

    private final String defaultGithubApi="https://api.github.com";

    private final String repoUrl="/repos";

    /**
     * 主上传方法
     *
     * @param fileUrl 文件路径
     * @return 返回download_url
     */
    @Override
    public String upload(String fileUrl) {
        fileUrl = fileUrl.replace('\\', '/');
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') == -1 ?
                0 : fileUrl.lastIndexOf('/') + 1);

        try {
            //1. 解析配置
            handleConfig();
            //2. 构建url
            String url = createUrl(fileName);
            log.debug("请求URL: {}",url);
            //3. 构建请求体
            String bodyStr = createRequestBody(fileUrl, fileName);
            //4. 构建请求
            Request request = new Request.Builder()
                    .header("Authorization", "token " + config.getToken())
                    .url(url)
                    .put(RequestBody.create(bodyStr.getBytes(StandardCharsets.UTF_8)))
                    .build();
            //5. 发送请求
            Response response = okHttpClient.newCall(request).execute();
            //6. 验证结果并返回

            if (response.body() != null) {
                if (response.code() == 201) {
                    GithubRespBody respBody = JsonUtils.toObject(response.body().string(),GithubRespBody.class);
                    if (respBody!=null){
                        return respBody.getContent().getDownload_url();
                    }else {
                        return null;
                    }
                } else {
                    String tempStr = response.body().string();
                    log.warn("请求发生错误, 响应体如下: " + tempStr);
                    return null;
                }
            } else {
                return null;
            }

        } catch (IOException | UploaderException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 处理配置
     */
    private void handleConfig() throws IOException {
        //处理文件地址
        String configFileUrl = System.getProperty("user.home") + "/imgUploader/github.properties";
        configFileUrl = configFileUrl.replace('\\', '/');
        InputStream is = null;
        try {
            /*
              加载文件输入流
             */
            Properties properties = new Properties();
            File file = new File(configFileUrl);
            if (!file.exists() || !file.canRead()) {
                throw new UploaderException("配置文件错误，文件不存在不存在");
            }
            is = new FileInputStream(file);
            properties.load(is);

            /*
            开始解析
             */
            String path = properties.getProperty("path");
            String owner = properties.getProperty("owner");
            String repo = properties.getProperty("repo");
            String token = properties.getProperty("token");
            Boolean tsName = Boolean.valueOf(properties.getProperty("tsName"));
            Boolean gatewayEnabled=Boolean.valueOf(properties.getProperty("gateway.enabled"));
            String gatewayHost=properties.getProperty("gateway.host");
            Integer gatewayPort=Integer.valueOf(properties.getProperty("gateway.port"));
            String gatewayPath=properties.getProperty("gateway.path");
            Boolean gatewayHttps=Boolean.valueOf(properties.getProperty("gateway.https"));
            config.setTsName(tsName);

            if (path != null && path.length() > 0) {
                config.setPath(path);
            }
            if (owner != null && owner.length() > 0) {
                config.setOwner(owner);
            } else {
                throw new UploaderException("配置文件错误，owner属性不存在");
            }
            if (repo != null && repo.length() > 0) {
                config.setRepo(repo);
            } else {
                throw new UploaderException("配置文件错误，repo属性不存在");
            }
            if (token != null && token.length() > 0) {
                config.setToken(token);
            } else {
                throw new UploaderException("配置文件错误，token属性不存在");
            }

            config.setUseGateway(gatewayEnabled);
            config.setGatewayHost(gatewayHost);
            config.setGatewayPort(gatewayPort);
            config.setGatewayPath(gatewayPath);
            config.setGatewayHttps(gatewayHttps);


        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    /**
     * 构建请求URL
     */
    private String createUrl(String fileName)                                                                                        {
        StringBuilder sb = new StringBuilder();
        String hostUrl = getHostUrl();
        sb.append(hostUrl);
        sb.append(repoUrl);
        sb.append('/');
        sb.append(config.getOwner());
        sb.append('/');
        sb.append(config.getRepo());
        sb.append("/contents/");
        if (config.getPath() != null) {
            sb.append(config.getPath());
        } else {
            sb.append(datePath());
        }
        /*
        时间戳命名处理
         */
        if (config.getTsName()) {
            sb.append(getTimestampNaming(fileName));
        } else {
            sb.append(fileName.replace(" ", ""));

        }
        return sb.toString();
    }


    private String getHostUrl(){
        if (config.getUseGateway()){
            StringBuilder sb=new StringBuilder();
            if (config.getGatewayHttps()){
                sb.append("https://");
            }else {
                sb.append("http://");
            }
            sb.append(config.getGatewayHost());
            sb.append(':');
            sb.append(config.getGatewayPort());
            sb.append(config.getGatewayPath());
            return sb.toString();
        }else {
            return defaultGithubApi;
        }
    }


    /**
     * 构建请求体
     */
    private String createRequestBody(String fileUrl, String fileName) throws IOException {
        Base64.Encoder encoder = Base64.getEncoder();
        /*
        try-with-resources
        要求resource实现 AutoCloseable接口
         */
        try (InputStream is = new FileInputStream(fileUrl)) {
            int len = is.available();
            byte[] bytes = new byte[len];
            int i;
            do {
                i = is.read(bytes);
            } while (i != -1);

            UploadData uploadData = new UploadData();
            uploadData.setMessage("upload " + fileName + " by img_uploader");
            uploadData.setContent(encoder.encodeToString(bytes));
            return JsonUtils.toJson(uploadData);
        }
    }


    private String datePath() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(date) + "/";
    }

    /**
     * 时间戳命名
     */
    private String getTimestampNaming(String fileName) {
        String fileSub = fileName.lastIndexOf('.') == -1 ?
                "" : fileName.substring(fileName.lastIndexOf('.'));
        return System.currentTimeMillis() + fileSub;
    }


}
