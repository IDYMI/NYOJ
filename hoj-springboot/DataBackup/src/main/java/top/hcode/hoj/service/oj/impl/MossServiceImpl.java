package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.manager.oj.MossManager;
import top.hcode.hoj.pojo.dto.ContestMossImportDTO;
import top.hcode.hoj.pojo.entity.contest.ContestMoss;
import top.hcode.hoj.pojo.vo.ContestMossListVO;
import top.hcode.hoj.pojo.vo.ContestMossResultVO;
import top.hcode.hoj.service.oj.MossService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Date;

/**
 *
 * @Description:
 */
@Service
public class MossServiceImpl implements MossService {

    @Resource
    private MossManager mossManager;

    @Override
    public CommonResult<List<String>> getContestLanguage(Long cid, Boolean excludeAdmin) {
        try {
            return CommonResult.successResponse(mossManager.getContestLanguage(cid, excludeAdmin));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<ContestMossListVO>> getMossDateList(Long cid, String language) {
        try {
            return CommonResult.successResponse(mossManager.getMossDateList(cid, language));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<String>> addMoss(ContestMossImportDTO contestMossImportDTO) {
        try {
            return CommonResult.successResponse(mossManager.addMoss(contestMossImportDTO));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @Override
    public CommonResult<IPage<ContestMoss>> getMoss(Long cid, Integer currentPage, Integer limit, String keyword,
            String language, String time) {
        try {
            return CommonResult.successResponse(mossManager.getMoss(cid, currentPage, limit, keyword, language, time));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ContestMossResultVO> getMossResult(Long id, Long cid) {
        try {
            return CommonResult.successResponse(mossManager.getMossResult(id, cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

}