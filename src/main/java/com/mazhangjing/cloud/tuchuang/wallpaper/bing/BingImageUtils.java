package com.mazhangjing.cloud.tuchuang.wallpaper.bing;

import com.alibaba.fastjson.JSON;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Some;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BingImageUtils {
    private static Logger logger = LoggerFactory.getLogger(BingImageUtils.class);
    private static String BASIC_HOST = "http://www.bing.com";
    private static String BASIC_URL = BASIC_HOST + "/HPImageArchive.aspx?format=js&idx=%s&n=1";

    private static String getURLRequest(int dayBeforeToday) {
        return String.format(BASIC_URL, dayBeforeToday);
    }

    public static BingRequestResult getBingRequestResult(int dayBeforeToday) throws IOException {
        String urlRequest = getURLRequest(dayBeforeToday);
        String result = Request.Get(urlRequest).execute().returnContent().asString();

        return JSON.parseObject(result, BingRequestResult.class);
    }

    public static Option<String> getBingTodayImageUrl(int dayBeforeToday) throws IOException {
        BingRequestResult bingRequestResult = getBingRequestResult(dayBeforeToday);
        List<BingRequestResultImage> images = bingRequestResult.images;
        if (images != null && images.size() > 0) {
            BingRequestResultImage image = images.get(0);
            return Some.apply(BASIC_HOST + image.url);
        } else return Option.empty();
    }

    public static Option<File> getImageFromFile(String url, File savedFile) throws IOException {
        InputStream is = Request.Get(url).execute().returnContent().asStream();
        if (savedFile.exists()) {
            boolean deleteResult = savedFile.delete();
            logger.info("File Exist, Try Delete now, delete Result: " + deleteResult);
            if (!deleteResult) {
                logger.info("Delete Failed, return now...");
                return Option.empty();
            }
        }
        boolean result = saveStreamToFile(is, savedFile);
        if (result) {
            return Some.apply(savedFile);
        } else return Option.empty();
    }


    private static Boolean saveStreamToFile(InputStream is, File savedFile) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(savedFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null) os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static class BingRequestResult {
        private List<BingRequestResultImage> images = new ArrayList<>();
        private Map<String, String> tooltips = new HashMap<>();

        public List<BingRequestResultImage> getImages() {
            return images;
        }

        public BingRequestResult setImages(List<BingRequestResultImage> images) {
            this.images = images;
            return this;
        }

        public Map<String, String> getTooltips() {
            return tooltips;
        }

        public BingRequestResult setTooltips(Map<String, String> tooltips) {
            this.tooltips = tooltips;
            return this;
        }

        public BingRequestResult() {
        }

        @Override
        public String toString() {
            return "BingRequestResult{" +
                    "images=" + images +
                    ", tooltips=" + tooltips +
                    '}';
        }
    }

    public static class BingRequestResultImage {
        private Integer startdate;
        private String fullstartdate;
        private Integer enddate;
        private String url;
        private String urlbase;
        private String copyright;
        private String copyrightlink;
        private String title;
        private String quiz;
        private Boolean wp;
        private String hsh;

        public BingRequestResultImage() {
        }

        @Override
        public String toString() {
            return "BingRequestResultImage{" +
                    "startdate=" + startdate +
                    ", fullstartdate='" + fullstartdate + '\'' +
                    ", enddate=" + enddate +
                    ", url='" + url + '\'' +
                    ", urlbase='" + urlbase + '\'' +
                    ", copyright='" + copyright + '\'' +
                    ", copyrightlink='" + copyrightlink + '\'' +
                    ", title='" + title + '\'' +
                    ", quiz='" + quiz + '\'' +
                    ", wp=" + wp +
                    ", hsh='" + hsh + '\'' +
                    '}';
        }

        public Integer getStartdate() {
            return startdate;
        }

        public BingRequestResultImage setStartdate(Integer startdate) {
            this.startdate = startdate;
            return this;
        }

        public String getFullstartdate() {
            return fullstartdate;
        }

        public BingRequestResultImage setFullstartdate(String fullstartdate) {
            this.fullstartdate = fullstartdate;
            return this;
        }

        public Integer getEnddate() {
            return enddate;
        }

        public BingRequestResultImage setEnddate(Integer enddate) {
            this.enddate = enddate;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public BingRequestResultImage setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getUrlbase() {
            return urlbase;
        }

        public BingRequestResultImage setUrlbase(String urlbase) {
            this.urlbase = urlbase;
            return this;
        }

        public String getCopyright() {
            return copyright;
        }

        public BingRequestResultImage setCopyright(String copyright) {
            this.copyright = copyright;
            return this;
        }

        public String getCopyrightlink() {
            return copyrightlink;
        }

        public BingRequestResultImage setCopyrightlink(String copyrightlink) {
            this.copyrightlink = copyrightlink;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public BingRequestResultImage setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getQuiz() {
            return quiz;
        }

        public BingRequestResultImage setQuiz(String quiz) {
            this.quiz = quiz;
            return this;
        }

        public Boolean getWp() {
            return wp;
        }

        public BingRequestResultImage setWp(Boolean wp) {
            this.wp = wp;
            return this;
        }

        public String getHsh() {
            return hsh;
        }

        public BingRequestResultImage setHsh(String hsh) {
            this.hsh = hsh;
            return this;
        }
    }
}
