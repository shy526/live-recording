package top.ccxxh.liverecording;

import com.sun.xml.internal.fastinfoset.tools.XML_SAX_StAX_FI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxxh.liverecording.service.LiveService;
import top.ccxxh.liverecording.service.impl.BiliBiliServiceImpl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qing
 */
@SpringBootApplication
public class LiveRecordingApplication implements CommandLineRunner {
    private final static Logger log = LoggerFactory.getLogger(LiveRecordingApplication.class);
    @Autowired
    private LiveService biliBiliService;

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired
    private HttpClientService httpClientService;

    public static void main(String[] args) {
        SpringApplication.run(LiveRecordingApplication.class, args);
    }
    public static  final SimpleDateFormat DATA_FORMAT=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private final static long MAX_SIZE = (long) ((1024L * 1024L) * 100D);
    @Override
    public void run(String... args) throws Exception {
        Integer roomId = 4896470;
        if (biliBiliService.getLiveStatus(roomId)) {
            String livePayUrl = biliBiliService.getLivePayUrl(roomId);
            log.info(livePayUrl);
            HttpResult httpResult = httpClientService.get(livePayUrl);
            String filePath=DATA_FORMAT.format(new Date())+"の%s.flv";
            try (
                    BufferedInputStream liveIn = new BufferedInputStream(httpResult.getResponse().getEntity().getContent());
                    BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(filePath))
            ) {
                byte[] buff = new byte[1024*4];
                int len = -1;
                long size = 0;
                while ((len = liveIn.read(buff)) != -1) {
                    fileOut.write(buff, 0, len);
                    size += len;
                    if (size >= MAX_SIZE) {
                        break;
                    }
                }
                fileOut.flush();
                log.info("end");
            }
          new File(filePath).renameTo(new File(String.format(filePath, DATA_FORMAT.format(new Date()))));
        } else {
            log.info("end");
        }
    }
}
