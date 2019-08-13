package com.mazhangjing.cloud.tuchuang;

import com.aliyun.oss.model.OSSObjectSummary;
import com.mazhangjing.cloud.tuchuang.oss.OSSConfig;
import com.mazhangjing.cloud.tuchuang.oss.OSSUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TuchuangApplicationTests {
	@Autowired OSSConfig config;
	@Autowired OSSUtils utils;

	@Test
	public void contextLoads() {
		System.out.println(config);
		System.out.println(utils.config());
		assert (config != null && utils != null);
	}

	@Test public void testUploadAndDownload() throws InterruptedException {
		assert (utils.upload(new File(UUID.randomUUID().toString())) == null);
		String result = utils.upload(new File("src/test/akka_vote.png"));
		System.out.println("Result is " + result);
		assert (result != null);
		String fileName = result.replace(config.getFileHost() + "/","");
		System.out.println("fileName = " + fileName);
		TimeUnit.SECONDS.sleep(3);
		boolean downloadRes = utils.downloadFile(fileName,
				"src/test/" + UUID.randomUUID().toString() + ".png");
		assert downloadRes;
		TimeUnit.SECONDS.sleep(3);
		assert (!utils.downloadFile(UUID.randomUUID().toString(), "MayNotExist"));
	}

	@Test public void testListFile(){
		List<OSSObjectSummary> ossObjectSummaries = utils.listFile("");
		ossObjectSummaries.forEach(item -> {
			System.out.println("item.getKey() = " + item.getKey());
			System.out.println("item.getLastModified() = " + item.getLastModified());
			System.out.println("item.getSize() = " + item.getSize());
		});
		assert (!ossObjectSummaries.isEmpty());
		List<String> folders = utils.listFolder("");
		System.out.println("folders = " + folders);
	}

}

