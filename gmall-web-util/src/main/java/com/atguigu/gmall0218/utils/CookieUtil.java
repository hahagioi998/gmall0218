package com.atguigu.gmall0218.utils;



import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author qiyu
 * @create 2019-08-02 19:28
 * @Description:从cookie中获得值，把值存入cookie, 设定cookie的作用域。
 */
public class CookieUtil {
    /**
     * 获取cookie中的值
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder){
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null){
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    if (isDecoder) {//如果涉及中文
                        retValue = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                    } else {
                        retValue = cookies[i].getValue();
                    }
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 得到cookie的域名
     * @param request
     * @return
     */
    private static final String getDomainName(HttpServletRequest request) {
        String domainName = null;
        //获取请求地址
        String serverName = request.getRequestURL().toString();
        if (serverName == null || serverName.equals("")) {
            domainName = "";
        } else {
            //将地址转为小写
            serverName = serverName.toLowerCase();
            //将前7个字母截取掉
            serverName = serverName.substring(7);
            //找到"/"第一次出现的位置
            final int end = serverName.indexOf("/");
            //截取到第一次/之前的字符串
            serverName = serverName.substring(0, end);
            final String[] domains = serverName.split("\\.");
            int len = domains.length;
            if (len > 3) {
                // www.xxx.com.cn
                domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
            } else if (len <= 3 && len > 1) {
                // xxx.com or xxx.cn
                domainName = domains[len - 2] + "." + domains[len - 1];
            } else {
                domainName = serverName;
            }
        }

        if (domainName != null && domainName.indexOf(":") > 0) {
            String[] ary = domainName.split("\\:");
            domainName = ary[0];
        }
        System.out.println("domainName = " + domainName);
        return domainName;



    }
    /**
     * 将值存进cookie中
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxage
     * @param isEncode
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxage, boolean isEncode) {
        try {
            if (cookieValue == null) {
                cookieValue = "";
            } else if (isEncode) {
                cookieValue = URLEncoder.encode(cookieValue, "utf-8");
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxage >= 0)
                cookie.setMaxAge(cookieMaxage);
            if (null != request)// 设置域名的cookie
                cookie.setDomain(getDomainName(request));
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        setCookie(request, response, cookieName, null, 0, false);
    }

}
