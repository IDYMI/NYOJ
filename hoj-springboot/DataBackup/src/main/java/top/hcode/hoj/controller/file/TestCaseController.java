package top.hcode.hoj.controller.file;

import top.hcode.hoj.common.exception.StatusForbiddenException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.service.file.TestCaseService;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *
 * @Date: 2021/10/5 19:51
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
public class TestCaseController {

    @Autowired
    private TestCaseService testCaseService;

    @PostMapping("/upload-testcase-zip")
    @ResponseBody
    @RequiresAuthentication
    public CommonResult<Map<Object, Object>> uploadTestcaseZip(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "default") String mode,
            @RequestParam(value = "gid", required = false) Long gid) {
        return testCaseService.uploadTestcaseZip(file, gid, mode);
    }

    @GetMapping("/download-testcase")
    @RequiresAuthentication
    public void downloadTestcase(
            @RequestParam(value = "pid", required = false) Long pid,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "fileListDir", required = false) String fileListDir,
            HttpServletResponse response)
            throws StatusFailException, StatusForbiddenException {
        testCaseService.downloadTestcase(pid, name, fileListDir, response);
    }
}