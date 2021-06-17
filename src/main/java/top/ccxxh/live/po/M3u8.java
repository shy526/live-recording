package top.ccxxh.live.po;

import java.io.Serializable;
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
        for (String item:itemArray){
             String payUrl = item;
             if (payUrl.charAt(0)!='#'){
                 m3u8.addPayList(payUrl);
             }
        }
        m3u8.setUrl(url);
        return m3u8;
    }
}
