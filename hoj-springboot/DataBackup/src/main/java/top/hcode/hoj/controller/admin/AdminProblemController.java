package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.service.admin.problem.AdminProblemService;

import java.util.*;

/**
 *
 * @Date: 2020/12/11 21:45
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/problem")
@RequiresRoles(value = { "root", "problem_admin", "admin" }, logical = Logical.OR)
public class AdminProblemController {

    @Autowired
    private AdminProblemService adminProblemService;

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    public CommonResult<IPage<Problem>> getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "auth", required = false) Integer auth,
            @RequestParam(value = "oj", required = false) String oj) {
        return adminProblemService.getProblemList(limit, currentPage, keyword, auth, oj);
    }

    @GetMapping("")
    @RequiresAuthentication
    public CommonResult<Problem> getProblem(@RequestParam("pid") Long pid) {
        return adminProblemService.getProblem(pid);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid) {
        return adminProblemService.deleteProblem(pid);
    }

    @PostMapping("")
    @RequiresAuthentication
    public CommonResult<Void> addProblem(@RequestBody ProblemDTO problemDto) {
        return adminProblemService.addProblem(problemDto);
    }

    @PutMapping("")
    @RequiresAuthentication
    public CommonResult<Void> updateProblem(@RequestBody ProblemDTO problemDto) {
        return adminProblemService.updateProblem(problemDto);
    }

    @GetMapping("/get-problem-cases")
    @RequiresAuthentication
    public CommonResult<List<ProblemCase>> getProblemCases(@RequestParam("pid") Long pid,
            @RequestParam(value = "isUpload", defaultValue = "true") Boolean isUpload) {
        return adminProblemService.getProblemCases(pid, isUpload);
    }

    @PostMapping("/compile-spj")
    @RequiresAuthentication
    public CommonResult compileSpj(@RequestBody CompileDTO compileDTO) {
        return adminProblemService.compileSpj(compileDTO);
    }

    @PostMapping("/compile-interactive")
    @RequiresAuthentication
    public CommonResult compileInteractive(@RequestBody CompileDTO compileDTO) {
        return adminProblemService.compileInteractive(compileDTO);
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    public CommonResult<Void> importRemoteOJProblem(@RequestParam("name") String name,
            @RequestParam("problemId") String problemId) {
        return adminProblemService.importRemoteOJProblem(name, problemId);
    }

    @PutMapping("/change-problem-auth")
    @RequiresAuthentication
    public CommonResult<Void> changeProblemAuth(@RequestBody Problem problem) {
        return adminProblemService.changeProblemAuth(problem);
    }

}