package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.CheckACDTO;
import top.hcode.hoj.pojo.entity.contest.ContestPrint;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.contest.ContestSign;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.service.oj.ContestAdminService;

/**
 *
 * @Date: 2021/9/20 13:15
 * @Description: 处理比赛管理模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestAdminController {

    @Autowired
    private ContestAdminService contestAdminService;

    /**
     * @MethodName getContestACInfo
     * @Params * @param null
     * @Description 获取各个用户的ac情况，仅限于比赛管理者可查看
     * @Return
     * @Since 2021/1/17
     */
    @GetMapping("/get-contest-ac-info")
    @RequiresAuthentication
    public CommonResult<IPage<ContestRecord>> getContestACInfo(@RequestParam("cid") Long cid,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit) {

        return contestAdminService.getContestACInfo(cid, currentPage, limit);
    }

    /**
     * @MethodName checkContestACInfo
     * @Params * @param null
     * @Description 比赛管理员确定该次提交的ac情况
     * @Return
     * @Since 2021/1/17
     */
    @PutMapping("/check-contest-ac-info")
    @RequiresAuthentication
    public CommonResult<Void> checkContestACInfo(@RequestBody CheckACDTO checkACDto) {

        return contestAdminService.checkContestACInfo(checkACDto);
    }

    @GetMapping("/get-contest-print")
    @RequiresAuthentication
    public CommonResult<IPage<ContestPrint>> getContestPrint(@RequestParam("cid") Long cid,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit) {

        return contestAdminService.getContestPrint(cid, currentPage, limit);
    }

    /**
     * @param id
     * @param cid
     * @MethodName checkContestStatus
     * @Description 更新该打印为确定状态
     * @Return
     * @Since 2021/9/20
     */
    @PutMapping("/check-contest-print-status")
    @RequiresAuthentication
    public CommonResult<Void> checkContestPrintStatus(@RequestParam("id") Long id,
            @RequestParam("cid") Long cid) {

        return contestAdminService.checkContestPrintStatus(id, cid);
    }

    @GetMapping("/get-contest-sign")
    @RequiresAuthentication
    public CommonResult<IPage<ContestSign>> getContestSign(@RequestParam("cid") Long cid,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "type", required = false) Boolean type,
            @RequestParam(value = "gender", required = false) Boolean gender,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return contestAdminService.getContestSign(cid, currentPage, limit, type, gender, status, keyword);
    }

    @GetMapping("/get-contest-user-sign")
    @RequiresAuthentication
    public CommonResult<ContestSignVO> getContestSignInfo(
            @RequestParam("cid") Long cid,
            @RequestParam("id") Long id) {

        return contestAdminService.getContestSignInfo(cid, id);
    }

    /**
     * @param id
     * @param cid
     * @param status
     * @MethodName checkContestSignStatus
     * @Description 更新比赛报名的审核状态
     * @Return
     */
    @PostMapping("/check-contest-sign-status")
    @RequiresAuthentication
    public CommonResult<Void> checkContestSignStatus(@RequestBody Map<String, Object> params) {

        return contestAdminService.checkContestSignStatus(params);
    }

    @PostMapping("/contest-sign")
    @RequiresAuthentication
    public CommonResult<Void> updateContestSign(@RequestBody ContestSignVO contestSignVo) {

        return contestAdminService.updateContestSign(contestSignVo);
    }
}