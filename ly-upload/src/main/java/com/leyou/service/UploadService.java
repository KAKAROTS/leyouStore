package com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.exception.LyException;
import com.leyou.config.UploadProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private UploadProperties uploadProperties;
    @Autowired
    private FastFileStorageClient storageClient;//fastdfs上传客户端
    @Autowired
    private ThumbImageConfig thumbImageConfig;//缩略图配置
    public String upload(MultipartFile file) throws IOException {
        //先校验文件的mime类型
        if(!uploadProperties.getAllowContentTypes().contains(file.getContentType())){
            throw new LyException("请上传正确的文件", HttpStatus.BAD_REQUEST);
        }
        //再校验图片的内容，要交验图片内容就要读取图片，看是返回值是否为空
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image==null){
            throw new LyException("图片受损",HttpStatus.BAD_REQUEST);
        }
//将文件写入到本地磁盘
//        File dir = new File(uploadProperties.getLocalPath());
//        if(!dir.exists()){
//            dir.mkdirs();
//        }
//        String filename=new Date().getTime()+file.getOriginalFilename();
//        File file1 = new File(dir,filename);
//        file.transferTo(file1);
//        String url=file1.getPath();
        //改造上传功能，将文件写入到分布式文件管理系统
        //先获取文件的后缀名
        String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
        //调用fdfs文件上传客户端,上传图片及图片的缩略图,需要读取文件的流,文件扩展名，文件大小
        StorePath storePath = storageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(), extension, null);
        String thumbImagePath = thumbImageConfig.getThumbImagePath(storePath.getFullPath());
        System.out.println(storePath.getFullPath());
        System.out.println(storePath.getPath());
        System.out.println(thumbImagePath);
        String url=uploadProperties.getBaseUrl()+thumbImagePath;
        return url;
    }
}
