package com.mazhangjing.cloud.tuchuang.web;

import com.mazhangjing.cloud.tuchuang.oss.OSSUtils;
import com.mazhangjing.cloud.tuchuang.wallpaper.ScheduleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final OSSUtils utils;

    private final ScheduleConfig scheduleConfig;

    @Autowired
    public HomeController(OSSUtils utils, ScheduleConfig scheduleConfig) {
        this.utils = utils;
        this.scheduleConfig = scheduleConfig;
    }

    @GetMapping(value = "/api/wallpaper")
    @ResponseBody
    public Map<String,String> wallpaper() {
        HashMap<String, String> res = new HashMap<>();
        if (scheduleConfig.getInformation().isEmpty()) {
            res.put("status","404");
            res.put("reason","不存在这样的资源");
            return res;
        } else {
            Tuple2<LocalDate, Tuple2<String, String>> inf = scheduleConfig.getInformation().get();
            LocalDate localDate = inf._1();
            Tuple2<String, String> urls = inf._2();
            String normal = urls._1();
            String enhance = urls._2();
            res.put("status","200");
            res.put("reason","");
            res.put("update", String.valueOf(localDate));
            res.put("normal", normal);
            res.put("enhance", enhance);
            return res;
        }
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
