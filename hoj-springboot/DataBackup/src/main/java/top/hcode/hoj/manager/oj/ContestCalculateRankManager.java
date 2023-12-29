package top.hcode.hoj.manager.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.pojo.vo.ACMStatisticContestVO;
import top.hcode.hoj.pojo.vo.ContestAwardConfigVO;
import top.hcode.hoj.pojo.vo.ContestRecordVO;
import top.hcode.hoj.pojo.vo.OIContestRankVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @Date: 2022/3/11 20:11
 * @Description:
 */
@Component
public class ContestCalculateRankManager {

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ContestRecordEntityService contestRecordEntityService;

    @Resource
    private SynchronousManager synchronousManager;

    @Autowired
    private GroupMemberEntityService groupMemberEntityService;

    public List<ACMContestRankVO> calcACMRank(boolean isOpenSealRank,
            boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            boolean isContainsAfterContestJudge,
            Long nowtime) {
        return calcACMRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                false,
                null,
                isContainsAfterContestJudge,
                nowtime);
    }

    public List<ACMContestRankVO> calcSynchronousACMRank(boolean isOpenSealRank,
            boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            boolean isContainsAfterContestJudge,
            Long nowtime) {
        return calcSynchronousACMRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                false,
                null,
                isContainsAfterContestJudge,
                nowtime);
    }

    public List<OIContestRankVO> calcOIRank(Boolean isOpenSealRank,
            Boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            Boolean isContainsAfterContestJudge,
            Long nowtime) {

        return calcOIRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                false,
                null,
                isContainsAfterContestJudge,
                nowtime);
    }

    /**
     * @param isOpenSealRank              是否是查询封榜后的数据
     * @param removeStar                  是否需要移除打星队伍
     * @param contest                     比赛实体信息
     * @param currentUserId               当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList               关注的用户（uuid）列表
     * @param externalCidList             榜单额外显示的比赛列表
     * @param useCache                    是否对初始排序计算的结果进行缓存
     * @param cacheTime                   缓存的时间 单位秒
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @param nowtime                     比赛的现在时间
     * @MethodName calcACMRank
     * @Description TODO
     * @Return
     * @Since 2021/12/10
     */
    public List<ACMContestRankVO> calcACMRank(boolean isOpenSealRank,
            boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            boolean useCache,
            Long cacheTime,
            boolean isContainsAfterContestJudge,
            Long nowtime) {
        List<ACMContestRankVO> orderResultList;
        Long minSealRankTime = null;
        Long maxSealRankTime = null;
        if (useCache) {
            String key = null;
            if (isContainsAfterContestJudge) {
                key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_" + contest.getId();
            } else {
                key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_contains_after_" + contest.getId();
            }
            orderResultList = (List<ACMContestRankVO>) redisUtils.get(key);
            if (orderResultList == null) {
                if (isOpenSealRank) {
                    minSealRankTime = DateUtil.between(contest.getStartTime(), contest.getSealRankTime(),
                            DateUnit.SECOND);
                    maxSealRankTime = contest.getDuration();
                }
                orderResultList = getACMOrderRank(contest, isOpenSealRank, minSealRankTime, maxSealRankTime,
                        externalCidList, isContainsAfterContestJudge, nowtime);
                redisUtils.set(key, orderResultList, cacheTime);
            }
        } else {
            if (isOpenSealRank) {
                minSealRankTime = DateUtil.between(contest.getStartTime(), contest.getSealRankTime(), DateUnit.SECOND);
                maxSealRankTime = contest.getDuration();
            }
            orderResultList = getACMOrderRank(contest, isOpenSealRank, minSealRankTime, maxSealRankTime,
                    externalCidList, isContainsAfterContestJudge, nowtime);
        }

        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        Queue<ContestAwardConfigVO> awardConfigVoList = null;
        boolean isNeedSetAward = contest.getAwardType() != null && contest.getAwardType() > 0;
        if (removeStar) {
            // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
            if (isNeedSetAward) {
                awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                        orderResultList.size());
            }
        } else {
            if (isNeedSetAward) {
                if (contest.getAwardType() == 1) {
                    long count = orderResultList.stream().filter(e -> !starAccountMap.containsKey(e.getUsername()))
                            .count();
                    awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                            (int) count);
                } else {
                    awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                            orderResultList.size());
                }
            }
        }

        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        // 重新排序
        List<ACMContestRankVO> result = orderResultList.stream()
                .sorted(Comparator.comparing(ACMContestRankVO::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                        .thenComparing(ACMContestRankVO::getTotalTime) // 再以总耗时升序
                ).collect(Collectors.toList());

        // 将本oj的synchronous状态设为false
        orderResultList.forEach(ACMContestRankvo -> ACMContestRankvo.setSynchronous(false));

        return getTopRank(removeStar, isNeedSetAward, currentUserId, concernedList, result, starAccountMap,
                awardConfigVoList,
                needAddConcernedUser);
    }

    /**
     * @param isOpenSealRank              是否是查询封榜后的数据
     * @param removeStar                  是否需要移除打星队伍
     * @param contest                     比赛实体信息
     * @param currentUserId               当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList               关注的用户（uuid）列表
     * @param externalCidList             榜单额外显示的比赛列表
     * @param useCache                    是否对初始排序计算的结果进行缓存
     * @param cacheTime                   缓存的时间 单位秒
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @param nowtime                     比赛的现在时间
     * @MethodName calcSynchronousACMRank
     * @Description TODO
     * @Return
     * @Since 2021/12/10
     */
    public List<ACMContestRankVO> calcSynchronousACMRank(boolean isOpenSealRank,
            boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            boolean useCache,
            Long cacheTime,
            boolean isContainsAfterContestJudge,
            Long nowtime) {
        List<ACMContestRankVO> orderResultList;
        Long minSealRankTime = null;
        Long maxSealRankTime = null;
        if (useCache) {
            String key = null;
            if (isContainsAfterContestJudge) {
                key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_" + contest.getId();
            } else {
                key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_contains_after_" + contest.getId();
            }
            orderResultList = (List<ACMContestRankVO>) redisUtils.get(key);
            if (orderResultList == null) {
                if (isOpenSealRank) {
                    minSealRankTime = DateUtil.between(contest.getStartTime(), contest.getSealRankTime(),
                            DateUnit.SECOND);
                    maxSealRankTime = contest.getDuration();
                }
                orderResultList = getACMOrderRank(contest, isOpenSealRank, minSealRankTime, maxSealRankTime,
                        externalCidList, isContainsAfterContestJudge, nowtime);
                redisUtils.set(key, orderResultList, cacheTime);
            }
        } else {
            if (isOpenSealRank) {
                minSealRankTime = DateUtil.between(contest.getStartTime(), contest.getSealRankTime(), DateUnit.SECOND);
                maxSealRankTime = contest.getDuration();
            }
            orderResultList = getACMOrderRank(contest, isOpenSealRank, minSealRankTime, maxSealRankTime,
                    externalCidList, isContainsAfterContestJudge, nowtime);
        }

        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        Queue<ContestAwardConfigVO> awardConfigVoList = null;
        boolean isNeedSetAward = contest.getAwardType() != null && contest.getAwardType() > 0;
        if (removeStar) {
            // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
            if (isNeedSetAward) {
                awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                        orderResultList.size());
            }
        } else {
            if (isNeedSetAward) {
                if (contest.getAwardType() == 1) {
                    long count = orderResultList.stream().filter(e -> !starAccountMap.containsKey(e.getUsername()))
                            .count();
                    awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                            (int) count);
                } else {
                    awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                            orderResultList.size());
                }
            }
        }

        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        // 将本oj的synchronous状态设为false
        orderResultList.forEach(ACMContestRankvo -> ACMContestRankvo.setSynchronous(false));

        // 是否开启同步赛
        if (contest.getAuth() == 4 || contest.getAuth() == 5) {
            List<ACMContestRankVO> synchronousResultList = synchronousManager.getSynchronousRankList(contest,
                    isContainsAfterContestJudge, removeStar, nowtime);
            if (!CollectionUtils.isEmpty(synchronousResultList)) {
                // 将两个列表合并
                orderResultList.addAll(synchronousResultList);

                // TODO 同步赛首A修复
                // 将所有的ACMContestRankVO中的Submisson拆分
                List<HashMap<String, Object>> submissions = orderResultList.stream()
                        .map(ACMContestRankVO::getSubmissionInfo)
                        .flatMap(submissionInfo -> getSubmissions(submissionInfo).stream())
                        .collect(Collectors.toList());

                // 按照时间从小到大进行排序
                Collections.sort(submissions, new SubmissonComparator());

                HashMap<String, Long> firstACMap = new HashMap<>();

                for (HashMap<String, Object> submission : submissions) {
                    Boolean isAC = false;
                    String displayId = "";
                    Iterator<Map.Entry<String, Object>> internalIterator = submission.entrySet().iterator();
                    while (internalIterator.hasNext()) {
                        Map.Entry<String, Object> internalEntry = internalIterator.next();
                        String key = internalEntry.getKey();
                        Object value = internalEntry.getValue();

                        if ("isAC".equals(key) && value != null) {
                            isAC = true;
                        }

                        if ("displayId".equals(key)) {
                            displayId = value.toString();
                        }

                        // 已经通过题目
                        if ("ACTime".equals(key) && isAC && displayId != "") { // 检查键是否为"ACTime"
                            // 记录当前记录的提交时间
                            Long ACTime = ((Number) value).longValue();

                            Long time = firstACMap.getOrDefault(displayId, null);
                            if (time == null) {
                                firstACMap.put(displayId, ACTime);
                            } else {
                                // 相同提交时间也是first AC
                                if (time.longValue() == ACTime.longValue()) {
                                }
                            }
                            break;
                        }
                    }
                }
                for (ACMContestRankVO contestRankVO : orderResultList) {
                    HashMap<String, HashMap<String, Object>> submission = contestRankVO.getSubmissionInfo();
                    // 遍历 submissionInfos
                    Iterator<Map.Entry<String, HashMap<String, Object>>> iterator = submission.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<String, HashMap<String, Object>> entry = iterator.next();
                        String problemKey = entry.getKey();
                        HashMap<String, Object> submissionData = entry.getValue();

                        Boolean isAC = false;
                        int errorNumber = -1;
                        Long ACTime = -1L;
                        // 遍历内部HashMap
                        Iterator<Map.Entry<String, Object>> internalIterator = submissionData.entrySet().iterator();
                        while (internalIterator.hasNext()) {
                            Map.Entry<String, Object> internalEntry = internalIterator.next();
                            String key = internalEntry.getKey();
                            Object value = internalEntry.getValue();

                            if ("isAC".equals(key) && value != null) {
                                isAC = true;
                            }

                            if ("errorNum".equals(key) && value != null) {
                                errorNumber = (int) value;
                            }

                            // 已经通过题目
                            if ("ACTime".equals(key) && isAC) { // 检查键是否为"ACTime"
                                // 判断是不是first AC
                                boolean isFirstAC = false;

                                // 记录当前记录的提交时间
                                ACTime = ((Number) value).longValue();

                                Long firstACValue = firstACMap.get(problemKey);

                                if (firstACMap != null) {
                                    // 相同提交时间也是first AC
                                    if (firstACValue.longValue() == ACTime.longValue()) {
                                        isFirstAC = true;
                                    }
                                    submissionData.put("isFirstAC", isFirstAC);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 重新排序
        List<ACMContestRankVO> result = orderResultList.stream()
                .sorted(Comparator.comparing(ACMContestRankVO::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                        .thenComparing(ACMContestRankVO::getTotalTime) // 再以总耗时升序
                ).collect(Collectors.toList());

        return getTopRank(removeStar, isNeedSetAward, currentUserId, concernedList, result, starAccountMap,
                awardConfigVoList,
                needAddConcernedUser);
    }

    // 自定义的比较器
    static class SubmissonComparator implements Comparator<HashMap<String, Object>> {
        @Override
        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
            Long actime1 = findACTime(o1);
            Long actime2 = findACTime(o2);

            // 根据 ACTime 的大小进行比较
            return Long.compare(actime1, actime2);
        }

        // 辅助方法，用于找到 ACTime 的值
        private Long findACTime(HashMap<String, Object> submissionInfo) {
            // 遍历 submissionInfo
            Boolean isAC = false;

            Iterator<Map.Entry<String, Object>> iterator = submissionInfo.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                Object value = entry.getValue();

                if ("isAC".equals(key) && value != null) {
                    isAC = true;
                }

                // TODO 同步赛时间筛选
                if ("ACTime".equals(key) && isAC) { // 检查键是否为"ACTime"
                    Long ACTime = ((Number) value).longValue();
                    return ACTime;
                }

            }

            // 如果找不到 ACTime，可以返回一个默认值或者抛出异常，具体根据需求而定
            return 0L;
        }
    }

    private List<ACMContestRankVO> getACMOrderRank(Contest contest,
            Boolean isOpenSealRank,
            Long minSealRankTime,
            Long maxSealRankTime,
            List<Integer> externalCidList,
            Boolean isContainsAfterContestJudge,
            Long nowtime) {

        List<ContestRecordVO> contestRecordList = contestRecordEntityService.getACMContestRecord(contest.getUid(),
                contest.getId(),
                externalCidList,
                contest.getStartTime());

        List<String> superAdminUidList = getSuperAdminUidList(contest.getGid(), contest.getId());

        List<ACMContestRankVO> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        int index = 0;

        HashMap<String, Long> firstACMap = new HashMap<>();

        for (ContestRecordVO contestRecord : contestRecordList) {

            if (nowtime != null) {
                if (contestRecord.getTime() > nowtime) {
                    // 将超过查询时间的数据排除
                    continue;
                }
            }

            if (superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员的提交不入排行榜
                continue;
            }

            boolean isAfterContestJudge = contestRecord.getSubmitTime().getTime() >= contest.getEndTime().getTime();
            boolean isBeforeContestJudge = contestRecord.getSubmitTime().getTime() < contest.getStartTime().getTime();

            if (isBeforeContestJudge) {
                // 比赛开始前的提交记录不入排行榜，跳过
                continue;
            }

            if ((!isContainsAfterContestJudge || isOpenSealRank) && isAfterContestJudge) {
                // 如果不包含比赛结束后的提交 或者 处于封榜状态，则跳过比赛后的提交
                continue;
            }

            ACMContestRankVO ACMContestRankVo;
            if (!uidMapIndex.containsKey(contestRecord.getUid())) { // 如果该用户信息没还记录

                // 初始化参数
                ACMContestRankVo = new ACMContestRankVO();
                ACMContestRankVo.setRealname(contestRecord.getRealname())
                        .setAvatar(contestRecord.getAvatar())
                        .setSchool(contestRecord.getSchool())
                        .setGender(contestRecord.getGender())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setNickname(contestRecord.getNickname())
                        .setAc(0)
                        .setTotalTime(0L)
                        .setTotal(0);

                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                ACMContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(ACMContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                ACMContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid())); // 根据记录的index进行获取
            }

            HashMap<String, Object> problemSubmissionInfo = ACMContestRankVo.getSubmissionInfo()
                    .get(contestRecord.getDisplayId());

            if (problemSubmissionInfo == null) {
                problemSubmissionInfo = new HashMap<>();
                problemSubmissionInfo.put("errorNum", 0);
            }

            ACMContestRankVo.setTotal(ACMContestRankVo.getTotal() + 1);

            // 如果是当前是开启封榜的时段和同时该提交是处于封榜时段 尝试次数+1
            if (isOpenSealRank && isInSealTimeSubmission(minSealRankTime, maxSealRankTime, contestRecord.getTime())) {

                int tryNum = (int) problemSubmissionInfo.getOrDefault("tryNum", 0);
                problemSubmissionInfo.put("tryNum", tryNum + 1);

            } else {

                // 如果该题目已经AC过了，其它都不记录了
                if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                    continue;
                }

                // 记录已经按题目提交耗时time升序了

                // 通过的话
                if (contestRecord.getStatus().intValue() == Constants.Contest.RECORD_AC.getCode()) {
                    // 总解决题目次数ac+1
                    ACMContestRankVo.setAc(ACMContestRankVo.getAc() + 1);

                    // 判断是不是first AC
                    boolean isFirstAC = false;
                    Long time = firstACMap.getOrDefault(contestRecord.getDisplayId(), null);
                    if (time == null) {
                        isFirstAC = true;
                        firstACMap.put(contestRecord.getDisplayId(), contestRecord.getTime());
                    } else {
                        // 相同提交时间也是first AC
                        if (time.longValue() == contestRecord.getTime().longValue()) {
                            isFirstAC = true;
                        }
                    }

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("isAC", true);
                    problemSubmissionInfo.put("isFirstAC", isFirstAC);
                    problemSubmissionInfo.put("ACTime", contestRecord.getTime());
                    problemSubmissionInfo.put("errorNum", errorNumber);
                    if (isAfterContestJudge) {
                        problemSubmissionInfo.put("isAfterContest", true);
                    }

                    // 同时计算总耗时，总耗时加上 该题目未AC前的错误次数*20*60+题目AC耗时
                    ACMContestRankVo.setTotalTime(
                            ACMContestRankVo.getTotalTime() + errorNumber * 20 * 60 + contestRecord.getTime());

                    // 未通过同时需要记录罚时次数
                } else if (contestRecord.getStatus().intValue() == Constants.Contest.RECORD_NOT_AC_PENALTY.getCode()) {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber + 1);
                } else {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber);
                }
            }
            ACMContestRankVo.getSubmissionInfo().put(contestRecord.getDisplayId(), problemSubmissionInfo);
        }

        List<ACMContestRankVO> orderResultList = result.stream()
                .sorted(Comparator.comparing(ACMContestRankVO::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                        .thenComparing(ACMContestRankVO::getTotalTime) // 再以总耗时升序
                ).collect(Collectors.toList());

        return orderResultList;
    }

    /**
     * @param isOpenSealRank              是否是查询封榜后的数据
     * @param removeStar                  是否需要移除打星队伍
     * @param contest                     比赛实体信息
     * @param currentUserId               当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList               关注的用户（uuid）列表
     * @param externalCidList             榜单额外显示比赛列表
     * @param useCache                    是否对初始排序计算的结果进行缓存
     * @param cacheTime                   缓存的时间 单位秒
     * @param isContainsAfterContestJudge 是否包含比赛结束后的提交
     * @MethodName calcOIRank
     * @Description TODO
     * @Return
     * @Since 2021/12/10
     */
    public List<OIContestRankVO> calcOIRank(boolean isOpenSealRank,
            boolean removeStar,
            Contest contest,
            String currentUserId,
            List<String> concernedList,
            List<Integer> externalCidList,
            boolean useCache,
            Long cacheTime,
            boolean isContainsAfterContestJudge,
            Long nowtime) {

        List<OIContestRankVO> orderResultList;
        if (useCache) {
            String key = null;
            if (isContainsAfterContestJudge) {
                key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_contains_after_" + contest.getId();
            } else {
                key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_" + contest.getId();
            }
            orderResultList = (List<OIContestRankVO>) redisUtils.get(key);
            if (orderResultList == null) {
                orderResultList = getOIOrderRank(contest, externalCidList, isOpenSealRank, isContainsAfterContestJudge,
                        nowtime);
                redisUtils.set(key, orderResultList, cacheTime);
            }
        } else {
            orderResultList = getOIOrderRank(contest, externalCidList, isOpenSealRank, isContainsAfterContestJudge,
                    nowtime);
        }

        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        Queue<ContestAwardConfigVO> awardConfigVoList = null;
        boolean isNeedSetAward = contest.getAwardType() != null && contest.getAwardType() > 0;
        if (removeStar) {
            // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
            if (isNeedSetAward) {
                awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                        orderResultList.size());
            }
        } else {
            if (isNeedSetAward) {
                if (contest.getAwardType() == 1) {
                    long count = orderResultList.stream().filter(e -> !starAccountMap.containsKey(e.getUsername()))
                            .count();
                    awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                            (int) count);
                } else {
                    awardConfigVoList = getContestAwardConfigList(contest.getAwardConfig(), contest.getAwardType(),
                            orderResultList.size());
                }
            }
        }

        // 记录当前用户排名数据和关注列表的用户排名数据
        List<OIContestRankVO> topOIRankVoList = new ArrayList<>();
        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        int rankNum = 1;
        OIContestRankVO lastOIRankVo = null;
        ContestAwardConfigVO configVo = null;
        int len = orderResultList.size();
        for (int i = 0; i < len; i++) {
            OIContestRankVO currentOIRankVo = orderResultList.get(i);
            if (!removeStar && starAccountMap.containsKey(currentOIRankVo.getUsername())) {
                // 打星队伍排名为-1
                currentOIRankVo.setRank(-1);
                currentOIRankVo.setIsWinAward(false);
            } else {
                if (rankNum == 1) {
                    currentOIRankVo.setRank(rankNum);
                } else {
                    // 当前用户的程序总运行时间和总得分跟前一个用户一样的话，同时前一个不应该为打星用户，排名则一样
                    if (lastOIRankVo.getTotalScore().equals(currentOIRankVo.getTotalScore())
                            && lastOIRankVo.getTotalTime().equals(currentOIRankVo.getTotalTime())) {
                        currentOIRankVo.setRank(lastOIRankVo.getRank());
                    } else {
                        currentOIRankVo.setRank(rankNum);
                    }
                }

                if (isNeedSetAward && currentOIRankVo.getTotalScore() > 0) {
                    if (configVo == null || configVo.getNum() == 0) {
                        if (!awardConfigVoList.isEmpty()) {
                            configVo = awardConfigVoList.poll();
                            currentOIRankVo.setAwardName(configVo.getName());
                            currentOIRankVo.setAwardBackground(configVo.getBackground());
                            currentOIRankVo.setAwardColor(configVo.getColor());
                            currentOIRankVo.setIsWinAward(true);
                            configVo.setNum(configVo.getNum() - 1);
                        } else {
                            isNeedSetAward = false;
                            currentOIRankVo.setIsWinAward(false);
                        }
                    } else {
                        currentOIRankVo.setAwardName(configVo.getName());
                        currentOIRankVo.setAwardBackground(configVo.getBackground());
                        currentOIRankVo.setAwardColor(configVo.getColor());
                        currentOIRankVo.setIsWinAward(true);
                        configVo.setNum(configVo.getNum() - 1);
                    }
                } else {
                    currentOIRankVo.setIsWinAward(false);
                }

                lastOIRankVo = currentOIRankVo;
                rankNum++;
            }

            // 默认当前请求用户的排名显示在最顶行
            if (!StringUtils.isEmpty(currentUserId) &&
                    currentOIRankVo.getUid().equals(currentUserId)) {
                topOIRankVoList.add(0, currentOIRankVo);
            }

            // 需要添加关注用户
            if (needAddConcernedUser) {
                if (concernedList.contains(currentOIRankVo.getUid())) {
                    topOIRankVoList.add(currentOIRankVo);
                }
            }
        }
        topOIRankVoList.addAll(orderResultList);
        return topOIRankVoList;
    }

    private List<OIContestRankVO> getOIOrderRank(Contest contest,
            List<Integer> externalCidList,
            Boolean isOpenSealRank,
            Boolean isContainsAfterContestJudge,
            Long nowtime) {

        List<ContestRecordVO> oiContestRecord = contestRecordEntityService.getOIContestRecord(contest,
                externalCidList, isOpenSealRank, isContainsAfterContestJudge);

        List<String> superAdminUidList = getSuperAdminUidList(contest.getGid(), contest.getId());

        List<OIContestRankVO> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> uidMapTime = new HashMap<>();

        boolean isHighestRankScore = Constants.Contest.OI_RANK_HIGHEST_SCORE.getName()
                .equals(contest.getOiRankScoreType());

        int index = 0;

        for (ContestRecordVO contestRecord : oiContestRecord) {

            if (nowtime != null) {
                if (contestRecord.getTime() > nowtime) {
                    // 将超过查询时间的数据排除
                    continue;
                }
            }

            if (superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员的提交不入排行榜
                continue;
            }

            boolean isAfterContestJudge = contestRecord.getSubmitTime().getTime() >= contest.getEndTime().getTime();
            boolean isBeforeContestJudge = contestRecord.getSubmitTime().getTime() < contest.getStartTime().getTime();

            if (isBeforeContestJudge) {
                // 比赛开始前的提交记录不入排行榜，跳过
                continue;
            }

            if ((!isContainsAfterContestJudge || isOpenSealRank) && isAfterContestJudge) {
                // 如果不包含比赛结束后的提交 或者 处于封榜状态，则跳过比赛后的提交
                continue;
            }

            if (contestRecord.getStatus().equals(Constants.Contest.RECORD_AC.getCode())) { // AC
                HashMap<String, Integer> pidMapTime = uidMapTime.get(contestRecord.getUid());
                if (pidMapTime != null) {
                    Integer useTime = pidMapTime.get(contestRecord.getDisplayId());
                    if (useTime != null) {
                        if (useTime > contestRecord.getUseTime()) { // 如果时间消耗比原来的少
                            pidMapTime.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                        }
                    } else {
                        pidMapTime.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                    }
                } else {
                    HashMap<String, Integer> tmp = new HashMap<>();
                    tmp.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                    uidMapTime.put(contestRecord.getUid(), tmp);
                }
            }

            OIContestRankVO oiContestRankVo;
            if (!uidMapIndex.containsKey(contestRecord.getUid())) { // 如果该用户信息没还记录
                // 初始化参数
                oiContestRankVo = new OIContestRankVO();
                oiContestRankVo.setRealname(contestRecord.getRealname())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setSchool(contestRecord.getSchool())
                        .setAvatar(contestRecord.getAvatar())
                        .setGender(contestRecord.getGender())
                        .setNickname(contestRecord.getNickname())
                        .setTotalScore(0);

                HashMap<String, Integer> submissionInfo = new HashMap<>();
                oiContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(oiContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                oiContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid())); // 根据记录的index进行获取
            }

            // 记录总分
            HashMap<String, Integer> submissionInfo = oiContestRankVo.getSubmissionInfo();
            Integer score = submissionInfo.get(contestRecord.getDisplayId());
            if (isHighestRankScore) {
                if (score == null) {
                    oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() + contestRecord.getScore());
                    submissionInfo.put(contestRecord.getDisplayId(), contestRecord.getScore());
                }
            } else {
                if (contestRecord.getScore() != null) {
                    if (score != null) { // 为了避免同个提交时间的重复计算
                        oiContestRankVo
                                .setTotalScore(oiContestRankVo.getTotalScore() - score + contestRecord.getScore());
                    } else {
                        oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() + contestRecord.getScore());
                    }
                }
                submissionInfo.put(contestRecord.getDisplayId(), contestRecord.getScore());
            }

        }

        for (OIContestRankVO oiContestRankVo : result) {
            HashMap<String, Integer> pidMapTime = uidMapTime.get(oiContestRankVo.getUid());
            int sumTime = 0;
            if (pidMapTime != null) {
                for (String key : pidMapTime.keySet()) {
                    Integer time = pidMapTime.get(key);
                    sumTime += time == null ? 0 : time;
                }
            }
            oiContestRankVo.setTotalTime(sumTime);
            oiContestRankVo.setTimeInfo(pidMapTime);
        }

        // 根据总得分进行降序,再根据总时耗升序排序
        List<OIContestRankVO> orderResultList = result.stream()
                .sorted(Comparator.comparing(OIContestRankVO::getTotalScore, Comparator.reverseOrder())
                        .thenComparing(OIContestRankVO::getTotalTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());
        return orderResultList;
    }

    public List<ACMStatisticContestVO> calcStatisticRank(List<Contest> contestList) {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        List<ACMStatisticContestVO> resultList = new ArrayList<>();

        int index = 0;

        for (int i = 0; i < contestList.size(); i++) {
            Contest contest = contestList.get(i);

            // 进行排序计算得到用户的排名
            List<ACMContestRankVO> orderResultList = calcACMRank(
                    false,
                    true,
                    contest,
                    userRolesVo.getUid(),
                    null,
                    null,
                    false,
                    null);

            // 将ACMContestRankVO 转化为 ACMStatisticContestVO
            if (orderResultList.size() > 0) {

                for (int j = 0; j < orderResultList.size(); j++) {
                    ACMStatisticContestVO ACMStatisticContestVo;

                    if (!uidMapIndex.containsKey(orderResultList.get(j).getUid())) { // 如果该用户信息没还记录
                        // 初始化参数
                        ACMStatisticContestVo = new ACMStatisticContestVO();
                        ACMStatisticContestVo.setRealname(orderResultList.get(j).getRealname())
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

                        ACMStatisticContestVo.setContestInfo(contestInfo);

                        resultList.add(ACMStatisticContestVo);
                        uidMapIndex.put(orderResultList.get(j).getUid(), index);
                        index++;
                    } else {
                        ACMStatisticContestVo = resultList.get(uidMapIndex.get(orderResultList.get(j).getUid())); // 根据记录的index进行获取
                    }

                    // 将该场比赛的总AC计入
                    ACMStatisticContestVo.setAc(ACMStatisticContestVo.getAc() + orderResultList.get(j).getAc());
                    // 将该场比赛的总罚时计入
                    ACMStatisticContestVo
                            .setTotalTime(ACMStatisticContestVo.getTotalTime() + orderResultList.get(j).getTotalTime());
                    // 将该场比赛的总提交数计入
                    ACMStatisticContestVo
                            .setTotal(ACMStatisticContestVo.getTotal() + orderResultList.get(j).getTotal());

                    HashMap<String, Object> contestInfo = ACMStatisticContestVo.getContestInfo()
                            .get(contest.getId().toString());

                    if (contestInfo == null) {
                        contestInfo = new HashMap<>();
                        contestInfo.put("title", contest.getTitle());
                        contestInfo.put("AC", orderResultList.get(j).getAc());
                        contestInfo.put("TotalTime", orderResultList.get(j).getTotalTime());
                    }

                    // 计入比赛信息
                    ACMStatisticContestVo.getContestInfo().put(contest.getId().toString(), contestInfo);
                }
            }
        }

        // 重新排序
        List<ACMStatisticContestVO> result = resultList.stream()
                .sorted(Comparator.comparing(ACMStatisticContestVO::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                        .thenComparing(ACMStatisticContestVO::getTotalTime) // 再以总耗时升序
                ).collect(Collectors.toList());

        int rankNum = 1;
        int len = result.size();
        ACMStatisticContestVO lastACMRankVo = null;

        // 设置每个人的排名
        for (int i = 0; i < len; i++) {
            ACMStatisticContestVO ACMStatisticContestVo = result.get(i);

            if (rankNum == 1) {
                ACMStatisticContestVo.setRank(rankNum);
            } else {
                // 当前用户的总罚时和AC数跟前一个用户一样的话，同时前一个不应该为打星，排名则一样
                if (Objects.equals(lastACMRankVo.getAc(), ACMStatisticContestVo.getAc())
                        && lastACMRankVo.getTotalTime().equals(ACMStatisticContestVo.getTotalTime())) {
                    ACMStatisticContestVo.setRank(lastACMRankVo.getRank());
                } else {
                    ACMStatisticContestVo.setRank(rankNum);
                }
            }
            lastACMRankVo = ACMStatisticContestVo;
            rankNum++;
        }
        return result;
    }

    public List<String> getSuperAdminUidList(Long gid, Long cid) {

        List<String> superAdminUidList = userInfoEntityService.getNowContestAdmin(cid);

        if (gid != null) {
            QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
            groupMemberQueryWrapper.eq("gid", gid).eq("auth", 5);

            List<GroupMember> groupRootList = groupMemberEntityService.list(groupMemberQueryWrapper);

            for (GroupMember groupMember : groupRootList) {
                superAdminUidList.add(groupMember.getUid());
            }
        }
        return superAdminUidList;
    }

    private boolean isInSealTimeSubmission(Long minSealRankTime, Long maxSealRankTime, Long time) {
        return time >= minSealRankTime && time < maxSealRankTime;
    }

    private HashMap<String, Boolean> starAccountToMap(String starAccountStr) {
        if (StringUtils.isEmpty(starAccountStr)) {
            return new HashMap<>();
        }
        JSONObject jsonObject = JSONUtil.parseObj(starAccountStr);
        List<String> list = jsonObject.get("star_account", List.class);
        HashMap<String, Boolean> res = new HashMap<>();
        for (String str : list) {
            if (!StringUtils.isEmpty(str)) {
                res.put(str, true);
            }
        }
        return res;
    }

    private Queue<ContestAwardConfigVO> getContestAwardConfigList(String awardConfig, Integer awardType,
            Integer totalUser) {
        if (StringUtils.isEmpty(awardConfig)) {
            return new LinkedList<>();
        }
        JSONObject jsonObject = JSONUtil.parseObj(awardConfig);
        List<JSONObject> list = jsonObject.get("config", List.class);

        Queue<ContestAwardConfigVO> queue = new LinkedList<>();

        if (awardType == 1) {
            // 占比转换成具体人数
            for (JSONObject object : list) {
                ContestAwardConfigVO configVo = JSONUtil.toBean(object, ContestAwardConfigVO.class);
                if (configVo.getNum() != null && configVo.getNum() > 0) {
                    int num = (int) (configVo.getNum() * 0.01 * totalUser);
                    if (num > 0) {
                        configVo.setNum(num);
                        queue.offer(configVo);
                    }
                }
            }
        } else {
            for (JSONObject object : list) {
                ContestAwardConfigVO configVo = JSONUtil.toBean(object, ContestAwardConfigVO.class);
                if (configVo.getNum() != null && configVo.getNum() > 0) {
                    queue.offer(configVo);
                }
            }
        }
        return queue;
    }

    public List<ACMContestRankVO> getTopRank(
            boolean removeStar,
            boolean isNeedSetAward,
            String currentUserId,
            List<String> concernedList,
            List<ACMContestRankVO> result,
            HashMap<String, Boolean> starAccountMap,
            Queue<ContestAwardConfigVO> awardConfigVoList,
            boolean needAddConcernedUser) {

        List<ACMContestRankVO> topACMRankVoList = new ArrayList<>();
        int rankNum = 1;
        int len = result.size();
        ACMContestRankVO lastACMRankVo = null;
        ContestAwardConfigVO configVo = null;
        for (int i = 0; i < len; i++) {
            ACMContestRankVO currentACMRankVo = result.get(i);
            if (!removeStar && starAccountMap.containsKey(currentACMRankVo.getUsername())) {
                // 打星队伍排名为-1
                currentACMRankVo.setRank(-1);
                currentACMRankVo.setIsWinAward(false);
            } else {
                if (rankNum == 1) {
                    currentACMRankVo.setRank(rankNum);
                } else {
                    // 当前用户的总罚时和AC数跟前一个用户一样的话，同时前一个不应该为打星，排名则一样
                    if (Objects.equals(lastACMRankVo.getAc(), currentACMRankVo.getAc())
                            && lastACMRankVo.getTotalTime().equals(currentACMRankVo.getTotalTime())) {
                        currentACMRankVo.setRank(lastACMRankVo.getRank());
                    } else {
                        currentACMRankVo.setRank(rankNum);
                    }
                }

                if (isNeedSetAward && currentACMRankVo.getAc() > 0) {
                    if (configVo == null || configVo.getNum() == 0) {
                        if (!awardConfigVoList.isEmpty()) {
                            configVo = awardConfigVoList.poll();
                            currentACMRankVo.setAwardName(configVo.getName());
                            currentACMRankVo.setAwardBackground(configVo.getBackground());
                            currentACMRankVo.setAwardColor(configVo.getColor());
                            currentACMRankVo.setIsWinAward(true);
                            configVo.setNum(configVo.getNum() - 1);
                        } else {
                            isNeedSetAward = false;
                            currentACMRankVo.setIsWinAward(false);
                        }
                    } else {
                        currentACMRankVo.setAwardName(configVo.getName());
                        currentACMRankVo.setAwardBackground(configVo.getBackground());
                        currentACMRankVo.setAwardColor(configVo.getColor());
                        currentACMRankVo.setIsWinAward(true);
                        configVo.setNum(configVo.getNum() - 1);
                    }
                } else {
                    currentACMRankVo.setIsWinAward(false);
                }

                lastACMRankVo = currentACMRankVo;
                rankNum++;
            }
            // 默认将请求用户的排名置为最顶
            if (!StringUtils.isEmpty(currentUserId) &&
                    currentACMRankVo.getUid().equals(currentUserId)) {
                topACMRankVoList.add(0, currentACMRankVo);
            }

            // 需要添加关注用户
            if (needAddConcernedUser) {
                if (concernedList.contains(currentACMRankVo.getUid())) {
                    topACMRankVoList.add(currentACMRankVo);
                }
            }
        }
        topACMRankVoList.addAll(result);
        return topACMRankVoList;
    }

    public List<HashMap<String, Object>> getSubmissions(HashMap<String, HashMap<String, Object>> submissionInfo) {
        List<HashMap<String, Object>> submissions = new ArrayList<>();
        // 遍历 submissionInfos
        Iterator<Map.Entry<String, HashMap<String, Object>>> iterator = submissionInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HashMap<String, Object>> entry = iterator.next();
            String key = entry.getKey();
            HashMap<String, Object> submissionData = entry.getValue();

            submissionData.put("displayId", key);

            submissions.add(submissionData);
        }
        return submissions;
    }
}