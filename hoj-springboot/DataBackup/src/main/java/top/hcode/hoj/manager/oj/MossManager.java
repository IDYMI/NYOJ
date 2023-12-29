package top.hcode.hoj.manager.oj;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONArray;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestMossEntityService;
import top.hcode.hoj.dao.contest.ContestMossResultEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.common.exception.MossException;
import top.hcode.hoj.manager.file.ContestFileManager;
import top.hcode.hoj.pojo.dto.ContestMossImportDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestMoss;
import top.hcode.hoj.pojo.entity.contest.ContestMossResult;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.pojo.vo.ContestMossImportVO;
import top.hcode.hoj.pojo.vo.ContestMossListVO;
import top.hcode.hoj.pojo.vo.ContestMossResultIndexVO;
import top.hcode.hoj.pojo.vo.ContestMossResultVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.GroupValidator;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.Moss;
import top.hcode.hoj.utils.MossGetLanguage;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.concurrent.ForkJoinPool;

import javax.annotation.Resource;

/**
 *
 * @Description:
 */
@Component
@Slf4j
public class MossManager {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestMossEntityService contestMossEntityService;

    @Autowired
    private ContestMossResultEntityService contestMossResultEntityService;

    @Resource
    private AccountManager accountManager;

    @Resource
    private ContestFileManager contestFileManager;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    public List<String> getContestLanguage(Long cid, Boolean excludeAdmin)
            throws StatusFailException, StatusForbiddenException, AccessException {

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        Long gid = contest.getGid();
        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权查看该比赛提交代码的所有语言！");
        }

        boolean isACM = contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode();

        List<Judge> judgeList = contestFileManager.getJudgeList(isACM, cid, excludeAdmin);

        List<String> languageList = judgeList.stream()
                .map(judge -> contestFileManager.languageToFileSuffix(judge.getLanguage().toLowerCase()))
                .distinct()
                .collect(Collectors.toList());

