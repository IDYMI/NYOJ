package top.hcode.hoj.controller.admin;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.service.admin.rejudge.RejudgeService;

import javax.annotation.Resource;

/**
 *
 * @Date: 2021/1/3 14:09
 * @Description: 超管或者题目管理重判提交
 */

@RestController
@RequestMapping("/api/admin/judge")
@RequiresRoles(value = { "root", "problem_admin", "admin" }, logical = Logical.OR)
public class AdminJudgeController {

    @Resource
    private RejudgeService rejudgeService;

    @GetMapping("/rejudge")
    @RequiresAuthentication
    @RequiresPermissions("rejudge")
    public CommonResult<Judge> rejudge(@RequestParam("submitId") Long submitId) {
        return rejudgeService.rejudge(submitId);
    }

    @GetMapping("/rejudge-contest-problem")
    @RequiresAuthentication
    @RequiresPermissions("rejudge")
    public CommonResult<Void> rejudgeContestProblem(@RequestParam("cid") Long cid, @RequestParam("pid") Long pid) {
        return rejudgeService.rejudgeContestProblem(cid, pid);
    }

    @GetMapping("/manual-judge")
    @RequiresAuthentication
    @RequiresPermissions("rejudge")
    public CommonResult<Judge> manualJudge(@RequestParam("submitId") Long submitId,
            @RequestParam("status") Integer status,
            @RequestParam(value = "score", required = false) Integer score) {
        return rejudgeService.manualJudge(submitId, status, score);
    }

    @GetMapping("/cancel-judge")
    @RequiresAuthentication
    @RequiresPermissions("rejudge")
    public CommonResult<Judge> cancelJudge(@RequestParam("submitId") Long submitId) {
        return rejudgeService.cancelJudge(submitId);
    }
}
