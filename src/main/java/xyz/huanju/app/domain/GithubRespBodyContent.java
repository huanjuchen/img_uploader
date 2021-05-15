package xyz.huanju.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HuanJu
 * @date 2020/8/6 3:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRespBodyContent {

    private String name;

    private String path;

    private Integer size;

    private String url;

    private String download_url;

    private String type;

}
