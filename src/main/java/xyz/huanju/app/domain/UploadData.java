package xyz.huanju.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HuanJu
 * @date 2020/8/5 21:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadData {

    /**
     * commit信息
     */
    private String message;

    /**
     * 上传的文件内容 Base64编码
     */
    private String content;

    /**
     * 分支
     */
    private String branch="master";


}
