package org.hhs.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.hhs.domain.QrCodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hewater on 2017/9/13.
 */
@Service
public class GeneratorImage {
    private Logger logger = LoggerFactory.getLogger(GeneratorImage.class);
    private static final int QRCOLOR = 0xFF000000;   //默认是黑色
    private static final int BGWHITE = 0xFFFFFFFF;   //背景颜色
    private static File fileRoot = new File("image");
    static {
        if(!fileRoot.exists()){
            fileRoot.mkdir();
        }
    }

//    public static void main(String...args) throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader(new File("F:\\test.txt")));
//        String temp = "";
//        List<QrCodeInfo> lists = new ArrayList<QrCodeInfo>();
//        QrCodeInfo qrCodeInfo = null;
//        while((temp = reader.readLine())!=null){
//            String[] strings = temp.trim().split("\\s+");
//            qrCodeInfo = new QrCodeInfo();
//            qrCodeInfo.setContent(strings[0]);
//            qrCodeInfo.setNum(strings[1]);
//            lists.add(qrCodeInfo);
//        }
//        GeneratorImage generatorImage = new GeneratorImage();
//        for(int i = 0; i < lists.size()/10; i++){
//            generatorImage.generatorOne(lists.subList(i*10, 10*(i+1)), "erweima" + i+".jpg");
//        }
//    }

    public List<QrCodeInfo> getQrInfoList(BufferedReader reader){
        String temp = "";
        List<QrCodeInfo> lists = new ArrayList<QrCodeInfo>();
        QrCodeInfo qrCodeInfo = null;
        try {
            while((temp = reader.readLine())!=null){
                String[] strings = temp.trim().split("\\s+");
                qrCodeInfo = new QrCodeInfo();
                qrCodeInfo.setContent(strings[0]);
                qrCodeInfo.setNum(strings[1]);
                lists.add(qrCodeInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public BufferedImage getQR_CODEBufferedImage(String content, BarcodeFormat barcodeFormat, int width, int height, Map<EncodeHintType, ?> hints)
    {
        MultiFormatWriter multiFormatWriter = null;
        BitMatrix bm = null;
        BufferedImage image = null;
        try
        {
            multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            bm = multiFormatWriter.encode(content, barcodeFormat, width, height, hints);
            int w = bm.getWidth();
            int h = bm.getHeight();
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < w; x++)
            {
                for (int y = 0; y < h; y++)
                {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 设置二维码的格式参数
     *
     * @return
     */
    public Map<EncodeHintType, Object> getDecodeHintType()
    {
        // 用于设置QR二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.MAX_SIZE, 350);
        hints.put(EncodeHintType.MIN_SIZE, 100);

        return hints;
    }

    //获得二维码+编号的图片
    public BufferedImage generatorQrCode(String content, String productName){
        try{
            int width = 200;
            int height = 200;
            /**
             * 读取二维码图片，并构建绘图对象
             */
            BufferedImage image = getQR_CODEBufferedImage(content, BarcodeFormat.QR_CODE, width, height, getDecodeHintType());
            Graphics2D g = image.createGraphics();
            g.dispose();

            //把编号添加上去，这里最多支持两行。太长就会自动截取啦
            if (productName != null && !productName.equals("")) {
                //新的图片，把带logo的二维码下面加上文字
                BufferedImage outImage = new BufferedImage(width, height+25, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D outg = outImage.createGraphics();
                //画二维码到新的面板
                outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                //画文字到新的面板
                outg.setColor(Color.BLACK);
                Font font = new Font("宋体",Font.BOLD,28);
                outg.setFont(font); //字体、字型、字号
                int strWidth = outg.getFontMetrics().stringWidth(productName);
                if (strWidth > (width-1)) {
//                  //长度过长就截取前面部分
                    String productName1 = productName.substring(0, productName.length()/2);
                    String productName2 = productName.substring(productName.length()/2, productName.length());
                    int strWidth1 = outg.getFontMetrics().stringWidth(productName1);
                    int strWidth2 = outg.getFontMetrics().stringWidth(productName2);
                    outg.drawString(productName1, width/2  - strWidth1/2, image.getHeight() + (outImage.getHeight() - image.getHeight())/2 + 12 );
                    BufferedImage outImage2 = new BufferedImage(200, 270, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg2 = outImage2.createGraphics();
                    outg2.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                    outg2.setColor(Color.BLACK);
                    outg2.setFont(new Font("宋体",Font.BOLD,28)); //字体、字型、字号
                    outg2.drawString(productName2, width/2  - strWidth2/2, outImage.getHeight() + (outImage2.getHeight() - outImage.getHeight())/2 + 5 );
                    outg2.dispose();
                    outImage2.flush();
                    outImage = outImage2;
                }else {
                    outg.drawString(productName, width/2  - strWidth/2 , image.getHeight() + (outImage.getHeight() - image.getHeight())/2 + 5 ); //画文字
                }
                outg.dispose();
                outImage.flush();
                image = outImage;
            }
            image.flush();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            baos.flush();
//            ImageIO.write(image, "png", baos);

            //二维码生成的路径，但是实际项目中，我们是把这生成的二维码显示到界面上的，因此下面的折行代码可以注释掉



//            ImageIO.write(image, "png", file); //TODO
//            String imageBase64QRCode =  Base64.encodeBase64URLSafeString(baos.toByteArray());
//            baos.close();
            return image;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void generatorOne(List<QrCodeInfo> infos, String fileName){
        List<BufferedImage> lists = getBufferedImages(infos);
        BufferedImage resultImage = new BufferedImage(2683, 847*2, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D resultG = resultImage.createGraphics();

        BufferedImage bufferedImage = null;
        for(int i = 0; i < lists.size(); i++){
            bufferedImage = lists.get(i);
            if(i < 5){
                resultG.drawImage(bufferedImage, 168+i*277+250*i, 130, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
            }else{
                resultG.drawImage(bufferedImage, 168+(i-5)*277+250*(i-5), 1025, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
            }
        }
        resultG.dispose();
        resultImage.flush();
        File file = new File(fileRoot,fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ImageIO.write(resultImage, "png", file);
            logger.info("生成图片---"+file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public List<BufferedImage> getBufferedImages(List<QrCodeInfo> infos){
        List<BufferedImage> lists = new ArrayList<BufferedImage>();
        for(QrCodeInfo qrCodeInfo : infos){
            BufferedImage bufferedImage = generatorQrCode(qrCodeInfo.getContent(), qrCodeInfo.getNum());
            lists.add(bufferedImage);
        }
        return  lists;
    }

}
