package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.InventDTO;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.pojo.vo.InventVO;
import top.hcode.hoj.pojo.vo.ReplyVO;
import top.hcode.hoj.pojo.vo.UserInventVO;

public interface InventService {

    public CommonResult<UserInventVO> addInvent(InventDTO inventDto);

    public CommonResult<Integer> getInventStatus(Long cid, String username, String toUsername);

    public CommonResult<Void> deleteInvent(Long cid, String username, String toUsername);

    public CommonResult<ReplyVO> handleInvent(InventVO inventvo);

    public CommonResult<ContestSignVO> addSign(ContestSignVO ContestSignvo);

    public CommonResult<ContestSignVO> getSign(Long cid, String username);

}
