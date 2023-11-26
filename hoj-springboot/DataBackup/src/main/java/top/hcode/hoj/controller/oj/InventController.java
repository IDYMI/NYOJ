package top.hcode.hoj.controller.oj;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.InventDTO;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.pojo.vo.InventVO;
import top.hcode.hoj.pojo.vo.ReplyVO;
import top.hcode.hoj.pojo.vo.UserInventVO;
import top.hcode.hoj.service.oj.InventService;

/**
 *
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class InventController {

    @Autowired
    private InventService inventService;

    @PostMapping("/invent")
    @RequiresAuthentication
    public CommonResult<UserInventVO> addInvent(
            @RequestBody InventDTO inventDto) {
        return inventService.addInvent(inventDto);
    }

    @GetMapping("/invent")
    @RequiresAuthentication
    public CommonResult<Integer> getInventStatus(
            @RequestParam("cid") Long cid,
            @RequestParam("username") String username,
            @RequestParam("toUsername") String toUsername) {
        return inventService.getInventStatus(cid, username, toUsername);
    }

    @DeleteMapping("/invent")
    public CommonResult<Void> deleteInvent(
            @RequestParam("cid") Long cid,
            @RequestParam("username") String username,
            @RequestParam("toUsername") String toUsername) {
        return inventService.deleteInvent(cid, username, toUsername);
    }

    @PostMapping("/handle-invent")
    @RequiresAuthentication
    public CommonResult<ReplyVO> handleInvent(
            @RequestBody InventVO inventvo) {
        return inventService.handleInvent(inventvo);
    }

    @PostMapping("/sign")
    @RequiresAuthentication
    public CommonResult<ContestSignVO> addSign(
            @RequestBody ContestSignVO ContestSignvo) {
        return inventService.addSign(ContestSignvo);
    }

    @GetMapping("/sign")
    @RequiresAuthentication
    public CommonResult<ContestSignVO> getSign(
            @RequestParam("cid") Long cid,
            @RequestParam("username") String username) {
        return inventService.getSign(cid, username);
    }
}
