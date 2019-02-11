# 小 🐴 图床 - 后端部分

## 概要

这是我写的一个很简单的图床站，从前端上传的图片被从服务器中转到阿里云 OSS 进行保存，返回一个网址，用于文章博客使用。

程序基于 Spring Boot 编写，OSS 使用了 Aliyun 的 OSS SDK，值得一提的是，OSS 使用了 Scala 编写服务逻辑，Scala 和 Spring IOC、MVC 集成以提供服务。总的来说，除了 Scala 的 Object 伴生对象无法很好的和 Spring 兼容以外，其余部分协作的非常好，Spring 可以直接注入 Bean 属性、字段到 Scala 的类中，这非常方便。

## Scala vs Java

下面是对等的 OSS Java 实现和 Scala 实现的一个对比（参考自 https://zhuanlan.zhihu.com/p/51681183?utm_source=ZHShareTargetIDMore&utm_medium=social&utm_oi=31852001755136）：

```java
//AliyunOSSConfigConstant.java
public class AliyunOSSConfigConstant {
    //私有构造方法 禁止该类初始化
    private AliyunOSSConfigConstant() {
    }

    //仓库名称
    public static final String BUCKE_NAME = "my-blog-to-use";
    //地域节点
    public static final String END_POINT = "oss-cn-beijing.aliyuncs.com";
    //AccessKey ID
    public static final String AccessKey_ID = "你的AccessKeyID";
    //Access Key Secret
    public static final String AccessKey_Secret = "你的AccessKeySecret";
    //仓库中的某个文件夹
    public static final String FILE_HOST = "test";
}
```

或者采用配置方式：

```properties
#OSS配置
aliyun.oss.bucketname=my-blog-to-use
aliyun.oss.endpoint=oss-cn-beijing.aliyuncs.com
#阿里云主账号AccessKey拥有所有API的访问权限，风险很高。建议创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
aliyun.oss.keyid=你的AccessKeyID
aliyun.oss.keysecret=你的AccessKeySecret
aliyun.oss.filehost=test
```
```java
@Component
@PropertySource(value = "classpath:application-oss.properties")
@ConfigurationProperties(prefix = "aliyun.oss")
/**
 * 阿里云oss的配置类
 */
public class AliyunOSSConfig {
    private String bucketname;
    private String endpoint;
    private String keyid;
    private String keysecret;
    private String filehost;
    ...
    此处省略getter、setter以及 toString方法
}

@Component
public class AliyunOSSUtil {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AliyunOSSUtil.class);
    private static String FILE_URL;
    private static String bucketName = AliyunOSSConfigConstant.BUCKE_NAME;
    private static String endpoint = AliyunOSSConfigConstant.END_POINT;
    private static String accessKeyId = AliyunOSSConfigConstant.AccessKey_ID;
    private static String accessKeySecret = AliyunOSSConfigConstant.AccessKey_Secret;
    private static String fileHost = AliyunOSSConfigConstant.FILE_HOST;

    /**
     * 上传文件。
     *
     * @param file 需要上传的文件路径
     * @return 如果上传的文件是图片的话，会返回图片的"URL"，如果非图片的话会返回"非图片，不可预览。文件路径为：+文件路径"
     */
    public static String upLoad(File file) {
        // 默认值为：true
        boolean isImage = true;
        // 判断所要上传的图片是否是图片，图片可以预览，其他文件不提供通过URL预览
        try {
            Image image = ImageIO.read(file);
            isImage = image == null ? false : true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("------OSS文件上传开始--------" + file.getName());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(new Date());

        // 判断文件
        if (file == null) {
            return null;
        }
        // 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        try {
            // 判断容器是否存在,不存在就创建
            if (!ossClient.doesBucketExist(bucketName)) {
                ossClient.createBucket(bucketName);
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(createBucketRequest);
            }
            // 设置文件路径和名称
            String fileUrl = fileHost + "/" + (dateStr + "/" + UUID.randomUUID().toString().replace("-", "") + "-" + file.getName());
            if (isImage) {//如果是图片，则图片的URL为：....
                FILE_URL = "https://" + bucketName + "." + endpoint + "/" + fileUrl;
            } else {
                FILE_URL = "非图片，不可预览。文件路径为：" + fileUrl;
            }

            // 上传文件
            PutObjectResult result = ossClient.putObject(new PutObjectRequest(bucketName, fileUrl, file));
            // 设置权限(公开读)
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            if (result != null) {
                logger.info("------OSS文件上传成功------" + fileUrl);
            }
        } catch (OSSException oe) {
            logger.error(oe.getMessage());
        } catch (ClientException ce) {
            logger.error(ce.getErrorMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return FILE_URL;
    }


    /**
     * 通过文件名下载文件
     *
     * @param objectName    要下载的文件名
     * @param localFileName 本地要创建的文件名
     */
    public static void downloadFile(String objectName, String localFileName) {

        // 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
        ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(localFileName));
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 列举 test 文件下所有的文件
     */
    public static void listFile() {
        // 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        // 构造ListObjectsRequest请求。
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);

        // 设置prefix参数来获取fun目录下的所有文件。
        listObjectsRequest.setPrefix("test/");
        // 列出文件。
        ObjectListing listing = ossClient.listObjects(listObjectsRequest);
        // 遍历所有文件。
        System.out.println("Objects:");
        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
        }
        // 遍历所有commonPrefix。
        System.out.println("CommonPrefixes:");
        for (String commonPrefix : listing.getCommonPrefixes()) {
            System.out.println(commonPrefix);
        }
        // 关闭OSSClient。
        ossClient.shutdown();
    }
}
```

对于 Scala 而言，虽然这个小项目没有用到什么 Scala 的杀手技巧，比如 implicitly 和 match...case，FP 处理集合等等，但是就一般逻辑而言，使用 Scala 写比 Java 舒服很多。

```scala
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
```

因为是 IOC，因此 Scala 的 object 对象和 class 区别不大，都是单例。


## 程序运行和部署

![](http://static2.mazhangjing.com/20190211/fe491b6_cm_image2019-02-1119.58.50.png)

> 这个仓库已经包含了前端打包后的部分，如果需要前端实现代码（Vue实现），参考 https://github.com/corkine/cmBed_Vue 这个仓库即可。

> 部署指南：和众多 Spring Boot 程序一样，直接 java -jar xxx.jar 运行，你可以修改 resources/application.yml 的 OSS 属性，放在外面覆盖内部配置。