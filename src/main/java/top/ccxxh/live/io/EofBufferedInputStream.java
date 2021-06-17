package top.ccxxh.live.io;

import org.apache.http.conn.EofSensorInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 针对EofSensorInputStream 流的特殊处理
 * 个别流关闭时会阻塞,原因未知
 * @author qing
 */
public class EofBufferedInputStream extends BufferedInputStream {
    public EofBufferedInputStream(InputStream in) {
        super(in);
    }

    public EofBufferedInputStream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public void close() throws IOException {
        if (in instanceof EofSensorInputStream) {
            ((EofSensorInputStream) in).abortConnection();
        }
        super.close();
    }
}
