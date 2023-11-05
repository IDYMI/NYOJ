package top.hcode.hoj.manager.oj;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.pojo.vo.ACMContestStatisticVO;
import top.hcode.hoj.pojo.vo.OIContestRankVO;
import top.hcode.hoj.pojo.vo.UserContestsRankingVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.ContestValidator;

import javax.annotation.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.*;

/**
 *
 * @Date: 2022/3/11 20:30
 * @Description:
 */
@Component
public class ContestRankManager {

    @Resource
    private ContestCalculateRankManager contestCalculateRankManager;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestValidator contestValidator;

    /**
     * @param isOpenSealRank              是否封榜
     * @param removeStar                  是否移除打星队伍
     * @param currentUserId               当前用户id
     * @param concernedList               关联比赛的id列表
     * @param contest                     比赛信息
     * @param currentPage                 当前页面
     * @param limit                       分页大小
     * @param keyword                     搜索关键词：匹配学校或榜单显示名称
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @param nowtime                     比赛跳转榜单的时间
     * @desc 获取ACM比赛排行榜
     */
    public IPage<ACMContestRankVO> getContestACMRankPage(Boolean isOpenSealRank,
            Boolean removeStar,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            Contest contest,
            int currentPage,
            int limit,
            String keyword,
            Boolean isContainsAfterContestJudge,
            Long nowtime) {

        List<ACMContestRankVO> orderResultList = getContestACMRankList(
                isOpenSealRank,
                removeStar,
                currentUserId,
                concernedList,
                externalCidList,
                contest,
                isContainsAfterContestJudge,
                nowtime);

        if (StrUtil.isNotBlank(keyword)) {
            String finalKeyword = keyword.trim().toLowerCase();
            orderResultList = orderResultList.stream()
                    .filter(rankVo -> filterBySchoolORRankShowName(finalKeyword,
                            rankVo.getSchool(),
                            getUserRankShowName(contest.getRankShowName(),
                                    rankVo.getUsername(),
                                    rankVo.getRealname(),
                                    rankVo.getNickname())))
                    .collect(Collectors.toList());
        }
        // 计算好排行榜，然后进行分页
        return getPagingRankList(orderResultList, currentPage, limit);
    }

    /**
     * @param isOpenSealRank              是否封榜
     * @param removeStar                  是否移除打星队伍
     * @param currentUserId               当前用户id
     * @param concernedList               关联比赛的id列表
     * @param contest                     比赛信息
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @param nowtime                     比赛跳转榜单的时间
     * @desc 获取ACM比赛排行榜
     */
    public List<ACMContestRankVO> getContestACMRankList(
            Boolean isOpenSealRank,
            Boolean removeStar,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            Contest contest,
            Boolean isContainsAfterContestJudge,
            Long nowtime) {

        // 进行排序计算
        List<ACMContestRankVO> orderResultList = contestCalculateRankManager.calcACMRank(
                isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                isContainsAfterContestJudge,
                nowtime);

        return orderResultList;
    }

    /**
     * @param uid
     * @param username
     * @return
     * @Description 获取用户最近一年的比赛名次变化图
     */
    public UserContestsRankingVO getRecentYearContestsRanking(
            List<Long> contestsPidList,
            String uid,
            String username) throws StatusFailException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (StringUtils.isEmpty(uid) && StringUtils.isEmpty(username)) {
            if (userRolesVo != null) {
                uid = userRolesVo.getUid();
            } else {
                throw new StatusFailException("请求参数错误：uid和username不能都为空！");
            }
        }

        UserContestsRankingVO userContestsRankingVO = new UserContestsRankingVO();
        userContestsRankingVO.setEndDate(DateUtil.format(new Date(), "yyyy-MM-dd"));

        if (CollectionUtils.isEmpty(contestsPidList)) {
            userContestsRankingVO.setDataList(new ArrayList<>());
            userContestsRankingVO.setSolvedList(new ArrayList<>());
            return userContestsRankingVO;
        }

