package top.hcode.hoj.manager.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.dao.common.FileEntityService;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.GroupValidator;

import java.io.*;
import java.util.*;

/**
 *
 * @Date: 2022/3/10 14:57
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class BoxFileManager {

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private FileEntityService fileEntityService;

    public Map<Object, Object> uploadFile(MultipartFile file, Long gid)
            throws StatusFailException, StatusSystemErrorException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot && !(gid != null && groupValidator.isGroupAdmin(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (file == null) {
            throw new StatusFailException("上传的文件不能为空！");
        }
        if (file.getSize() > 1024 * 1024 * 512) {
            throw new StatusFailException("上传的文件大小不能大于512M！");
        }

        // 获取文件后缀
        String suffix = "";
        String filename = "";
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            // 通过UUID生成唯一文件名
            filename = IdUtil.simpleUUID() + "." + suffix;
        } else {
            filename = IdUtil.simpleUUID();
        }

        // 若不存在该目录，则创建目录
        FileUtil.mkdir(Constants.File.BOXFILE_BASE_FOLDER.getPath());

        try {
            // 将文件保存指定目录
            file.transferTo(FileUtil.file(Constants.File.BOXFILE_BASE_FOLDER.getPath() + File.separator + filename));
        } catch (Exception e) {
            log.error("文件上传异常-------------->", e);
            throw new StatusSystemErrorException("服务器异常：文件上传失败！");
        }

        top.hcode.hoj.pojo.entity.common.File file1 = new top.hcode.hoj.pojo.entity.common.File();
        file1.setFolderPath(Constants.File.BOXFILE_BASE_FOLDER.getPath())
                .setName(filename)
                .setFilePath(Constants.File.BOXFILE_BASE_FOLDER.getPath() + File.separator + filename)
                .setSuffix(suffix)
                .setType("file") // 设置为file文件
                .setHint(file.getOriginalFilename()) // 设置显示的文件名称
                .setUid(userRolesVo.getUid());
        fileEntityService.save(file1);

        return MapUtil.builder()
                .put("id", file1.getId())
                .put("hint", file1.getHint())
                .put("url", Constants.File.FILE_API.getPath() + filename)
                .map();
    }

    public void deleteFile(Long fileId) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        top.hcode.hoj.pojo.entity.common.File file = fileEntityService.getById(fileId);

        if (file == null) {
            throw new StatusFailException("错误：文件不存在！");
        }

        if (!file.getType().equals("file")) {
            throw new StatusForbiddenException("错误：不支持删除！");
        }

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        Long gid = file.getGid();

        if (!file.getUid().equals(userRolesVo.getUid())
                && !isRoot
                && !(gid != null && groupValidator.isGroupAdmin(userRolesVo.getUid(), gid))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = FileUtil.del(file.getFilePath());
        if (isOk) {
            fileEntityService.removeById(fileId);
        } else {
            throw new StatusFailException("删除失败");
        }
    }

}