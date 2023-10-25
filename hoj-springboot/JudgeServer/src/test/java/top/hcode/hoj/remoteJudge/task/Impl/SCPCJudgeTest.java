package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.util.Constants;
import java.util.concurrent.TimeUnit;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCPCJudgeTest {

        public static void main(String[] args) {
                // String completeProblemId = "11111";
                // String[] arr = completeProblemId.split("_");

                // System.out.println(arr.length);

                new SCPCJudgeTest().login("DYM_", "dym1215***@");
                // new SCPCJudgeTest().trySubmit(null, HOST);
                // String pwd = new SCPCJudgeTest().getContestPwd();
                // System.out.println(pwd);
        }

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

        public void login(String username, String password) {
                // 清除当前线程的cookies缓存
                HttpRequest.getCookieManager().getCookieStore().removeAll();

                // RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
                HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
                request.addHeaders(headers);
                request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                                .put("username", username)
                                .put("password", password)
                                .map()).toString());

                HttpResponse response = request.execute();

                String csrfToken = response.headers().get("Authorization").get(0);
                List<HttpCookie> cookies = response.getCookies();

                // TODO 测试提交题目
                // response = trySubmit(cookies, csrfToken);
                // System.out.println("提交结果：" + response.body());

                // TODO 测试报名比赛

                // response = signContest(cookies, csrfToken);
                // System.out.println(response.body());

                // TODO 测试获取最新的提交
                // Long cid = 0L;
                // Long maxRunId = getMaxRunId(username, cid, "1000", csrfToken);
                // if (maxRunId == -1L) { // 等待2s再次查询，如果还是失败，则表明提交失败了
                //         try {
                //                 TimeUnit.SECONDS.sleep(2);
                //         } catch (InterruptedException e) {
                //                 e.printStackTrace();
                //         }
                //         maxRunId = getMaxRunId(username, cid, "1000", csrfToken);
                // }
                // System.out.println(maxRunId);

                // TODO 测试获取状态
                // String submitId = "259821";
                // RemoteJudgeRes remoteJudgeRes = result(csrfToken, submitId);

                // System.out.println(remoteJudgeRes);

                // TODO 测试提交模块
        }

        public RemoteJudgeRes result(String csrfToken, String submitId) {

                String url = HOST + SUBMISSION_RESULT_URL;

                HttpRequest httpRequest = HttpUtil.createGet(url);

                headers.put("authorization", csrfToken);
                httpRequest.addHeaders(headers);

                // param 信息
                httpRequest.form("submitId", submitId);
                httpRequest.cookie();
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

        private String getContestPwd() {
                // 清除当前线程的cookies缓存
                HttpRequest.getCookieManager().getCookieStore().removeAll();

                // 登录管理账号获取密码
                HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
                request.addHeaders(headers);

                request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                                .put("username", Constants.HOJSuperAdmin.Username.getMode())
                                .put("password", Constants.HOJSuperAdmin.Password.getMode())
                                .map()).toString());

                HttpResponse response = request.execute();
                List<HttpCookie> cookies = response.getCookies();
                String csrfToken = response.headers().get("Authorization").get(0);
                String pwd = "";
                String url = HOST + CONTESTPWD_URL;
                request = HttpUtil.createGet(url);
                request.form("cid", "1118");

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

        private HttpResponse signContest(List<HttpCookie> cookies, String csrfToken) {
                String pwd = new SCPCJudgeTest().getContestPwd();

                System.out.println(pwd);
                String submitUrl = HOST + REGISTERCONTEST_URL;
                HttpRequest request = HttpUtil.createPost(submitUrl);

                // headers
                headers.put("authorization", csrfToken);
                request.addHeaders(headers);

                request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                                .put("password", pwd)
                                .put("cid", "1118")
                                .map()).toString());
                request.cookie(cookies);

                HttpResponse response = request.execute();

                return response;
                // "对不起！本次比赛只允许特定账号规则的用户参赛！"
        }

        private HttpResponse trySubmit(List<HttpCookie> cookies, String csrfToken) {
                String submitUrl = HOST + SUBMIT_URL;

                HttpRequest request = HttpUtil.createPost(submitUrl);
                headers.put("authorization", csrfToken);
                request.addHeaders(headers);

                String CompleteProblemId = "SCPC-1000";
                String[] arr = CompleteProblemId.split("-");
                String problemId = arr[1];

                problemId = problemId.toLowerCase();
                boolean isMatch = ReUtil.isMatch("[0-9]+_[a-z]*[0-9]*", problemId);
                Long cid = 0L;
                String pid = "";
                if (problemId.contains("_") && isMatch) {
                        String[] arr2 = problemId.split("_");
                        cid = Long.valueOf(arr2[0]);
                        pid = arr2[1];
                } else if (!problemId.contains("_")) {
                        pid = problemId;
                }
                request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                                .put("pid", pid)
                                .put("language", "Python3")
                                .put("code", "print(\\\"2.00\\\")")
                                .put("isRemote", false)
                                .put("cid", cid)
                                .map()).toString());

                request.cookie(cookies);

                HttpResponse response = request.execute();

                JSONObject jsonObject = new JSONObject(response);
                // 获取 msg 字段的值
                String msg = jsonObject.getStr("msg");

                System.out.println(msg);
                return response;
        }

        private Long getMaxRunId(String username, Long cid, String problemId, String csrfToken) {
                Long maxRunId = -1L;
                // 清除当前线程的cookies缓存
                HttpRequest.getCookieManager().getCookieStore().removeAll();
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
                httpRequest.cookie();
                String body = httpRequest.execute().body();

                System.out.println(body);
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
}