        List<HashMap<String, Object>> dataList = new ArrayList<>();
        List<Long> contestPids = new ArrayList<>();
        for (Long contestPid : contestsPidList) {
            Contest contest = contestEntityService.getById(contestPid);

            List<ACMContestRankVO> orderResultList = getContestACMRankList(
                    false,
                    true,
                    userRolesVo.getUid(),
                    null,
                    null,
                    contest,
                    false,
                    null);
            String keyword = username;

            if (StrUtil.isNotBlank(keyword)) {
                String finalKeyword = keyword.trim().toLowerCase();
                orderResultList = orderResultList.stream()
                        .filter(rankVo -> filterBySchoolORRankShowName(finalKeyword,
                                rankVo.getSchool(),
                                getUserRankShowName(contest.getRankShowName(),
                                        rankVo.getUsername(),
                                        rankVo.getRealname(),
                                        rankVo.getNickname())))
                        .collect(Collectors.toList());
            }

            if (orderResultList.size() > 0) {
                String user_uid = orderResultList.get(0).getUid();
                if (user_uid.equals(uid)) {
                    contestPids.add(contest.getId());
                    Integer rank = orderResultList.get(0).getRank();
                    Date startTime = contest.getStartTime();
                    String dateStr = DateUtil.format(startTime, "yyyy-MM-dd");
                    HashMap<String, Object> tmp = new HashMap<>(4);
                    tmp.put("date", dateStr);
                    tmp.put("rank", rank);
                    tmp.put("cid", contest.getId());
                    tmp.put("title", contest.getTitle());
                    dataList.add(tmp);
                }
            }
        }

