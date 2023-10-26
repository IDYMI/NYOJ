package top.hcode.hoj.crawler.problem;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.utils.Constants;

/*
 *
 * TODO 查询对应 Pid 用超管进入后台查看题目信息
 */
public class SCPCProblemStrategy extends ProblemStrategy {

    @Autowired
    private ProblemEntityService problemEntityService;

    public static final String HOST = "http://scpc.fun";

    public static final String JUDGE_NAME = "SCPC";
    public static final String COMMONPROBLEM_URL = "/api/get-problem-detail";
    public static final String CONTESTPROBLEM_URL = "/api/get-contest-problem-details";
    public static final String REALPROBLEM_URL = "/api/admin/problem";

    public static final String LOGIN_URL = "/api/login";
    public static String csrfToken = "";
    public static List<HttpCookie> cookies = new ArrayList<>();

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    public void login() {
        // 清除当前的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        // 登录管理账号获取密码
        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        request.addHeaders(headers);

        request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                .put("username", Constants.HOJSuperAdmin.Username.getMode())
                .put("password", Constants.HOJSuperAdmin.Password.getMode())
                .map()).toString());

        HttpResponse response = request.execute();
        csrfToken = response.headers().get("Authorization").get(0);
        cookies = response.getCookies();
    }

    private Long getRealId(Long cid, String disPlayId) {

        Long realId = -1L;
        String url = HOST + (cid == 0 ? COMMONPROBLEM_URL : CONTESTPROBLEM_URL);

        HttpRequest request = HttpUtil.createGet(url);
        // headers
        headers.put("authorization", csrfToken);
        request.addHeaders(headers);
        if (cid == 0) {
            request.form("problemId", disPlayId);
        } else {
            request.form("cid", cid).form("displayId", disPlayId).form("containsEnd", "true");
        }
        request.cookie(cookies);
        HttpResponse response = request.execute();
        String body = response.body();

        JSONObject jsonObject = new JSONObject(body);
        int status = jsonObject.getInt("status");

        if (status == 200) {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject record = data.getJSONObject("problem");

            // 获取题目对应的 pid
            return record.getLong("id");
        }
        return realId;
    }

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 超管登录
        login();
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

        Long realId = getRealId(cid, pid);

        if (realId == -1L) {
            throw new IllegalArgumentException("SCPC: Don't have such problem");
        }

        String problem_hint = (cid == 0
                ? String.format("<a style='color:#5c84a0' href='http://scpc.fun/problem/%s'>%s</a>",
                        pid, JUDGE_NAME + "-" + problemId)
                : String.format("<a style='color:#5c84a0' href='http://scpc.fun/contest/%s/problem/%s'>%s</a>",
                        cid, pid, JUDGE_NAME + "-" + problemId));

        String url = HOST + REALPROBLEM_URL;
        HttpRequest request = HttpUtil.createGet(url);
        // headers
        headers.put("authorization", csrfToken);
        request.addHeaders(headers);
        request.form("pid", realId);
        request.cookie(cookies);

        HttpResponse response = request.execute();
        String body = response.body();

        JSONObject jsonObject = new JSONObject(body);
        int status = jsonObject.getInt("status");

        if (status == 200) {
            JSONObject record = jsonObject.getJSONObject("data");

            Problem info = new Problem();
            info.setProblemId(JUDGE_NAME + "-" + problemId);
            info.setTitle(record.getStr("title"));
            info.setTimeLimit(record.getInt("timeLimit"));
            info.setMemoryLimit(record.getInt("memoryLimit"));

            info.setDescription(record.getStr("description").replace("/api", HOST + "/api"));
            info.setInput(record.getStr("input").replace("/api", HOST + "/api"));
            info.setOutput(record.getStr("output").replace("/api", HOST + "/api"));
            info.setHint(record.getStr("hint").replace("/api", HOST + "/api"));

            info.setExamples(record.getStr("examples"));
            info.setIsRemote(true);
            info.setSource(problem_hint);
            Integer difficultyValue = record.getInt("difficulty");
            Integer difficulty;
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
            info.setDifficulty(difficulty);
            info.setType(record.getInt("type"));
            info.setAuth(record.getInt("auth"));
            info.setAuthor(author)
                    .setOpenCaseResult(false)
                    .setIsRemoveEndBlank(false)
                    .setIsGroup(false);

            Boolean isRemote = record.getBool("isRemote"); // 是否为远程评测
            if (isRemote) {
                problemId = record.getStr("problemId");
                String remoteOJ = problemId.split("-")[0];
                info.setProblemId(problemId);
                // 解决远程评测id重复问题
                QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();

                queryWrapper.eq("problem_id", problemId);
                Problem problem = problemEntityService.getOne(queryWrapper);
                if (problem != null) {
                    throw new StatusFailException("该题目已添加，请勿重复添加！");
                }
                return new RemoteProblemInfo()
                        .setProblem(info)
                        .setTagList(null)
                        .setRemoteOJ(Constants.RemoteOJ.getRemoteOJ(remoteOJ));
            } else {
                info.setProblemId(JUDGE_NAME + "-" + problemId);
                return new RemoteProblemInfo()
                        .setProblem(info)
                        .setTagList(null)
                        .setRemoteOJ(Constants.RemoteOJ.SCPC);
            }

        } else {
            throw new IllegalArgumentException("SCPC: Don't have such problem");
        }
    }
}
