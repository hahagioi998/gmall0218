package com.atguigu.gmall0218.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;

import java.io.IOException;

/**
 * @author qiyu
 * @create 2019-07-26 15:01
 * @Description:TODO(这里用一句话来描述这个类的作用)
 */
public class textFileTest {
    @Test
    public void textFileUpload() throws IOException, MyException {
        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer=trackerClient.getConnection();
        StorageClient storageClient=new StorageClient(trackerServer,null);
        String orginalFilename="E:/电商/0218/07_尚硅谷JAVAEE技术_电商项目/资料/day 04/fdfs/001.jpg";
        String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println("s = " + s);
        }
    }
}
