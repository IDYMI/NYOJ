package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.manager.oj.InventManager;
import top.hcode.hoj.pojo.dto.InventDTO;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.pojo.vo.InventVO;
import top.hcode.hoj.pojo.vo.ReplyVO;
import top.hcode.hoj.pojo.vo.UserInventVO;
import top.hcode.hoj.service.oj.InventService;

import javax.annotation.Resource;

/**
 *
 * @Date: 2022/3/11 16:21
 * @Description:
 */
@Service
public class InventServiceImpl implements InventService {

    @Resource
    private InventManager inventManager;

    @Override
    public CommonResult<UserInventVO> addInvent(InventDTO inventDto) {
        try {
            return CommonResult.successResponse(inventManager.addInvent(inventDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Integer> getInventStatus(Long cid, String username, String toUsername) {
        try {
            return CommonResult.successResponse(inventManager.getInventStatus(cid, username, toUsername));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteInvent(Long cid, String username, String toUsername) {
        try {
            inventManager.deleteInvent(cid, username, toUsername);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ReplyVO> handleInvent(InventVO inventvo) {
        try {
            return CommonResult.successResponse(inventManager.handleInvent(inventvo));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ContestSignVO> addSign(ContestSignVO ContestSignvo) {
        try {
            return CommonResult.successResponse(inventManager.addSign(ContestSignvo));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ContestSignVO> getSign(Long cid, String username) {
        try {
            return CommonResult.successResponse(inventManager.getSign(cid, username));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

}