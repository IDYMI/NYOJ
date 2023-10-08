package top.hcode.hoj.controller.file;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.service.file.ImportHydroProblemService;
import org.apache.shiro.authz.annotation.Logical;

/**
 * @Author: Himit_ZH
 * @Date: 2022/10/16 00:19
 * @Description:
 */

@Controller
@RequestMapping("/api/file")
@RequiresRoles(value = { "root", "problem_admin", "admin" }, logical = Logical.OR)
public class ImportHydroProblemController {

    @Autowired
    private ImportHydroProblemService importHydroProblemService;

    /**
     * @param file
     * @MethodName importQDOJProblem
     * @Description zip文件导入题目
     * @Return
     * @Since 2022/10/16
     */

    @RequiresAuthentication
    @ResponseBody
    @PostMapping("/import-hydro-problem")
    public CommonResult<Void> importHydroProblem(@RequestParam("file") MultipartFile file) {
        return importHydroProblemService.importHydroProblem(file);
    }

}