        return languageList;
    }

    public List<ContestMossListVO> getMossDateList(Long cid, String language)
            throws StatusFailException, StatusForbiddenException, AccessException {

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        Long gid = contest.getGid();
        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权查看该比赛 Moss 的结果！");
        }

        QueryWrapper<ContestMoss> contestMossHtmlQueryWrapper = new QueryWrapper<>();

        contestMossHtmlQueryWrapper.select("html", "language", "MAX(gmt_create) as latest_gmt_create")
                .eq("cid", cid)
                .groupBy("html")
                .groupBy("language")
                .orderByDesc("latest_gmt_create"); // 添加排序条件;

        if (!StringUtils.isEmpty(language)) {
            contestMossHtmlQueryWrapper.eq("language", language);
        }

        List<Map<String, Object>> contestMossListMap = contestMossEntityService.listMaps(contestMossHtmlQueryWrapper);

        List<ContestMossListVO> contestMossListVoList = contestMossListMap.stream()
                .map(contestMossMap -> {
                    ContestMossListVO contestMossListVo = new ContestMossListVO();
                    contestMossListVo.setHtml((String) contestMossMap.get("html"));
                    contestMossListVo.setLanguage((String) contestMossMap.get("language"));
                    contestMossListVo.setGmtCreate(getDateToString((Date) contestMossMap.get("latest_gmt_create")));
                    return contestMossListVo;
                })
                .collect(Collectors.toList());

        return contestMossListVoList;
    }

    public List<String> addMoss(ContestMossImportDTO contestMossImportDTO)
            throws StatusFailException, StatusForbiddenException, AccessException, MossException, IOException,
            UnknownHostException {

        Long cid = contestMossImportDTO.getCid();
        List<String> modeList = contestMossImportDTO.getModeList();

        Boolean excludeAdmin = contestMossImportDTO.getExcludeAdmin();

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        Long gid = contest.getGid();
        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权提交Moss查重！");
        }

        boolean isACM = contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode();

        List<Judge> judgeList = contestFileManager.getJudgeList(isACM, cid, excludeAdmin);

        List<ContestProblem> contestProblemList = contestFileManager.getContestProblemList(cid);

        HashMap<String, Boolean> recordMap = new HashMap<>();
        /**
         * 以用户来分割提交的代码
         */
        List<String> usernameList = judgeList.stream()
                .filter(contestFileManager.distinctByKey(Judge::getUsername)) // 根据用户名过滤唯一
                .map(Judge::getUsername).collect(Collectors.toList()); // 映射出用户名列表

        HashMap<Long, String> cpIdMap = new HashMap<>();
        for (ContestProblem contestProblem : contestProblemList) {
            cpIdMap.put(contestProblem.getId(), contestProblem.getDisplayId());
        }

        HashMap<Long, String> displayTitleMap = new HashMap<>();
        for (ContestProblem contestProblem : contestProblemList) {
            displayTitleMap.put(contestProblem.getId(), contestProblem.getDisplayTitle());
        }

        // 上传 Moss 服务器的内容
        List<ContestMossImportVO> contestMossImportList = new ArrayList<>();
        for (String username : usernameList) {
            // 如果是ACM模式，则所有提交代码都要生成，如果同一题多次提交AC，加上提交Id ---> A_(666666).c
            // 如果是OI模式就生成最近一次提交即可，且带上分数 ---> A_(666666)_100.c
            List<Judge> userSubmissionList = judgeList.stream()
                    .filter(judge -> judge.getUsername().equals(username)) // 过滤出对应用户的提交
                    .sorted(Comparator.comparing(Judge::getSubmitTime).reversed()) // 根据提交时间进行降序
                    .collect(Collectors.toList());

            for (Judge judge : userSubmissionList) {
                String code = judge.getCode();
                String fileSuffix = contestFileManager.languageToFileSuffix(judge.getLanguage().toLowerCase());
                String filePath = username.replace(" ", "$") + File.separator
                        + cpIdMap.getOrDefault(judge.getCpid(), "null").replace(" ", "$") + "_"
                        + displayTitleMap.getOrDefault(judge.getCpid(), "null").replace(" ", "$");

                // OI模式只取最后一次提交
                if (!isACM) {
                    String key = judge.getUsername() + "_" + judge.getPid();
                    if (!recordMap.containsKey(key)) {
                        filePath += "_(" + judge.getSubmitId().toString() + ")" + judge.getScore() + "." + fileSuffix;

                        recordMap.put(key, true);
                    }

                } else {
                    filePath += "_(" + judge.getSubmitId().toString() + ")." + fileSuffix;
                }

                ContestMossImportVO contestMossImportVo = new ContestMossImportVO(filePath, code, fileSuffix);
                contestMossImportList.add(contestMossImportVo);
            }
        }

        if (CollectionUtils.isEmpty(contestMossImportList)) {
            throw new StatusFailException("错误：该比赛不存在通过代码！");
        }

        List<String> resultUrlList = new ArrayList<>();

        // 如果选择语言则但语言，默认为全语言查重
        List<String> languages = (!CollectionUtils.isEmpty(modeList)) ? modeList
                : getContestLanguage(cid, excludeAdmin);

        for (String language : languages) {
            // 刷选出结果中的对应语言提交记录
            List<ContestMossImportVO> newList = contestMossImportList.stream()
                    .filter(vo -> vo.getLanguage().equals(language))
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(newList)) {
                String result_url = getMossResultLink(language, newList);
                if (!StringUtils.isEmpty(result_url)) {
                    saveMoss(cid, language, result_url);
                    resultUrlList.add(result_url);
                }
            }
        }

        return resultUrlList;
    }

    public IPage<ContestMoss> getMoss(Long cid, Integer currentPage, Integer limit, String keyword, String language,
            String time)
            throws StatusFailException, StatusForbiddenException, AccessException {

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        Long gid = contest.getGid();
        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权查看Moss查重！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取ContestMoss列表
        QueryWrapper<ContestMoss> contestMossQueryWrapper = new QueryWrapper<>();
        contestMossQueryWrapper.eq("cid", contest.getId());

        // 获取Moss查重的所有结果
        List<ContestMossListVO> mossDateList = getMossDateList(cid, language);

        if (!CollectionUtils.isEmpty(mossDateList)) {
            // time 和 language 都为空则，返回最后一次所有语言的结果
            // time 为空 language 不为空，返回最后一次对应代码的结果
            if (StringUtils.isEmpty(time)) {
                List<String> htmlList = getLastHrefList(cid, language);
                contestMossQueryWrapper.in("html", htmlList);
            }
            // time 不为空 language 为空，返回对应时间段所有语言的结果
            // time 和 language 都不为空，返回对应时间段的对应语言结果
            else {
                mossDateList.stream()
                        .filter(contestMossListVo -> time.equals(contestMossListVo.getGmtCreate()))
                        .findFirst()
                        .ifPresent(
                                contestMossListVo -> contestMossQueryWrapper.eq("html", contestMossListVo.getHtml()));
            }

            // 添加 length 和 percent1 和 percent2 的降序排序
            contestMossQueryWrapper.orderByDesc("length", "percent1", "percent2").orderByDesc("gmt_create");

            // 关键词检索
            if (!StringUtils.isEmpty(keyword)) {
                String fakeyword = keyword.trim();
                contestMossQueryWrapper.and(
                        // 添加 username1 或者 username2 字段等于 username 的条件
                        wrapper -> wrapper.eq("username1", fakeyword).or().eq("username2", fakeyword));
            }

        }

        IPage<ContestMoss> contestMossIPage = new Page<>(currentPage, limit);

        return contestMossEntityService.page(contestMossIPage, contestMossQueryWrapper);
    }

    public ContestMossResultVO getMossResult(Long id, Long cid)
            throws StatusFailException, StatusForbiddenException, AccessException {

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        Long gid = contest.getGid();
        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权查看Moss查重！");
        }

        QueryWrapper<ContestMoss> contestMossQueryWrapper = new QueryWrapper<>();
        contestMossQueryWrapper.eq("id", id).eq("cid", contest.getId());
        ContestMoss contestMoss = contestMossEntityService.getOne(contestMossQueryWrapper, false);

        // TODO 将 contestMossResult 转化为 contestMossResultVo
        ContestMossResultVO contestMossResultVo = new ContestMossResultVO();

        if (contestMoss != null) {
            String username1 = contestMoss.getUsername1();
            String username2 = contestMoss.getUsername2();
            Long percent1 = contestMoss.getPercent1();
            Long percent2 = contestMoss.getPercent2();
            Long mossId = contestMoss.getId();
            String href = contestMoss.getHref();
            // 获取获取ContestMossResult列表
            QueryWrapper<ContestMossResult> contestMossResultQueryWrapper = new QueryWrapper<>();
            contestMossResultQueryWrapper.eq("cid", contest.getId()).eq("href", href);

            ContestMossResult contestMossResult = contestMossResultEntityService.getOne(
                    contestMossResultQueryWrapper,
                    false);

            Integer len = getConfigList(contestMossResult.getIcon1()).size();
            String code1 = replaceCodeHref(mossId, len, true, contestMossResult.getCode1());
            String code2 = replaceCodeHref(mossId, len, false, contestMossResult.getCode2());

            contestMossResultVo.setId(contestMossResult.getId());
            contestMossResultVo.setCid(contestMossResult.getCid());
            contestMossResultVo.setUsername1(username1);
            contestMossResultVo.setPercent1(percent1);
            contestMossResultVo.setUsername2(username2);
            contestMossResultVo.setPercent2(percent2);
            contestMossResultVo.setHref(contestMossResult.getHref());
            contestMossResultVo.setCode1(code1);
            contestMossResultVo.setCode2(code2);

            List<ContestMossResultIndexVO> indexList = IntStream
                    .range(0, len)
                    .mapToObj(i -> new ContestMossResultIndexVO(
                            getConfigList(contestMossResult.getCol1()).get(i),
                            getConfigList(contestMossResult.getIcon1()).get(i),
                            getConfigList(contestMossResult.getCol2()).get(i),
                            getConfigList(contestMossResult.getIcon2()).get(i)))
                    .collect(Collectors.toList());

            contestMossResultVo.setIndexList(indexList);
        }

        return contestMossResultVo;
    }

    public String getMossResultLink(String mode, List<ContestMossImportVO> userFiles)
            throws StatusFailException, MossException, IOException, UnknownHostException {
        try {

            Boolean accessible = isURLAccessible("moss.stanford.edu");

            if (accessible) {
                throw new StatusFailException("错误：无法连接到Moss服务器，请稍等后重试！");
            }

            // Moss 查重的用户Id
            String userId = Constants.MossUser.UserId.getMode();

            // 查重的语言
            String lan = MossGetLanguage.languageToExtension.get(mode);

            if (StringUtils.isEmpty(lan)) {
                throw new StatusFailException("错误：不支持该语言( " + mode + " )！");
            }

            // 获取一个与Moss服务器通信的新套接字客户端并设置其参数。
            Moss socketClient = new Moss();

            // 设置您的Moss用户ID
            socketClient.setUserID(userId);

            // 设置所有学生源代码的编程语言
            socketClient.setLanguage(lan);

            // 初始化连接并发送参数
            socketClient.run();

            // 上传学生的所有源文件
            for (ContestMossImportVO f : userFiles) {
                socketClient.uploadString(f.getFilename(), f.getCode());
            }

            // 上传完成，告诉服务器检查文件
            socketClient.sendQuery();

            // 获取带有Moss结果的URL并对其执行操作
            URL results = socketClient.getResultURL();

            return results.toString();
        } catch (Exception e) {
            log.error("moss error:", e);
            return null;
        }
    }

    public void saveMoss(Long cid, String mode, String resultUrl) throws IOException, StatusFailException {
        // 从 URL 加载 HTML 内容
        Document doc = Jsoup.connect(resultUrl).get();

        // 得到 <tbody> 中的需要内容
        List<String> contentList = getHtmlContent(mode, cid, resultUrl, doc);

        if (!CollectionUtils.isEmpty(contentList)) {
            // 使用并行流处理多个链接
            ForkJoinPool forkJoinPool = new ForkJoinPool(contentList.size());
            forkJoinPool.submit(() -> contentList.parallelStream().forEach(content -> {
                try {
                    Document top_doc = Jsoup.connect(getMossResultUrl(content, "top")).get();
                    getHrefContent(cid, mode, top_doc, content);
                } catch (IOException e) {
                    log.error("moss error:", e);
                    e.printStackTrace();
                }
            })).join();
        }
    }

    private List<String> getHtmlContent(String mode, Long cid, String resultUrl, Document doc)
            throws StatusFailException {
        List<String> contentList = new ArrayList<>();
        Elements aTags = doc.select("tbody a");

        if (aTags.size() < 2) { // 没有查重结果
            return contentList;
        }

        for (int i = 0; i < aTags.size(); i += 2) {
            Element tag1 = aTags.get(i);
            Element tag2 = aTags.get(i + 1);

            String href = extractHref(doc.select("a[href]").get(i).absUrl("href"));
            if (!StringUtils.isEmpty(href)) {

                String username1 = (extractUidPercent(tag1.text())[0]).replace("$", " ");
                Long percent1 = Long.valueOf(extractUidPercent(tag1.text())[1].replaceAll("[^\\d.]", ""));
                String username2 = (extractUidPercent(tag2.text())[0]).replace("$", " ");
                Long percent2 = Long.valueOf(extractUidPercent(tag2.text())[1].replaceAll("[^\\d.]", ""));
                Long length = Long.valueOf(doc.select("td[align=right]").get(i / 2).text());

                Boolean isOk = contestMossEntityService.saveOrUpdate(new ContestMoss()
                        .setCid(cid)
                        .setHtml(resultUrl)
                        .setUsername1(username1)
                        .setUid1(getUserUid(username1))
                        .setPercent1(percent1)
                        .setUsername2(username2)
                        .setUid2(getUserUid(username2))
                        .setPercent2(percent2)
                        .setLength(length)
                        .setHref(href)
                        .setLanguage(mode));
                if (!isOk) {
                    throw new StatusFailException("添加失败");
                }

                contentList.add(href);
            }
        }

        return contentList;
    }

    private void getHrefContent(Long cid, String mode, Document doc, String href) throws IOException {
        List<String> colList1 = new ArrayList<>();
        List<String> iconList1 = new ArrayList<>();
        List<String> colList2 = new ArrayList<>();
        List<String> iconList2 = new ArrayList<>();

        Elements aTags = doc.select("td > a");

        String href1 = aTags.get(0).attr("href");
        String code1 = getCode(cid, href1, mode);

        String href2 = aTags.get(2).attr("href");
        String code2 = getCode(cid, href2, mode);

        for (int i = 0; i < aTags.size(); i += 4) {
            colList1.add(aTags.get(i).text());
            iconList1.add(aTags.get(i + 1).select("img").attr("src"));

            colList2.add(aTags.get(i + 2).text());
            iconList2.add(aTags.get(i + 3).select("img").attr("src"));
        }

        String col1 = getConfigJson(colList1);
        String icon1 = getConfigJson(iconList1);
        String col2 = getConfigJson(colList2);
        String icon2 = getConfigJson(iconList2);
        // 保存到 contestMossResult
        contestMossResultEntityService.saveOrUpdate(new ContestMossResult()
                .setCid(cid)
                .setHref(href)
                .setCol1(col1)
                .setIcon1(icon1)
                .setCode1(code1)
                .setCol2(col2)
                .setIcon2(icon2)
                .setCode2(code2));
    }

    public static String getCode(Long cid, String hrefUrl, String mode) throws IOException {

        Document doc = Jsoup.connect(hrefUrl).get();

        Elements preElements = doc.select("pre");

        if (!preElements.isEmpty()) {
            // 将 html 转义为字符串
            // String preElement =
            // StringEscapeUtils.unescapeHtml4(preElements.get(0).toString());

            // 替换 <pre> 开始标签, </pre> 结束标签
            String preElement = preElements.get(0).toString().replace("<pre>", "").replace("</pre>", "");

            // 将所有的 file：后的内容替换为超链接
            preElement = replaceFileInfo(cid, preElement, mode);

            return preElement;
        }
        return new String();
    }

    public static String replaceFileInfo(Long cid, String code, String fileSuffix) {

        String pattern = "&gt;&gt;&gt;&gt; file: ([^_]+)_([^_]+)_\\((\\d+)\\)(\\d*)\\.\\w+";

        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(code);
        StringBuffer modifiedStringBuffer = new StringBuffer();

        // 循环遍历并替换所有匹配
        while (matcher.find()) {
            String displayId = Matcher.quoteReplacement(matcher.group(1)).replace("$", " ");
            String title = Matcher.quoteReplacement(matcher.group(2)).replace("$", " ");
            Long submitId = Long.valueOf(matcher.group(3));
            String score = matcher.group(4);

            // 如果 score 为空，则设置默认值或进行其他处理
            if (score.isEmpty()) {
                score = ""; // 或者根据需要设置默认值
            }

            String host = "";
            String contestProblemUrl = host + "/contest/" + String.valueOf(cid) + "/problem/" + displayId;
            String contestProblemCodeUrl = contestProblemUrl + "/submission-detail/" + submitId;

            // 构建替换的字符串
            String replacement = "<a href=\"" + Matcher.quoteReplacement(contestProblemUrl) + "\">" + displayId + "_"
                    + title + "</a>" + "_("
                    + "<a href=\"" + Matcher.quoteReplacement(contestProblemCodeUrl) + "\">" + submitId + "</a>"
                    + ")" + score + "." + fileSuffix;

            // 将匹配的内容替换为新字符串
            matcher.appendReplacement(modifiedStringBuffer, replacement);
        }

        // 将未匹配的部分追加到末尾
        matcher.appendTail(modifiedStringBuffer);

        String modifiedString = modifiedStringBuffer.toString();
        return modifiedString;
    }

    public static String replaceCodeHref(Long id, Integer length, Boolean isFirst, String code) {
        // 正则表达式匹配包含 <a> 元素的字符串，提取 href 属性的值
        String pattern = "<a\\s+[^>]*href=\"([^\"]*)\"[^>]*target=\"[^\"]*\"[^>]*>";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(code);

        // 用于保存替换后的字符串
        StringBuffer result = new StringBuffer();

        // 遍历匹配结果并进行替换
        while (matcher.find()) {
            String href = matcher.group(1); // 获取 href 属性的值
            int hashIndex = href.indexOf('#');
            String hashAndBeyond = (hashIndex != -1) ? href.substring(hashIndex + 1) : "";

            // 将 length 和 extractHashAndBeyond 解析的值相加后转为字符串
            String replacement = matcher.group()
                    .replace(href,
                            String.valueOf(id) + "#"
                                    + String.valueOf((isFirst ? length : 0) + Integer.parseInt(hashAndBeyond)))
                    .replaceFirst(" target=\"[^\"]*\"", ""); // 去除 target 属性;

            matcher.appendReplacement(result, replacement);
        }

        // 将剩余的部分追加到结果中
        matcher.appendTail(result);
        if (!isFirst) {
            return replaceCodeName(length, result.toString());
        }
        return result.toString();
    }

    public static String replaceCodeName(Integer length, String code) {
        // 正则表达式匹配包含 <a> 元素的字符串，提取 name 属性的值
        String pattern = "<a\\s+[^>]*name=\"([^\"]*)\"[^>]*>";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(code);

        // 用于保存替换后的字符串
        StringBuffer result = new StringBuffer();

        // 遍历匹配结果并进行替换
        while (matcher.find()) {
            String name = matcher.group(1); // 获取 name 属性的值
            String replacement = matcher.group().replace(name, String.valueOf(Integer.valueOf(name) + length));
            matcher.appendReplacement(result, replacement);
        }

        // 将剩余的部分追加到结果中
        matcher.appendTail(result);

        return result.toString();
    }

    public static boolean isURLAccessible(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");

            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getMossResultUrl(String originalUrl, String part) {
        return originalUrl.replaceFirst(".html$", "-" + part + ".html");
    }

    public static String getConfigJson(List<String> config) {
        JSONObject configJson = new JSONObject();
        configJson.set("config", config);
        return configJson.toString();
    }

    // 提取username和百分比
    private static String[] extractUidPercent(String href) {
        String[] parts = href.split("/");
        return (parts.length >= 2) ? new String[] { parts[parts.length - 2], parts[parts.length - 1] }
                : new String[] { "", "" };
    }

    // 提取匹配链接
    private static String extractHref(String link) {
        return link.contains("match") ? link : "";
    }

    private String getUserUid(String username) {
        UserRolesVO userRolesVo = userRoleEntityService.getUserRoles(null, username);
        return userRolesVo.getUid();
    }

    private List<String> getConfigList(String configListString) {
        JSONObject jsonObject = JSONUtil.parseObj(configListString);
        JSONArray jsonArray = jsonObject.getJSONArray("config");

        List<String> configList = jsonArray.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        return configList;
    }

    private String getDateToString(Date time) {
        String pattern = "MM-dd HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        // Convert Date to String
        String dateString = dateFormat.format(time);

        return dateString;
    }

    private List<String> getLastHrefList(Long cid, String language)
            throws StatusFailException, StatusForbiddenException, AccessException {
        // 获取所有语言的最后一次出现的结果
        List<ContestMossListVO> mossDateList = getMossDateList(cid, language);
        // 获取所有语言的最后一次出现的 HTML 列表
        return mossDateList.stream()
                .collect(Collectors.toMap(ContestMossListVO::getLanguage, ContestMossListVO::getHtml,
                        (existing, replacement) -> replacement))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}