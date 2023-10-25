package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @Date: 2021/6/24 21:19
 * @Description:
 */
@Slf4j(topic = "hoj")
public class SCPCJudge extends RemoteJudgeStrategy {

    public static final String HOST = "http://scpc.fun";
    public static final String LOGIN_URL = "/api/login";
    public static final String SUBMIT_URL = "/api/submit-problem-judge";
    public static final String SUBMISSION_RESULT_URL = "/api/get-submission-detail";
    public static final String CONTESTPWD_URL = "/api/admin/contest";
    public static final String REGISTERCONTEST_URL = "/api/register-contest";
    public static final String COMMONSUBMISSIONS_URL = "/api/get-submission-list";
    public static final String CONTESTSUBMISSIONS_URL = "/api/contest-submissions";

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    @Override
    public void submit() {
        login();
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        if (remoteJudgeDTO.getCompleteProblemId() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }

        if (remoteJudgeDTO.getLoginStatus() != 200) {
            log.error("Login to SCPC failed, the response status:{},username:{},password:{}",
                    remoteJudgeDTO.getLoginStatus(), remoteJudgeDTO.getUsername(), remoteJudgeDTO.getPassword());
            throw new RuntimeException(
                    "[SCPC] Failed to Login, the response status:" + remoteJudgeDTO.getLoginStatus());
        }

        HttpResponse response = trySubmit();

        if (response.getStatus() == 403) {
            JSONObject rankJsonObject = new JSONObject(response);
            // 获取 msg 字段的值
            String msg = rankJsonObject.getStr("msg");
            if (msg.equals("对不起，请你先注册该比赛，提交代码失败！")) {
                // 说明判题账号未被注册，采取超管获取比赛密码，并且注册
                response = signContest();
            } else if (msg.equals("对不起，您的提交频率过快，请稍后再尝试！")) {
                // 说明被限制提交频率了
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            response = trySubmit();
        }

        // if (response.getStatus() != 200) {
        //     log.error(
        //             "Submit to SCPC failed, the response status:{}, It may be that the frequency of submission operation is too fast. Please try later",
        //             response.getStatus());
        //     throw new RuntimeException("[SCPC] Failed to Submit, the response status:" + response.getStatus());
        // }

        Long maxRunId = getMaxRunId(
                remoteJudgeDTO.getUsername(),
                Long.valueOf(remoteJudgeDTO.getContestId()),
                remoteJudgeDTO.getProblemNum());
        if (maxRunId == -1L) { // 等待2s再次查询，如果还是失败，则表明提交失败了
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maxRunId = getMaxRunId(
                    remoteJudgeDTO.getUsername(),
                    Long.valueOf(remoteJudgeDTO.getContestId()),
                    remoteJudgeDTO.getProblemNum());
        }
        remoteJudgeDTO.setCookies(remoteJudgeDTO.getCookies())
                .setSubmitId(maxRunId);
    }

    private HttpResponse trySubmit() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        String csrfToken = remoteJudgeDTO.getCsrfToken();

        String submitUrl = HOST + SUBMIT_URL;
        HttpRequest request = HttpUtil.createPost(submitUrl);

        // headers
        headers.put("authorization", csrfToken);
        request.addHeaders(headers);

        request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                .put("language", getLanguage(remoteJudgeDTO.getLanguage()))
                .put("code", remoteJudgeDTO.getUserCode())
                .put("isRemote", false)
                .put("cid", Long.valueOf(remoteJudgeDTO.getContestId()))
                .put("pid", remoteJudgeDTO.getProblemNum())
                .put("gid", remoteJudgeDTO.getGid())
                .map()).toString());
        request.cookie(cookies);

        HttpResponse response = request.execute();
        remoteJudgeDTO.setSubmitStatus(response.getStatus());
        return response;
    }

    @Override
    public RemoteJudgeRes result() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        String csrfToken = remoteJudgeDTO.getCsrfToken();
        Long submitId = remoteJudgeDTO.getSubmitId();

        String url = HOST + SUBMISSION_RESULT_URL;

        HttpRequest httpRequest = HttpUtil.createGet(url);

        headers.put("authorization", csrfToken);
        httpRequest.addHeaders(headers);

        // param 信息
        httpRequest.form("submitId", submitId.toString());
        httpRequest.cookie(cookies);
        String body = httpRequest.execute().body();

        JSONObject jsonObject = new JSONObject(body);
        int respose_status = jsonObject.getInt("status");

        Integer status = 10;
        String time = "";
        String memory = "";
        String CEInfo = "";
        if (respose_status == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject record = data.getJSONObject("submission");
            status = record.getInt("status");
            time = record.getInt("time").toString();
            memory = record.getInt("memory").toString();
            CEInfo = record.getStr("errorMessage");
        }

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(status)
                .time(time == null ? null : Integer.parseInt(time))
                .memory(memory == null ? null : Integer.parseInt(memory))
                .build();
        if (status == -2) {
            remoteJudgeRes.setErrorInfo(HtmlUtil.unescape(CEInfo));
        }
        return remoteJudgeRes;
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        request.addHeaders(headers);

        request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                .put("username", remoteJudgeDTO.getUsername())
                .put("password", remoteJudgeDTO.getPassword())
                .map()).toString());

        HttpResponse response = request.execute();
        String csrfToken = response.headers().get("Authorization").get(0);

        remoteJudgeDTO.setLoginStatus(response.getStatus())
                .setCookies(response.getCookies())
                .setCsrfToken(csrfToken);
    }

    @Override
    public String getLanguage(String language) {
        return language;
    }

    private Long getMaxRunId(String username, Long cid, String problemId) {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        String csrfToken = remoteJudgeDTO.getCsrfToken();

        Long maxRunId = -1L;

        String url = HOST + (cid == 0 ? COMMONSUBMISSIONS_URL : CONTESTSUBMISSIONS_URL);
        HttpRequest httpRequest = HttpUtil.createGet(url);

        headers.put("authorization", csrfToken);
        httpRequest.addHeaders(headers);

        // param 信息
        httpRequest.form("onlyMine", "false")
                .form("username", username)
                .form("currentPage", "1")
                .form("limit", "100")
                .form("completeProblemID", "false")
                .form("problemID", problemId);

        if (cid != 0) {
            httpRequest.form("contestID", cid.toString())
                    .form("beforeContestSubmit", "false")
                    .form("containsEnd", "true")
                    .form("completeProblemID", "true");
        }
        httpRequest.cookie(cookies);
        String body = httpRequest.execute().body();

        JSONObject jsonObject = new JSONObject(body);
        int status = jsonObject.getInt("status");
        if (status == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray records = data.getJSONArray("records");

            if (records.size() > 0) {
                JSONObject record = records.getJSONObject(0);
                maxRunId = Long.parseLong(record.getStr("submitId"));
            }
        }
        return maxRunId;
    }

    private String getContestPwd() {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        // 登录管理账号获取密码
        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        request.addHeaders(headers);

        request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                .put("username", Constants.HOJSuperAdmin.Username.getMode())
                .put("password", Constants.HOJSuperAdmin.Password.getMode())
                .map()).toString());

        HttpResponse response = request.execute();
        String csrfToken = response.headers().get("Authorization").get(0);

        List<HttpCookie> cookies = response.getCookies();

        String pwd = "";
        String url = HOST + CONTESTPWD_URL;
        request = HttpUtil.createGet(url);
        request.form("cid", remoteJudgeDTO.getContestId());

        headers.put("authorization", csrfToken);
        request.addHeaders(headers);
        request.cookie(cookies);

        String body = request.execute().body();
        JSONObject jsonObject = new JSONObject(body);

        int status = jsonObject.getInt("status");
        if (status == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            pwd = data.getStr("pwd");
        }
        return pwd;
    }

    private HttpResponse signContest() {
        String pwd = getContestPwd();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        String csrfToken = remoteJudgeDTO.getCsrfToken();

        String submitUrl = HOST + REGISTERCONTEST_URL;
        HttpRequest request = HttpUtil.createPost(submitUrl);

        // headers
        headers.put("authorization", csrfToken);
        request.addHeaders(headers);

        request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                .put("password", pwd)
                .put("cid", remoteJudgeDTO.getContestId())
                .map()).toString());
        request.cookie(cookies);

        HttpResponse response = request.execute();
        return response;
        // "对不起！本次比赛只允许特定账号规则的用户参赛！"
    }
}
