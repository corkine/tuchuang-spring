package com.mazhangjing.cloud.tuchuang.wallpaper.baidu;

import com.baidu.aip.imageprocess.AipImageProcess;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

@Component
public class Enhance {

    public File callForEnhance(AipImageProcess client, File fromFile) throws IOException {
        String path = fromFile.getPath();
        // 上传
        HashMap<String, String> options = new HashMap<>();
        JSONObject res = client.imageQualityEnhance(path, options);
        // 解析
        String bsImage = res.getString("image");
        // 解码
        byte[] decode = Base64.getDecoder().decode(bsImage);
        // 写入文件
        String newName = fromFile.getPath().replace(".jpg","_enhanced.jpg");
        File newFile = new File(newName);
        FileOutputStream test = new FileOutputStream(newFile);
        test.write(decode);
        test.flush();
        test.close();
        return newFile;
    }
}
