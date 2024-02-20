package top.hcode.hoj.service.file.impl;

import top.hcode.hoj.common.exception.StatusForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.file.BoxFileManager;
import top.hcode.hoj.service.file.BoxFileService;

import javax.annotation.Resource;

import java.util.Map;

/**
 *
 * @Date: 2022/3/10 15:08
 * @Description:
 */
@Service
public class BoxFileServiceImpl implements BoxFileService {

    @Resource
    private BoxFileManager iDEFileManager;

    @Override
    public CommonResult<Map<Object, Object>> uploadFile(MultipartFile file, Long gid) {
        try {
            return CommonResult.successResponse(iDEFileManager.uploadFile(file, gid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteFile(Long fileId) {
        try {
            iDEFileManager.deleteFile(fileId);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

}