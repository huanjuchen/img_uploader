package xyz.huanju.app;

import xyz.huanju.app.uploader.Uploader;
import xyz.huanju.app.uploader.impl.GithubUploaderImpl;


/**
 * @author HuanJu
 * @date 2020/8/5 19:53
 */
public class Main {

    public static void main(String[] args){
        int len = args.length;
        int lastIndex = len - 1;
        Uploader uploader = new GithubUploaderImpl();

        for (int i = 0; i < len; i++) {
            String s = uploader.upload(args[i]);
            if (s != null && s.length() > 0) {
                /*
                上传成功
                在第一次时打印Upload Success
                 */
                if (i==0){
                    System.out.println("Upload Success:");
                }
                /*
                打印出download URL
                 */
                if (i != lastIndex) {
                    System.out.println(s);
                } else {
                    System.out.print(s);
                }
            }

        }
    }


}
