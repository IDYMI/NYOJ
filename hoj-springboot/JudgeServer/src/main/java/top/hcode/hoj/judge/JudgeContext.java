package top.hcode.hoj.judge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.dao.ContestRecordEntityService;
import top.hcode.hoj.dao.JudgeEntityService;
import top.hcode.hoj.dao.UserAcproblemEntityService;
import top.hcode.hoj.judge.entity.LanguageConfig;
import top.hcode.hoj.pojo.dto.TestJudgeReq;
import top.hcode.hoj.pojo.dto.TestJudgeRes;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.util.Constants;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 *
 * @Date: 2022/3/12 15:49
 * @Description:
 */
@Component
public class JudgeContext {

    @Autowired
    private JudgeStrategy judgeStrategy;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Resource
    private LanguageConfigLoader languageConfigLoader;

    public Judge Judge(Problem problem, Judge judge) {

        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        LanguageConfig languageConfig = languageConfigLoader.getLanguageConfigByName(judge.getLanguage());
        if (languageConfig.getSrcName() == null
                || (!languageConfig.getSrcName().endsWith(".c")
                        && !languageConfig.getSrcName().endsWith(".cpp"))) {
            problem.setTimeLimit(problem.getTimeLimit() * 2);
            problem.setMemoryLimit(problem.getMemoryLimit() * 2);
        }

        HashMap<String, Object> judgeResult = judgeStrategy.judge(problem, judge);

        Judge finalJudgeRes = new Judge();
        finalJudgeRes.setSubmitId(judge.getSubmitId());

        // 所有评测结果都有错误提醒
        finalJudgeRes.setErrorMessage((String) judgeResult.getOrDefault("errMsg", ""));

        // 设置最终结果状态码
        finalJudgeRes.setStatus((Integer) judgeResult.get("code"));
        // 设置最大时间和最大空间不超过题目限制时间和空间
        // kb
        Integer memory = (Integer) judgeResult.get("memory");
        finalJudgeRes.setMemory(Math.min(memory, problem.getMemoryLimit() * 1024));
        // ms
        Integer time = (Integer) judgeResult.get("time");
        finalJudgeRes.setTime(Math.min(time, problem.getTimeLimit()));
        // score
        finalJudgeRes.setScore((Integer) judgeResult.getOrDefault("score", null));
        // oi_rank_score
        finalJudgeRes.setOiRankScore((Integer) judgeResult.getOrDefault("oiRankScore", null));

        // 设置排序后的submit_id
        finalJudgeRes = setSortedId(finalJudgeRes);

        return finalJudgeRes;
    }

    public TestJudgeRes testJudge(TestJudgeReq testJudgeReq) {
        // c和c++为一倍时间和空间，其它语言为2倍时间和空间
        LanguageConfig languageConfig = languageConfigLoader.getLanguageConfigByName(testJudgeReq.getLanguage());
        if (languageConfig.getSrcName() == null
                || (!languageConfig.getSrcName().endsWith(".c")
                        && !languageConfig.getSrcName().endsWith(".cpp"))) {
            testJudgeReq.setTimeLimit(testJudgeReq.getTimeLimit() * 2);
            testJudgeReq.setMemoryLimit(testJudgeReq.getMemoryLimit() * 2);
        }
        return judgeStrategy.testJudge(testJudgeReq);
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemError {
        return Compiler.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage,
            HashMap<String, String> extraFiles) throws SystemError {
        return Compiler.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }

    public void updateOtherTable(Long submitId,
            Integer status,
            Long cid,
            String uid,
            Long pid,
            Long gid,
            Integer score,
            Integer useTime) {

        // 如果是AC,就更新user_acproblem表,
        if (status.intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus() && gid == null) {
            userAcproblemEntityService.saveOrUpdate(new UserAcproblem()
                    .setPid(pid)
                    .setUid(uid)
                    .setSubmitId(submitId));
        }

        if (cid != 0) { // 如果是比赛提交
            contestRecordEntityService.updateContestRecord(score, status, submitId, useTime);
        }
    }

    public Judge setSortedId(Judge judge) {
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("sorted_id")
                .eq("submit_id", judge.getSubmitId());
        Judge search_judge = judgeEntityService.getOne(judgeQueryWrapper, false);

        // 没有插入judge记录前
        if (search_judge == null) {
            judge.setSortedId(getMaxSortedId());
        }
        return judge;
    }

    public Long getMaxSortedId() {
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("sorted_id").orderByDesc("sorted_id").last("LIMIT 1");
        Judge judge = judgeEntityService.getOne(judgeQueryWrapper, false);

        return (judge != null && judge.getSortedId() != null) ? judge.getSortedId() + 1
                : judgeEntityService.count() + 1;
    }

}