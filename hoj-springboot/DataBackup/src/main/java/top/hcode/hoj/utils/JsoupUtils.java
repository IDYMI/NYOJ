package top.hcode.hoj.utils;

import cn.hutool.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JsoupUtils {

    /**
     * 获取连接
     *
     * @param url     api网址
     * @param params  请求参数
     * @param headers 用户头
     * @return 返回一个object
     * @throws IOException
     */
    public static Connection getConnectionFromUrl(String url, Map<String, String> params, Map<String, String> headers)
            throws IOException {
        // 给url添加参数
        if (params != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            if (url.indexOf("?") <= 0) {
                sb.append("?");
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append('&');
            }
            // 检查字符串是否以&结尾
            if (url.endsWith("&")) {
                // 使用substring去除末尾的&
                url = url.substring(0, url.length() - 1);
            }
            url = sb.toString();
        }
        Connection connection = Jsoup.connect(url);
        // 设置用户代理
        connection.userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        // 设置超时时间30秒
        connection.timeout(30000);
        // 设置请求头
        if (headers != null) {
            connection.headers(headers);
        }
        return connection;
    }

    public static Connection getShorterConnectionFromUrl(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            Map<String, String> payload) throws IOException, Exception {
        // 给url添加参数
        if (params != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            if (url.indexOf("?") <= 0) {
                sb.append("?");
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append('&');
            }
            // 检查字符串是否以&结尾
            if (url.endsWith("&")) {
                // 使用substring去除末尾的&
                url = url.substring(0, url.length() - 1);
            }
            url = sb.toString();
        }
        Connection connection = Jsoup.connect(url);
        // 设置用户代理
        connection.userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        // 设置超时时间5秒
        connection.timeout(5000);
        // 设置请求头
        if (headers != null) {
            connection.headers(headers);
        }
        // 添加请求 payload
        if (payload != null) {
            // 转换为 JSON 字符串
            String payloadJson = mapToJson(payload);
            connection.requestBody(payloadJson);
        }
        return connection;
    }

    private static String mapToJson(Map<String, String> map) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(map);
    }

    /**
     * 通过jsoup连接返回json格式
     *
     * @param connection Jsoup的connection连接
     * @return
     * @throws IOException
     */
    public static JSONObject getJsonFromConnection(Connection connection) throws IOException {
        // 设置忽略请求类型
        connection.ignoreContentType(true);
        String body = connection.execute().body();
        return new JSONObject(body);
    }

    public static Connection.Response postResponse(Connection connection, Map<String, String> postData)
            throws IOException {
        connection.data(postData);
        return connection.method(Connection.Method.POST).execute();
    }

    public static Connection.Response getResponse(Connection connection, Map<String, String> getData)
            throws IOException {
        // 添加参数
        if (getData != null) {
            connection.data(getData);
        }
        return connection.method(Connection.Method.GET).execute();
    }

    public static Document getDocument(Connection connection, Map<String, String> getData) throws IOException {
        // 添加参数
        if (getData != null) {
            connection.data(getData);
        }
        Document document = connection.get();
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().prettyPrint(false);
        return document;
    }
}
