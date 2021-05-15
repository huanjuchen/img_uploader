package xyz.huanju.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HuanJu
 * @date 2020/8/5 21:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubConfig {

    private String apiUrl;

    private String owner;

    private String repo;

    private String path;

    private String token;

    /**
     * 时间戳命名
     */
    private Boolean tsName = false;

    private Boolean useGateway = false;

    private String gatewayHost;

    private Integer gatewayPort;

    private String gatewayPath;

    private Boolean gatewayHttps = false;


}
