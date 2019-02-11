package com.mazhangjing.cloud.tuchuang.web;

import com.mazhangjing.cloud.tuchuang.oss.OSSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final OSSUtils utils;

    @Autowired
    public HomeController(OSSUtils utils) {
        this.utils = utils;
    }

    @PostMapping(value = "/api/upload")
    @ResponseBody
    public Map<String,String> uploadPicture(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        logger.info("文件 " + filename + " 正准备上传....");
        String uploadUrl = "";
        try {
            assert filename != null;
            if (!filename.isEmpty()) {
                File newFile = new File(filename);
                FileOutputStream os = new FileOutputStream(newFile);
                os.write(file.getBytes());
                os.close();
                file.transferTo(newFile);
                uploadUrl = utils.upload(newFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        HashMap<String, String> result = new HashMap<>();
        result.put("fileName",filename);
        result.put("url",uploadUrl);
        return result;
    }

}
