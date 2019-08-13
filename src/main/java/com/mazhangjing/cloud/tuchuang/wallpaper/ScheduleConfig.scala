package com.mazhangjing.cloud.tuchuang.wallpaper

import java.io.File
import java.nio.file.Paths
import java.time.LocalDate

import com.baidu.aip.imageprocess.AipImageProcess
import com.mazhangjing.cloud.tuchuang.oss.OSSUtils
import com.mazhangjing.cloud.tuchuang.wallpaper.baidu.{BaiduAuthConfig, Enhance}
import com.mazhangjing.cloud.tuchuang.wallpaper.bing.BingImageUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.scheduling.annotation.{EnableAsync, EnableScheduling, Scheduled}

@Configuration
@EnableAsync
@EnableScheduling
class ScheduleConfig {

  @Autowired var utils: OSSUtils = _

  @Autowired var baiduAuth: BaiduAuthConfig = _

  @Bean def baiduClient: AipImageProcess = {
    assert(baiduAuth != null)
    val c = new AipImageProcess(baiduAuth.getAppId, baiduAuth.getApiKey, baiduAuth.getSecureKey)
    c.setConnectionTimeoutInMillis(2000)
    c.setSocketTimeoutInMillis(60000)
    c
  }

  @Autowired var client: AipImageProcess = _

  @Autowired var enhance: Enhance = _

  private val logger = LoggerFactory.getLogger(this.getClass)

  var lastUpdate: LocalDate = _

  var lastUrl: (String,String) = _

  val tempFileName = "temp_wallpaper.jpg"

  def getInformation: Option[(LocalDate, (String,String))] = {
    if (lastUpdate == null || lastUrl == null) None
    else Option((lastUpdate, lastUrl))
  }

  //todo 更改时间
  @Scheduled(initialDelay = 10000, fixedDelay = 600000) def wallPaperDownloadChecker(): Unit = {
    logger.info("Checking Schedule now...")
    //最后更新日期如果为空，或者不是今天，那么则更新，否则不更新
    if (lastUpdate == null ||
      !lastUpdate.isEqual(LocalDate.now())) {
      logger.info("Updating Information now...")
      lastUrl = handleImageUrlGetter(0, Paths.get(tempFileName).toFile)
      lastUpdate = LocalDate.now()
    } else {
      logger.debug("Nothing to Update!")
    }
  }

  def handleImageUrlGetter(today:Int, tempFile:File): (String,String) = {
    try {
      logger.info(s"Get Image and Save to LocalFile $tempFile now...")
      val bingUrl = BingImageUtils.getBingTodayImageUrl(today).get
      val original = BingImageUtils.getImageFromFile(bingUrl, tempFile).get

      logger.info("Enhance Photo now...")
      val enhanced = enhance.callForEnhance(client, original)

      logger.info("Upload Image to OSS now...")
      val ossOriginalUrl = {
        val t = utils.upload(original)
        if (t == null) throw new RuntimeException("上传失败")
        else t
      }
      val oasEnhancedUrl = {
        val t = utils.upload(enhanced)
        if (t == null) throw new RuntimeException("上传增强照片失败")
        else t
      }
      (ossOriginalUrl, oasEnhancedUrl)
    } catch {
      case e: Throwable =>
        logger.warn("在获取照片地址时出错：" + e.getMessage)
        null
    }
  }

}