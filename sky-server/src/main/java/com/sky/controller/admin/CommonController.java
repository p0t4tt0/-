package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 上传图文，返回阿里云绝对路径
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public Result<String> upload(MultipartFile file)
    {
        log.info("文件上传 ：{}",file);

        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取后缀，最后一个”.“开始
            String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectname = UUID.randomUUID().toString() + substring;
            //文件请求路径
            String filepath = aliOssUtil.upload(file.getBytes(), objectname);

            return Result.success(filepath);
        } catch (IOException e) {
            log.info("文件上传失败 ：{}",e);
        }


        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
