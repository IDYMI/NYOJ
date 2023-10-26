package top.hcode.hoj.manager.oj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.pojo.vo.ContestSynchronousConfigVO;
import top.hcode.hoj.pojo.vo.JudgeVO;
import top.hcode.hoj.pojo.vo.ContestProblemVO;
import top.hcode.hoj.pojo.entity.problem.Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import top.hcode.hoj.utils.Constants;

/**
 * @param contest                     比赛的信息
 * @param isContainsAfterContestJudge 是否包含赛后提交
 * @param removeStar                  是否移除打星用户
 * @MethodName getSynchronousRank
 * @Description TODO
 * @Return
 * @Since 2021/12/10
 */
@Component
public class SynchronousManager {
    public static final String HOST = "http://scpc.fun";
    public static final String LOGIN_URL = "/api/login";
    public static String csrfToken = "";
    public static List<HttpCookie> cookies = new ArrayList<>();

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    public void login() {
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

    @Autowired
    private ContestEntityService contestEntityService;

    public URL getUrl(String contestUrl) {
        // 获取比赛的根域名
        try {
            URL url = new URL(contestUrl);
            return url;
        } catch (MalformedURLException e) {
            // 处理异常，可以打印日志或者抛出自定义异常
            e.printStackTrace();
            throw new RuntimeException("Malformed URL: " + contestUrl, e);
        }
    }

    public String getRootDomain(String contestUrl) {
        // 获取比赛的根域名
        URL url = getUrl(contestUrl);
        String rootDomain = url.getHost();
        return rootDomain;
    }

    public String getCid(String contestUrl) {
        // 获取比赛对应的 cid
        URL url = getUrl(contestUrl);
        String path = url.getPath();
        String[] pathSegments = path.split("/");
        String synchronousCid = pathSegments[pathSegments.length - 1];
        return synchronousCid;
    }

    public List<JSONObject> getSynchronousConfigList(Contest contest) {
        // 获取比赛对应的同步赛信息
        JSONObject SynchronousJsonObject = JSONUtil.parseObj(contest.getSynchronousConfig());
        List<JSONObject> result = SynchronousJsonObject.get("config", List.class);

        return result;
    }

    /**
     * @param ContestLink 同步赛的网址
     * @param api         对应的API接口
     * @param type        请求类型
     */
    public HttpRequest getHttpRequest(ContestSynchronousConfigVO synchronousConfig, String api, String type)
            throws MalformedURLException {
        // 清除当前的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        // login();
        String contestLink = synchronousConfig.getLink();
        // 请求头中的 authorization 信息
        String authorization = synchronousConfig.getAuthorization();
        // 根域名
        String rootDomain = getRootDomain(contestLink);
        // 新建网络请求
        String link = rootDomain + api;

        // 处理可能的 MalformedURLException
        try {
            if (type == "get") {
                HttpRequest request = HttpUtil.createGet(link);
                Map<String, String> headers = MapUtil
                        .builder(new HashMap<String, String>())
                        .put("authorization", authorization)
                        .put("Url-Type", "general")
                        .put("Content-Type", "application/json")
                        .map();
                request.addHeaders(headers);
                return request;
            } else {
                HttpRequest request = HttpUtil.createPost(link);
                Map<String, String> headers = MapUtil
                        .builder(new HashMap<String, String>())
                        .put("authorization", authorization)
                        .put("Url-Type", "general")
                        .put("Content-Type", "application/json")
                        .map();
                request.addHeaders(headers);
                return request;
            }
        } catch (Exception e) {
            // 处理异常，可以打印日志或者抛出自定义异常
            e.printStackTrace();
            throw new RuntimeException("Failed to create HTTP request", e);
        }
    }

    public List<ACMContestRankVO> getSynchronousRankList(Contest contest, boolean isContainsAfterContestJudge,
            boolean removeStar) {
        List<ACMContestRankVO> synchronousRankList = new ArrayList();

        List<JSONObject> synchronousConfigList = getSynchronousConfigList(contest);

        for (JSONObject object : synchronousConfigList) {
            try {
                ContestSynchronousConfigVO synchronousConfig = JSONUtil.toBean(object,
                        ContestSynchronousConfigVO.class);
                String contestUrl = synchronousConfig.getLink();
                String synchronousCid = getCid(contestUrl);

                // 新建网络请求
                String api = "/api/get-contest-rank";
                HttpRequest request = getHttpRequest(synchronousConfig, api, "post");

                // data 信息
                request.body(new JSONObject(MapUtil.builder(new HashMap<String, Object>())
                        .put("currentPage", 1)
                        .put("limit", 1000000)
                        .put("cid", synchronousCid)
                        .put("forceRefresh", false)
                        .put("removeStar", true)
                        .put("concernedList", new ArrayList<>())
                        .put("containsEnd", false)
                        .map()).toString());

                HttpResponse response = request.execute();
                String synchronousRankJson = response.body();

                // System.out.println(synchronousRankJson);

                JSONObject JsonObject = new JSONObject(synchronousRankJson);

                int status = JsonObject.getInt("status");
                if (status == 200) {
                    JSONObject data = JsonObject.getJSONObject("data");
                    JSONArray records = data.getJSONArray("records");

                    for (int i = 0; i < records.size(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        ACMContestRankVO rankVO = parseSynchronousRank(record);
                        synchronousRankList.add(rankVO);
                    }
                }
            } catch (Exception e) {
                // 处理异常情况，可以记录日志等
                e.printStackTrace();
            }
        }
        return synchronousRankList;
    }

    public List<JudgeVO> getSynchronousSubmissionList(Contest contest, boolean isContainsAfterContestJudge,
            String searchUsername, String searchDisplayId, Integer searchStatus) {
        List<JudgeVO> synchronousSubmissionList = new ArrayList();

        // 获取比赛对应的同步赛信息
        List<JSONObject> synchronousConfigList = getSynchronousConfigList(contest);

        for (JSONObject object : synchronousConfigList) {
            try {
                ContestSynchronousConfigVO synchronousConfig = JSONUtil.toBean(object,
                        ContestSynchronousConfigVO.class);

                String contestUrl = synchronousConfig.getLink();
                String synchronousCid = getCid(contestUrl);

                // 新建网络请求
                String api = "/api/contest-submissions";
                HttpRequest request = getHttpRequest(synchronousConfig, api, "get");

                // param 信息
                request.form("onlyMine", "false")
                        .form("currentPage", "1")
                        .form("limit", "100000000")
                        .form("completeProblemID", "false")
                        .form("contestID", synchronousCid)
                        .form("beforeContestSubmit", "false")
                        .form("containsEnd", isContainsAfterContestJudge);

                if (searchUsername != null) {
                    request.form("username", searchUsername.toString());
                }
                if (searchDisplayId != null) {
                    request.form("problemID", searchDisplayId.toString());
                }
                if (searchStatus != null) {
                    request.form("status", searchStatus.toString());
                }

                HttpResponse response = request.execute();
                String synchronousRankJson = response.body();

                // System.out.println(synchronousRankJson);

                JSONObject JsonObject = new JSONObject(synchronousRankJson);

                int status = JsonObject.getInt("status");
                if (status == 200) {
                    JSONObject data = JsonObject.getJSONObject("data");
                    JSONArray records = data.getJSONArray("records");

                    for (int i = 0; i < records.size(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        JudgeVO judgeVO = parseSynchronousSubmission(record);
                        synchronousSubmissionList.add(judgeVO);
                    }
                }
            } catch (Exception e) {
                // 处理异常情况，可以记录日志等
                e.printStackTrace();
            }
        }
        return synchronousSubmissionList;
    }

    public List<ContestProblemVO> getSynchronousContestProblemList(Contest contest,
            boolean isContainsAfterContestJudge) {
        List<ContestProblemVO> synchronousContestProblemList = new ArrayList();

        // 获取比赛对应的同步赛信息
        JSONObject SynchronousJsonObject = JSONUtil.parseObj(contest.getSynchronousConfig());
        List<JSONObject> synchronousConfigList = SynchronousJsonObject.get("config", List.class);

        for (JSONObject object : synchronousConfigList) {
            try {
                ContestSynchronousConfigVO synchronousConfig = JSONUtil.toBean(object,
                        ContestSynchronousConfigVO.class);

                String contestUrl = synchronousConfig.getLink();
                String synchronousCid = getCid(contestUrl);

                String api = "/api/get-contest-problem";
                HttpRequest request = getHttpRequest(synchronousConfig, api, "get");

                // param 信息
                request.form("cid", synchronousCid)
                        .form("containsEnd", isContainsAfterContestJudge);

                HttpResponse response = request.execute();
                String synchronousRankJson = response.body();

                // System.out.println(synchronousRankJson);

                JSONObject JsonObject = new JSONObject(synchronousRankJson);

                int status = JsonObject.getInt("status");
                if (status == 200) {
                    JSONArray records = JsonObject.getJSONArray("data");

                    for (int i = 0; i < records.size(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        ContestProblemVO contestProblemVO = parseSynchronousContestProblem(record);
                        synchronousContestProblemList.add(contestProblemVO);
                    }
                }
            } catch (Exception e) {
                // 处理异常情况，可以记录日志等
                e.printStackTrace();
            }
        }
        return synchronousContestProblemList;
    }

    public Judge getSynchronousSubmissionDetail(Long submitId, Long cid) {
        Judge judge = new Judge();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest != null && contest.getSynchronous()) {
            // 获取比赛对应的同步赛信息
            JSONObject SynchronousJsonObject = JSONUtil.parseObj(contest.getSynchronousConfig());
            List<JSONObject> synchronousConfigList = SynchronousJsonObject.get("config", List.class);

            for (JSONObject object : synchronousConfigList) {
                try {
                    ContestSynchronousConfigVO synchronousConfig = JSONUtil.toBean(object,
                            ContestSynchronousConfigVO.class);

                    String api = "/api/get-submission-detail";
                    HttpRequest request = getHttpRequest(synchronousConfig, api, "get");

                    // param 信息
                    request.form("submitId", submitId.toString());

                    HttpResponse response = request.execute();
                    String synchronousRankJson = response.body();

                    // System.out.println(synchronousRankJson);

                    JSONObject JsonObject = new JSONObject(synchronousRankJson);

                    int status = JsonObject.getInt("status");
                    if (status == 200) {
                        JSONObject data = JsonObject.getJSONObject("data");
                        JSONObject record = data.getJSONObject("submission");
                        judge = parseSynchronousSubmissionDetail(record);
                    }
                } catch (Exception e) {
                    // 处理异常情况，可以记录日志等
                    e.printStackTrace();
                }
            }
        }
        return judge;
    }

    public Problem getSynchronousProblem(String displayId, Long cid) {
        Problem problem = new Problem();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest != null && contest.getSynchronous()) {
            // 获取比赛对应的同步赛信息
            JSONObject SynchronousJsonObject = JSONUtil.parseObj(contest.getSynchronousConfig());
            List<JSONObject> synchronousConfigList = SynchronousJsonObject.get("config", List.class);

            for (JSONObject object : synchronousConfigList) {
                try {
                    ContestSynchronousConfigVO synchronousConfig = JSONUtil.toBean(object,
                            ContestSynchronousConfigVO.class);

                    String api = "/api/get-contest-problem-details";
                    HttpRequest request = getHttpRequest(synchronousConfig, api, "get");

                    String contestUrl = synchronousConfig.getLink();
                    String synchronousCid = getCid(contestUrl);

                    displayId = displayId.split("_")[1];
                    // param 信息
                    request.form("displayId", displayId)
                            .form("cid", synchronousCid).form("containsEnd", "true");

                    HttpResponse response = request.execute();
                    String synchronousRankJson = response.body();

                    // System.out.println(synchronousRankJson);

                    JSONObject JsonObject = new JSONObject(synchronousRankJson);

                    int status = JsonObject.getInt("status");
                    if (status == 200) {
                        JSONObject data = JsonObject.getJSONObject("data");
                        JSONObject record = data.getJSONObject("problem");
                        problem = parseSynchronousProblem(record);
                    }
                } catch (Exception e) {
                    // 处理异常情况，可以记录日志等
                    e.printStackTrace();
                }
            }
        }
        return problem;
    }

    public List<JudgeCase> getSynchronousCaseResultList(Long submitId, Long cid) {
        List<JudgeCase> synchronousCaseResult = new ArrayList();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest != null && contest.getSynchronous()) {
            // 获取比赛对应的同步赛信息
            JSONObject SynchronousJsonObject = JSONUtil.parseObj(contest.getSynchronousConfig());
            List<JSONObject> synchronousConfigList = SynchronousJsonObject.get("config", List.class);

            for (JSONObject object : synchronousConfigList) {
                try {
                    ContestSynchronousConfigVO synchronousConfig = JSONUtil.toBean(object,
                            ContestSynchronousConfigVO.class);

                    String api = "/api/get-all-case-result";
                    HttpRequest request = getHttpRequest(synchronousConfig, api, "get");

                    // param 信息
                    request.form("submitId", submitId.toString());

                    HttpResponse response = request.execute();
                    String synchronousRankJson = response.body();

                    // System.out.println(synchronousRankJson);

                    JSONObject JsonObject = new JSONObject(synchronousRankJson);

                    int status = JsonObject.getInt("status");
                    if (status == 200) {
                        JSONObject data = JsonObject.getJSONObject("data");
                        JSONArray records = data.getJSONArray("judgeCaseList");

                        for (int i = 0; i < records.size(); i++) {
                            JSONObject record = records.getJSONObject(i);
                            JudgeCase judgeCase = parseSynchronousCaseResult(record);
                            synchronousCaseResult.add(judgeCase);
                        }
                    }
                } catch (Exception e) {
                    // 处理异常情况，可以记录日志等
                    e.printStackTrace();
                }
            }
        }
        return synchronousCaseResult;
    }

    public static JudgeVO parseSynchronousSubmission(JSONObject record) {
        JudgeVO judgeVO = new JudgeVO();
        judgeVO.setUid(record.getStr("uid"))
                .setSubmitId(record.getLong("submitId"))
                .setUsername(record.getStr("username"))
                .setPid(record.getLong("pid"))
                .setDisplayPid(record.getStr("displayPid"))
                .setTitle(record.getStr("title"))
                .setDisplayId(record.getStr("displayId"))
                .setSubmitTime(record.getDate("submitTime"))
                .setStatus(record.getInt("status"))
                .setShare(record.getBool("share"))
                .setTime(record.getInt("time"))
                .setMemory(record.getInt("memory"))
                .setScore(record.getInt("score"))
                .setOiRankScore(record.getInt("oiRankScore"))
                .setLength(record.getInt("length"))
                .setLanguage(record.getStr("language"))
                .setCid(record.getLong("cid"))
                .setCpid(record.getLong("cpid"))
                .setSource(record.getStr("source"))
                .setJudger(record.getStr("judger"))
                .setIp(record.getStr("ip"))
                .setIsManual(record.getBool("isManual"))
                .setRemote(true);
        return judgeVO;
    }

    public static ACMContestRankVO parseSynchronousRank(JSONObject record) {
        ACMContestRankVO rankVO = new ACMContestRankVO();
        rankVO.setUid(record.getStr("uid"))
                .setUsername(record.getStr("username"))
                .setRealname(record.getStr("realname"))
                .setNickname(record.getStr("nickname"))
                .setSchool(record.getStr("school"))
                .setGender(record.getStr("gender"))
                .setAvatar(record.getStr("avatar"))
                .setTotalTime(record.getLong("totalTime"))
                .setTotal(record.getInt("total"))
                .setAc(record.getInt("ac"))
                .setRemote(true);

        JSONObject submissionInfo = record.getJSONObject("submissionInfo");
        HashMap<String, HashMap<String, Object>> submissionInfoMap = new HashMap<>();
        for (String key : submissionInfo.keySet()) {
            JSONObject submissionDetail = submissionInfo.getJSONObject(key);
            HashMap<String, Object> submissionDetailMap = new HashMap<>();
            submissionDetailMap.put("errorNum", submissionDetail.getInt("errorNum"));
            submissionDetailMap.put("isAC", submissionDetail.getBool("isAC"));
            submissionDetailMap.put("ACTime", submissionDetail.getLong("ACTime"));
            submissionDetailMap.put("isFirstAC", submissionDetail.getBool("isFirstAC"));
            submissionInfoMap.put(key, submissionDetailMap);
        }
        rankVO.setSubmissionInfo(submissionInfoMap);

        return rankVO;
    }

    public static ContestProblemVO parseSynchronousContestProblem(JSONObject record) {
        ContestProblemVO contestProblemVO = new ContestProblemVO();
        contestProblemVO.setId(record.getLong("id"))
                .setDisplayId(record.getStr("displayId"))
                .setCid(record.getLong("cid"))
                .setPid(record.getLong("pid"))
                .setDisplayTitle(record.getStr("displayTitle"))
                .setColor(record.getStr("color"))
                .setAc(record.getInt("ac"))
                .setTotal(record.getInt("total"));

        return contestProblemVO;
    }

    public static Judge parseSynchronousSubmissionDetail(JSONObject record) {
        Judge judge = new Judge();
        judge.setSubmitId(record.getLong("submitId"))
                .setPid(record.getLong("pid"))
                .setDisplayPid(record.getStr("displayPid"))
                .setUid(record.getStr("uid"))
                .setUsername(record.getStr("username"))
                .setSubmitTime(record.getDate("submitTime"))
                .setStatus(record.getInt("status"))
                .setShare(record.getBool("share"))
                .setErrorMessage(record.getStr("errorMessage"))
                .setTime(record.getInt("time"))
                .setMemory(record.getInt("memory"))
                .setScore(record.getInt("score"))
                .setLength(record.getInt("length"))
                .setCode(record.getStr("code"))
                .setLanguage(record.getStr("language"))
                .setCid(record.getLong("cid"))
                .setCpid(record.getLong("cpid"))
                .setGid(record.getLong("gid"))
                .setJudger(record.getStr("judger"))
                .setIp(record.getStr("ip"))
                .setVersion(record.getInt("version"))
                .setOiRankScore(record.getInt("oiRankScore"))
                .setVjudgeSubmitId(record.getLong("vjudgeSubmitId"))
                .setVjudgeUsername(record.getStr("vjudgeUsername"))
                .setVjudgePassword(record.getStr("vjudgePassword"))
                .setIsManual(record.getBool("isManual"))
                .setGmtCreate(record.getDate("gmtCreate"))
                .setGmtModified(record.getDate("gmtModified"));
        return judge;
    }

    public static JudgeCase parseSynchronousCaseResult(JSONObject record) {
        JudgeCase judgeCase = new JudgeCase();
        judgeCase.setPid(record.getLong("pid"))
                .setSubmitId(record.getLong("submitId"))
                .setUid(record.getStr("uid"))
                .setCaseId(record.getLong("caseId"))
                .setTime(record.getInt("time"))
                .setStatus(record.getInt("status"))
                .setMemory(record.getInt("memory"))
                .setScore(record.getInt("score"))
                .setStatus(record.getInt("status"))
                .setInputData(record.getStr("inputData"))
                .setOutputData(record.getStr("outputData"))
                .setUserOutput(record.getStr("userOutput"))
                .setGroupNum(record.getInt("groupNum"))
                .setSeq(record.getInt("seq"))
                .setMode(record.getStr("mode"))
                .setGmtCreate(record.getDate("gmtCreate"))
                .setGmtModified(record.getDate("gmtModified"));
        return judgeCase;
    }

    public static Problem parseSynchronousProblem(JSONObject record) {
        Problem problem = new Problem();
        problem.setId(record.getLong("id"));
        problem.setProblemId(record.getStr("problemId"));
        problem.setTitle(record.getStr("title"));
        problem.setAuthor(record.getStr("author"));
        problem.setType(record.getInt("type"));
        problem.setJudgeMode(record.getStr("judgeMode"));
        problem.setJudgeCaseMode(record.getStr("judgeCaseMode"));
        problem.setTimeLimit(record.getInt("timeLimit"));
        problem.setMemoryLimit(record.getInt("memoryLimit"));
        problem.setStackLimit(record.getInt("stackLimit"));
        problem.setDescription(record.getStr("description"));
        problem.setInput(record.getStr("input"));
        problem.setOutput(record.getStr("output"));
        problem.setExamples(record.getStr("examples"));
        problem.setIsRemote(record.getBool("isRemote"));
        problem.setSource(record.getStr("source"));
        problem.setDifficulty(record.getInt("difficulty"));
        problem.setHint(record.getStr("hint"));
        problem.setAuth(record.getInt("auth"));
        problem.setIoScore(record.getInt("ioScore"));
        problem.setCodeShare(record.getBool("codeShare"));
        problem.setSpjCode(record.getStr("spjCode", null));
        problem.setSpjLanguage(record.getStr("spjLanguage", null));
        problem.setUserExtraFile(record.getStr("userExtraFile", null));
        problem.setJudgeExtraFile(record.getStr("judgeExtraFile", null));
        problem.setIsRemoveEndBlank(record.getBool("isRemoveEndBlank"));
        problem.setOpenCaseResult(record.getBool("openCaseResult"));
        problem.setIsUploadCase(record.getBool("isUploadCase"));
        problem.setCaseVersion(record.getStr("caseVersion"));
        problem.setModifiedUser(record.getStr("modifiedUser"));
        problem.setIsGroup(record.getBool("isGroup"));
        problem.setGid(record.getLong("gid", null));
        problem.setApplyPublicProgress(record.getInt("applyPublicProgress", null));
        problem.setIsFileIO(record.getBool("isFileIO"));
        problem.setIoReadFileName(record.getStr("ioReadFileName", null));
        problem.setIoWriteFileName(record.getStr("ioWriteFileName", null));

        return problem;
    }

}
