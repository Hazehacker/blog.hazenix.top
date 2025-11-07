package top.hazenix.controller.web;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.hazenix.constant.MessageConstant;
import top.hazenix.exception.FailUploadException;
import top.hazenix.result.Result;
import top.hazenix.utils.AliOssUtil;

import java.io.IOException;

@RestController
@RequestMapping("/common")
@Slf4j
@RequiredArgsConstructor
public class WebController {

    private final AliOssUtil aliOssUtil;


    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        log.info("文件上传：{}",file.getOriginalFilename());
        //将文件交给OSS存储管理
        String url;
        try {
            url = aliOssUtil.upload(file.getBytes(),file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            throw new FailUploadException(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success(url);
    }

}
