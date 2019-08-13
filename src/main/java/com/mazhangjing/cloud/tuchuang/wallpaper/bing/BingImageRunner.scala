package com.mazhangjing.cloud.tuchuang.wallpaper.bing

import java.nio.file.Paths
import org.slf4j.LoggerFactory

object BingImageRunner {

  private val logger = LoggerFactory.getLogger("Runner")

  private val TEMP_FILENAME = "today_bing_image.jpeg"

  def Try[T](op: => Option[T]): Option[T] =
    try {
      op
    } catch {
      case e: Throwable => logger.warn(e.getMessage); None
    }

  def main(args: Array[String]): Unit = {
    println("===" * 10)
    println("Usage: java -jar xxx.jar 0 today_bing_image.jpeg, \n第一个参数指定日期，昨天为 1， 前天为 2，以此类推，第二个参数指定文件保存位置。" +
      "\n类所在位置：com.mazhangjing.wallpaper.BingImageRunner")
    println("===" * 10)
    args match {
      case Array(indexString, pathString, _*) =>
        logger.info(s"For Index: $indexString, Path: $pathString, Init Download Sequence...")
        Try(Some(indexString.toInt)) match {
          case None => doSaveFile(0, pathString)
          case Some(index) => doSaveFile(index, pathString)
        }
      case _ =>
        logger.info(s"No Arguments,Use Default Arg(index: 0, fileName: $TEMP_FILENAME) to init Download Sequence...")
        doSaveFile(0, TEMP_FILENAME)
    }
  }

  def doSaveFile(index: Int, fileName: String): Unit = {
    Try {
      BingImageUtils.getBingTodayImageUrl(index)
    } match {
      case None => logger.warn("Can't Resolve URL")
      case Some(url) =>
        logger.info(s"Get File From URL: $url now...")
        Try {
          BingImageUtils.getImageFromFile(url, Paths.get(fileName).toFile)
        } match {
          case None => logger.warn("Can't Save File From URL")
          case Some(_) => logger.info(s"Saved File to $fileName")
        }
    }
  }
}

