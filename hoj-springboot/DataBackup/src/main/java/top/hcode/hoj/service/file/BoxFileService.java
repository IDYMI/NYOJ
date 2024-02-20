package top.hcode.hoj.service.file;

import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.result.CommonResult;

import java.util.Map;

/**
 *
 * @Date: 2022/3/10 15:08
 * @Description:
 */
public interface BoxFileService {

    public CommonResult<Map<Object, Object>> uploadFile(MultipartFile file, Long gid);

    public CommonResult<Void> deleteFile(Long fileId);

}