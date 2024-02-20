package top.hcode.hoj.controller.file;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.service.file.BoxFileService;

import java.util.*;

@Controller
@RequestMapping("/api/file")
public class BoxFileController {

    @Autowired
    private BoxFileService ideFileService;

    @PostMapping("/upload-file")
    @ResponseBody
    @RequiresAuthentication
    public CommonResult<Map<Object, Object>> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "gid", required = false) Long gid) {
        return ideFileService.uploadFile(file, gid);
    }

    @RequestMapping(value = "/delete-file", method = RequestMethod.GET)
    @RequiresAuthentication
    @ResponseBody
    public CommonResult<Void> deleteFile(@RequestParam("fileId") Long fileId) {
        return ideFileService.deleteFile(fileId);
    }

}