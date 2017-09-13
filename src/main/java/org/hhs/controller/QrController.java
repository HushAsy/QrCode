package org.hhs.controller;

import lombok.extern.slf4j.Slf4j;
import org.hhs.domain.QrCodeInfo;
import org.hhs.qrcode.QrCode;
import org.hhs.service.GeneratorImage;
import org.hhs.util.ZipCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * Created by admin on 2017/9/7.
 */
@Controller
@Slf4j
public class QrController {
    private Logger logger = LoggerFactory.getLogger(QrController.class);
    @Autowired
    private GeneratorImage generatorImage;
    @RequestMapping("work")
    public void getQrcode(@RequestParam("file") MultipartFile mfile, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!mfile.isEmpty()){
            InputStream inputStream = mfile.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            List<QrCodeInfo> qrCodeInfoList = generatorImage.getQrInfoList(new BufferedReader(inputStreamReader));
            for(int i = 0; i < qrCodeInfoList.size()/10; i++){
                generatorImage.generatorOne(qrCodeInfoList.subList(i*10, 10*(i+1)), "erweima" + i+".jpg");
            }

            int left = qrCodeInfoList.size()%10;
            int mod = qrCodeInfoList.size()/10;
            if(qrCodeInfoList.size()%10 != 0){
                generatorImage.generatorOne(qrCodeInfoList.subList(mod*10, 10*mod+left), "erweima" + mod+".jpg");
            }
            logger.info("图片生成完毕");
        }
    }

    @RequestMapping("done")
    public void getImage(@RequestParam("file") MultipartFile mfile, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!mfile.isEmpty()){
            InputStream inputStream = mfile.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String temp = "";
            while ((temp = br.readLine())!=null){
                stringBuilder.append(temp.trim());
            }
            String[] strings = stringBuilder.toString().split(";");
            QrCode qrCode = new QrCode();
            for(int i=0; i < strings.length; i++) {
                qrCode.getQrCode("www.baidu.com", strings[i], "hello"+(i+1)+".png");
            }

            ZipCompressor zc = new ZipCompressor("erweima.zip");
            zc.compress("image");


            String fileName = "erweima.zip";
            File file = new File(fileName);
            if (file.exists()) {
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition",
                        "attachment;fileName=" + fileName);// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    logger.info("下载成功");
                } catch (Exception e) {

                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            logger.error("error", e);
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            logger.error("error", e);
                        }
                    }
                }
            }
        }
    }

}
