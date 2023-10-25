package top.hcode.hoj.crawler.problem;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.utils.Constants;

public class SCPCProblemStrategy extends ProblemStrategy {
    public static final String JUDGE_NAME = "SCPC";
    public static final String HOST = "http://scpc.fun";
    public static final String LOGIN_URL = "/api/login";
    public static final String COMMONPROBLEM_URL = "/api/get-problem-detail";
    public static final String CONTESTPROBLEM_URL = "/api/get-contest-problem-details";

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 验证题号是否符合规范
        problemId = problemId.toLowerCase();
        boolean isMatch = ReUtil.isMatch("[0-9]+_[a-z]*[0-9]*", problemId);
        Long cid = 0L;
        String pid = "";
        if (problemId.contains("_") && isMatch) {
            String[] arr = problemId.split("_");
            cid = Long.valueOf(arr[0]);
            pid = arr[1];
        } else if (!problemId.contains("_")) {
            pid = problemId;
        } else {
            throw new IllegalArgumentException("SCPC: Incorrect problem id format! Must be like `110_a` or `abc123`");
        }

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
        String csrfToken = response.headers().get("Authorization").get(0);

        List<HttpCookie> cookies = response.getCookies();

        Problem info = new Problem();
        String url = HOST + (cid == 0 ? COMMONPROBLEM_URL : CONTESTPROBLEM_URL);
        String problem_hint = (cid == 0
                ? String.format("<a style='color:#5c84a0' href='http://scpc.fun/problem/%s'>%s</a>",
                        problemId, JUDGE_NAME + "-" + problemId)
                : String.format("<a style='color:#5c84a0' href='http://scpc.fun/contest/%s/problem/%s'>%s</a>",
                        cid, pid, JUDGE_NAME + "-" + problemId));
        request = HttpUtil.createGet(url);

        // headers
        headers.put("authorization", csrfToken);
        request.addHeaders(headers);
        if (cid == 0) {
            request.form("problemId", pid);
        } else {
            request.form("cid", cid).form("displayId", pid).form("containsEnd", "true");
        }

        request.cookie(cookies);

        response = request.execute();
        String body = response.body();
        JSONObject jsonObject = new JSONObject(body);
        int status = jsonObject.getInt("status");

        info.setProblemId(JUDGE_NAME + "-" + problemId);
        if (status == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject record = data.getJSONObject("problem");

            info.setTitle(record.getStr("title"));
            info.setTimeLimit(record.getInt("timeLimit"));
            info.setMemoryLimit(record.getInt("memoryLimit"));
            info.setDescription(record.getStr("description"));
            info.setInput(record.getStr("input"));
            info.setOutput(record.getStr("output"));
            info.setExamples(record.getStr("examples"));
            info.setHint(record.getStr("hint"));
            info.setIsRemote(true);
            info.setSource(problem_hint);
            int difficultyValue = record.getInt("difficulty");
            int difficulty;
            switch (difficultyValue) {
                case 10000:
                case 20000:
                    difficulty = 0;
                    break;
                case 30000:
                case 40000:
                    difficulty = 1;
                    break;
                default:
                    difficulty = 2;
            }
            info.setType(0)
                    .setAuth(1)
                    .setAuthor(author)
                    .setOpenCaseResult(false)
                    .setIsRemoveEndBlank(false)
                    .setIsGroup(false)
                    .setDifficulty(difficulty);
        }
        return new RemoteProblemInfo()
                .setProblem(info)
                .setTagList(null)
                .setRemoteOJ(Constants.RemoteOJ.SCPC);
    }
}
