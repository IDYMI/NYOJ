package top.hcode.hoj.controller.file;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.service.file.ImportFpsProblemService;
import org.apache.shiro.authz.annotation.Logical;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/5 19:45
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@RequiresRoles(value = { "root", "problem_admin", "admin" }, logical = Logical.OR)
public class ImportFpsProblemController {

    @Resource
    private ImportFpsProblemService importFpsProblemService;

    /**
     * @param file
     * @MethodName importFpsProblem
     * @Description zip文件导入题目
     * @Return
     * @Since 2021/10/06
     */

    @RequiresAuthentication
    @ResponseBody
    @PostMapping("/import-fps-problem")
    public CommonResult<Void> importFPSProblem(@RequestParam("file") MultipartFile file) {
        return importFpsProblemService.importFPSProblem(file);
    }

}