        if (CollectionUtils.isEmpty(dataList)) {
            userContestsRankingVO.setSolvedList(new ArrayList<>());
            userContestsRankingVO.setDataList(new ArrayList<>());
            return userContestsRankingVO;
        }
        userContestsRankingVO.setSolvedList(contestPids);
        userContestsRankingVO.setDataList(dataList);
        return userContestsRankingVO;
    }

    /**
     * @param cids        查询比赛的cid列表
     * @param currentPage 当前页面
     * @param limit       分页大小
     * @param keyword     搜索关键词：匹配学校或榜单显示名称
     * @desc 获取ACM比赛排行榜
     */
    public IPage<ACMContestStatisticVO> getContestStatisticPage(
            String cids,
            int currentPage,
            int limit,
            String keyword) throws StatusFailException, StatusForbiddenException {

        // 使用 '+' 分割字符串，然后将每个段转换为Long类型
        List<Long> contest_cids = Arrays.stream(cids.split("\\+"))
                .map(segment -> {
                    try {
                        long value = Long.parseLong(segment);
                        return (value >= 0) ? value : -1L;
                    } catch (NumberFormatException e) {
                        return -1L;
                    }
                })
                .collect(Collectors.toList());

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        List<ACMContestStatisticVO> resultList = new ArrayList<>();

        int index = 0;

        for (int i = 0; i < contest_cids.size(); i++) {
            Long cid = contest_cids.get(i);
            if (cid == -1L) {
                throw new StatusFailException("错误，请输入正确的 cid, 对应错误 cid: " + cids.split("\\+")[i] + "无效");
            }

            Contest contest = contestEntityService.getById(cid);
            if (contest == null) { // 查询不存在
                throw new StatusFailException("错误：cid对应比赛不存在, 对应错误 cid: " + cids.split("\\+")[i] + "无效");
            }

            Boolean isACM = (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode());

            if (!isACM) {
                throw new StatusFailException("错误：cid对应比赛不为ACM类型, 对应错误 cid: " + cids.split("\\+")[i] + "无效");
            }

            // 超级管理员或者该比赛的创建者，则为比赛管理者
            // 是否为超级管理员或者题目管理或者普通管理
            boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                    || SecurityUtils.getSubject().hasRole("problem_admin")
                    || SecurityUtils.getSubject().hasRole("admin");

            // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
            contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

            // 进行排序计算得到用户的排名
            List<ACMContestRankVO> orderResultList = getContestACMRankList(
                    false,
                    true,
                    userRolesVo.getUid(),
                    null,
                    null,
                    contest,
                    false,
                    null);

            // 将ACMContestRankVO 转化为 ACMContestStatisticVO
            if (orderResultList.size() > 0) {

                for (int j = 0; j < orderResultList.size(); j++) {
                    ACMContestStatisticVO ACMContestStatisticVo;

                    if (!uidMapIndex.containsKey(orderResultList.get(j).getUid())) { // 如果该用户信息没还记录
                        // 初始化参数
                        ACMContestStatisticVo = new ACMContestStatisticVO();
                        ACMContestStatisticVo.setRealname(orderResultList.get(j).getRealname())
                                .setAvatar(orderResultList.get(j).getAvatar())
                                .setSchool(orderResultList.get(j).getSchool())
                                .setGender(orderResultList.get(j).getGender())
                                .setUid(orderResultList.get(j).getUid())
                                .setUsername(orderResultList.get(j).getUsername())
                                .setNickname(orderResultList.get(j).getNickname())
                                .setAc(0)
                                .setTotalTime(0L)
                                .setTotal(0);

                        HashMap<String, HashMap<String, Object>> contestInfo = new HashMap<>();

                        ACMContestStatisticVo.setContestInfo(contestInfo);

                        resultList.add(ACMContestStatisticVo);
                        uidMapIndex.put(orderResultList.get(j).getUid(), index);
                        index++;
                    } else {
                        ACMContestStatisticVo = resultList.get(uidMapIndex.get(orderResultList.get(j).getUid())); // 根据记录的index进行获取
                    }

                    // 将该场比赛的总AC计入
                    ACMContestStatisticVo.setAc(ACMContestStatisticVo.getAc() + orderResultList.get(j).getAc());
                    // 将该场比赛的总罚时计入
                    ACMContestStatisticVo
                            .setTotalTime(ACMContestStatisticVo.getTotalTime() + orderResultList.get(j).getTotalTime());
                    // 将该场比赛的总提交数计入
                    ACMContestStatisticVo
                            .setTotal(ACMContestStatisticVo.getTotal() + orderResultList.get(j).getTotal());

                    HashMap<String, Object> contestInfo = ACMContestStatisticVo.getContestInfo()
                            .get(contest.getId().toString());

                    if (contestInfo == null) {
                        contestInfo = new HashMap<>();
                        contestInfo.put("title", contest.getTitle());
                        contestInfo.put("AC", orderResultList.get(j).getAc());
                        contestInfo.put("TotalTime", orderResultList.get(j).getTotalTime());
                    }

                    // 计入比赛信息
                    ACMContestStatisticVo.getContestInfo().put(contest.getId().toString(), contestInfo);
                }
            }
        }

        // 重新排序
        List<ACMContestStatisticVO> result = resultList.stream()
                .sorted(Comparator.comparing(ACMContestStatisticVO::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                        .thenComparing(ACMContestStatisticVO::getTotalTime) // 再以总耗时升序
                ).collect(Collectors.toList());

        int rankNum = 1;
        int len = result.size();
        ACMContestStatisticVO lastACMRankVo = null;

        // 设置每个人的排名
        for (int i = 0; i < len; i++) {
            ACMContestStatisticVO ACMContestStatisticVo = result.get(i);

            if (rankNum == 1) {
                ACMContestStatisticVo.setRank(rankNum);
            } else {
                // 当前用户的总罚时和AC数跟前一个用户一样的话，同时前一个不应该为打星，排名则一样
                if (Objects.equals(lastACMRankVo.getAc(), ACMContestStatisticVo.getAc())
                        && lastACMRankVo.getTotalTime().equals(ACMContestStatisticVo.getTotalTime())) {
                    ACMContestStatisticVo.setRank(lastACMRankVo.getRank());
                } else {
                    ACMContestStatisticVo.setRank(rankNum);
                }
            }
            lastACMRankVo = ACMContestStatisticVo;
            rankNum++;
        }

        for (int i = 0; i < contest_cids.size(); i++) {
            Long cid = contest_cids.get(i);
            Contest contest = contestEntityService.getById(cid);

            // keyword 查询
            if (StrUtil.isNotBlank(keyword)) {
                String finalKeyword = keyword.trim().toLowerCase();
                result = result.stream()
                        .filter(rankVo -> {
                            boolean shouldFilter = filterBySchoolORRankShowName(finalKeyword,
                                    rankVo.getSchool(),
                                    getUserRankShowName(contest.getRankShowName(),
                                            rankVo.getUsername(),
                                            rankVo.getRealname(),
                                            rankVo.getNickname()));
                            return shouldFilter; // 返回 true 则筛选，返回 false 则不筛选
                        })
                        .collect(Collectors.toList());
            }
        }

        // 计算好排行榜，然后进行分页
        return getPagingRankList(result, currentPage, limit);
    }

    /**
     * @param isOpenSealRank              是否封榜
     * @param removeStar                  是否移除打星队伍
     * @param currentUserId               当前用户id
     * @param concernedList               关联比赛的id列表
     * @param contest                     比赛信息
     * @param currentPage                 当前页面
     * @param limit                       分页大小
     * @param keyword                     搜索关键词：匹配学校或榜单显示名称
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @desc 获取OI比赛排行榜
     */
    public IPage<OIContestRankVO> getContestOIRankPage(Boolean isOpenSealRank,
            Boolean removeStar,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            Contest contest,
            int currentPage,
            int limit,
            String keyword,
            Boolean isContainsAfterContestJudge,
            Long nowtime) {

        List<OIContestRankVO> orderResultList = contestCalculateRankManager.calcOIRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                isContainsAfterContestJudge,
                nowtime);

        if (StrUtil.isNotBlank(keyword)) {
            String finalKeyword = keyword.trim().toLowerCase();
            orderResultList = orderResultList.stream()
                    .filter(rankVo -> filterBySchoolORRankShowName(finalKeyword,
                            rankVo.getSchool(),
                            getUserRankShowName(contest.getRankShowName(),
                                    rankVo.getUsername(),
                                    rankVo.getRealname(),
                                    rankVo.getNickname())))
                    .collect(Collectors.toList());
        }

        // 计算好排行榜，然后进行分页
        return getPagingRankList(orderResultList, currentPage, limit);
    }

    /**
     * 获取ACM比赛排行榜外榜
     *
     * @param isOpenSealRank              是否开启封榜
     * @param removeStar                  是否移除打星队伍
     * @param contest                     比赛信息
     * @param currentUserId               当前用户id
     * @param concernedList               关注用户uid列表
     * @param externalCidList             关联比赛id列表
     * @param currentPage                 当前页码
     * @param limit                       分页大小
     * @param keyword                     搜索关键词
     * @param useCache                    是否启用缓存
     * @param cacheTime                   缓存时间（秒）
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @return
     */
    public IPage<ACMContestRankVO> getACMContestScoreboard(Boolean isOpenSealRank,
            Boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            int currentPage,
            int limit,
            String keyword,
            Boolean useCache,
            Long cacheTime,
            Boolean isContainsAfterContestJudge) {
        if (CollectionUtil.isNotEmpty(externalCidList)) {
            useCache = false;
        }

        List<ACMContestRankVO> acmContestRankVOS = getContestACMRankList(
                isOpenSealRank,
                removeStar,
                currentUserId,
                concernedList,
                externalCidList,
                contest,
                isContainsAfterContestJudge,
                null);

        if (StrUtil.isNotBlank(keyword)) {
            String finalKeyword = keyword.trim().toLowerCase();
            acmContestRankVOS = acmContestRankVOS.stream()
                    .filter(rankVo -> filterBySchoolORRankShowName(finalKeyword,
                            rankVo.getSchool(),
                            getUserRankShowName(contest.getRankShowName(),
                                    rankVo.getUsername(),
                                    rankVo.getRealname(),
                                    rankVo.getNickname())))
                    .collect(Collectors.toList());
        }

        return getPagingRankList(acmContestRankVOS, currentPage, limit);
    }

    /**
     * 获取OI比赛排行榜外榜
     *
     * @param isOpenSealRank              是否开启封榜
     * @param removeStar                  是否移除打星队伍
     * @param contest                     比赛信息
     * @param currentUserId               当前用户id
     * @param concernedList               关注用户uid列表
     * @param externalCidList             关联比赛id列表
     * @param currentPage                 当前页码
     * @param limit                       分页大小
     * @param keyword                     搜索关键词
     * @param useCache                    是否启用缓存
     * @param cacheTime                   缓存时间（秒）
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @return
     */
    public IPage<OIContestRankVO> getOIContestScoreboard(Boolean isOpenSealRank,
            Boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            int currentPage,
            int limit,
            String keyword,
            Boolean useCache,
            Long cacheTime,
            Boolean isContainsAfterContestJudge) {

        if (CollectionUtil.isNotEmpty(externalCidList)) {
            useCache = false;
        }
        List<OIContestRankVO> oiContestRankVOList = contestCalculateRankManager.calcOIRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                useCache,
                cacheTime,
                isContainsAfterContestJudge,
                null);

        if (StrUtil.isNotBlank(keyword)) {
            String finalKeyword = keyword.trim().toLowerCase();
            oiContestRankVOList = oiContestRankVOList.stream()
                    .filter(rankVo -> filterBySchoolORRankShowName(finalKeyword,
                            rankVo.getSchool(),
                            getUserRankShowName(contest.getRankShowName(),
                                    rankVo.getUsername(),
                                    rankVo.getRealname(),
                                    rankVo.getNickname())))
                    .collect(Collectors.toList());
        }
        return getPagingRankList(oiContestRankVOList, currentPage, limit);
    }

    private <T> Page<T> getPagingRankList(List<T> rankList, int currentPage, int limit) {
        Page<T> page = new Page<>(currentPage, limit);
        int count = rankList.size();
        List<T> pageList = new ArrayList<>();
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(rankList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    private String getUserRankShowName(String contestRankShowName, String username, String realName, String nickname) {
        switch (contestRankShowName) {
            case "username":
                return username;
            case "realname":
                return realName;
            case "nickname":
                return nickname;
        }
        return null;
    }

    private boolean filterBySchoolORRankShowName(String keyword, String school, String rankShowName) {
        if (StrUtil.isNotEmpty(school) && school.toLowerCase().contains(keyword)) {
            return true;
        }
        return StrUtil.isNotEmpty(rankShowName) && rankShowName.toLowerCase().contains(keyword);
    }

}