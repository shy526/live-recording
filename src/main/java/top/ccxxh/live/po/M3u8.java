package top.ccxxh.live.po;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qing
 */
public class M3u8 implements Serializable {
    private String url;
    private List<String> payList;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getPayList() {
        return payList;
    }

    public void addPayList(String payUrl) {
        if (this.payList == null) {
            this.payList = new ArrayList<>();
        }
        payList.add(payUrl);
    }

    public static M3u8 parse(String url, String str) {
        M3u8 m3u8 = new M3u8();
        String[] itemArray = str.split("\n");

        for (String item : itemArray) {
            if (item.charAt(0) != '#') {
                item = getPayUrl(url, item);
                m3u8.addPayList(item);
            }
        }
        m3u8.setUrl(url);
        return m3u8;
    }

    private static String getPayUrl(String url, String item) {
        String regex = "^(http|https)://.*";
        if (!item.matches(regex)) {
            if (item.charAt(0) == '/') {
                item = item.substring(1);
            }
            item = getHostUrl(url) + item;

        }

        return item;
    }

    private static String getHostUrl(String url) {
        String urlPath = null;
        try {
            URL urlO = new URL(url);
            StringBuilder urlStr = new StringBuilder(urlO.getProtocol()).append("://").append(urlO.getHost()).append(urlO.getPath());
            urlPath = urlStr.substring(0, urlStr.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
        }
        return urlPath;
    }
}
