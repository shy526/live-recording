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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

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

    private final static long MAX_SIZE = (long) ((1024L * 1024L) * 100D);

    @Override
    public void run(String... args) throws Exception {
        Integer roomId = 22528847;
        if (biliBiliService.getLiveStatus(roomId)) {
            String livePayUrl = biliBiliService.getLivePayUrl(roomId);
            HttpResult httpResult = httpClientService.get(livePayUrl);
            try (
                    BufferedInputStream liveIn = new BufferedInputStream(httpResult.getResponse().getEntity().getContent());
                    BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream("test.flv"))
            ) {
                byte[] buff = new byte[1024];
                int len = -1;
                long size = 0;
                while ((len = liveIn.read(buff)) != -1) {
                    fileOut.write(buff, 0, len);
                    size += len;
                    if (size >= MAX_SIZE) {
                        fileOut.flush();
                        break;
                    }
                }
                log.info("end");
            }
        }
    }
}
