package com.mazhangjing.cloud.tuchuang.oss

import java.io.{File, PrintWriter, StringWriter}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util

import com.aliyun.oss.model.OSSObjectSummary
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class OSSUtils {

  @Autowired var config:OSSConfig = _
  private val logger = LoggerFactory.getLogger(classOf[OSSUtils])

  import java.util.UUID

  import com.aliyun.oss.model.{CannedAccessControlList, CreateBucketRequest, PutObjectRequest}

  def upload(file: File):String = {

    val client = config.getClientInstance
    val time = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
    val filePath = s"$time/${UUID.randomUUID().toString.replace("-","").substring(0,7)}"
    val fileUrl = filePath + s"_${file.getName.replace(" ","")}"

    try {
      logger.info(s"OSS 文件上传开始， File:${file.getName}")

      if (!client.doesBucketExist(config.getBucketName)) {
        client.createBucket(config.getBucketName)
        val request = new CreateBucketRequest(config.getBucketName)
        request.setCannedACL(CannedAccessControlList.PublicRead)
        client.createBucket(request)
      }
      val putRequest = new PutObjectRequest(config.getBucketName, fileUrl, file)
      val putResult = client.putObject(putRequest)
      client.setBucketAcl(config.getBucketName, CannedAccessControlList.PublicRead)
      putResult match {
        case null => logger.info(s"$file - $fileUrl - 文件上传失败 - $putResult")
        case _ =>
          logger.info(s"$file - $fileUrl - 文件上传成功")
          return config.getFileHost + "/" + fileUrl
      }
    } catch {
        case ex : Exception =>
          val w = new StringWriter()
          ex.printStackTrace(new PrintWriter(w))
          logger.info(ex.getMessage + ", Details: \n" + w.toString)
    } finally {
      if (client != null) client.shutdown()
    }
    null
  }

  import com.aliyun.oss.model.GetObjectRequest

  def downloadFile(objectName: String, localFileName: String): Boolean = {
    try {
      val ossClient = config.getClientInstance
      ossClient.getObject(new GetObjectRequest(config.getBucketName, objectName), new File(localFileName))
      ossClient.shutdown()
      return true
    } catch {
      case ex :Exception =>
        logger.info(s"DownLoad File Error: ${ex.getMessage}")
    }
    false
  }

  import com.aliyun.oss.model.ListObjectsRequest

  def listFile(prefix:String = ""): util.List[OSSObjectSummary] = {
    val ossClient = config.getClientInstance
    val listObjectsRequest = new ListObjectsRequest(config.getBucketName)
    if (prefix != "") listObjectsRequest.setPrefix(prefix)
    val listing = ossClient.listObjects(listObjectsRequest)

    val listWithOutFolders = listing.getObjectSummaries
    ossClient.shutdown()
    listWithOutFolders
  }

  def listFolder(prefix:String = ""): util.List[String] = {
    val ossClient = config.getClientInstance
    val listObjectsRequest = new ListObjectsRequest(config.getBucketName)
    if (prefix != "") listObjectsRequest.setPrefix(prefix)
    val listing = ossClient.listObjects(listObjectsRequest)

    val listJustFolders = listing.getCommonPrefixes
    ossClient.shutdown()
    listJustFolders
  }
}