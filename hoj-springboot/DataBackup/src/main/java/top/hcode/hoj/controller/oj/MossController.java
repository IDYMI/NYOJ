package top.hcode.hoj.controller.oj;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestMossImportDTO;
import top.hcode.hoj.pojo.entity.contest.ContestMoss;
import top.hcode.hoj.pojo.vo.ContestMossListVO;
import top.hcode.hoj.pojo.vo.ContestMossResultVO;
import top.hcode.hoj.service.oj.MossService;

import java.util.List;

/**
 *
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class MossController {

    @Autowired
    private MossService mossService;

    @GetMapping("/get-contest-language")
    @RequiresAuthentication
    public CommonResult<List<String>> getContestLanguage(
            @RequestParam("cid") Long cid,
            @RequestParam(value = "excludeAdmin", defaultValue = "false") Boolean excludeAdmin) {
        return mossService.getContestLanguage(cid, excludeAdmin);
    }

    @GetMapping("/get-moss-list")
    @RequiresAuthentication
    public CommonResult<List<ContestMossListVO>> getContest(
            @RequestParam(value = "cid", required = true) Long cid,
            @RequestParam(value = "language", required = false) String language) {
        return mossService.getMossDateList(cid, language);
    }

    @PostMapping("/submit-contest-moss")
    @RequiresAuthentication
    public CommonResult<List<String>> addMoss(
            @RequestBody ContestMossImportDTO contestMossImportDTO) {
        return mossService.addMoss(contestMossImportDTO);
    }

    @GetMapping("/get-contest-moss")
    @RequiresAuthentication
    public CommonResult<IPage<ContestMoss>> getMoss(
            @RequestParam(value = "cid", required = true) Long cid,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "time", required = false) String time) {
        return mossService.getMoss(cid, currentPage, limit, keyword, language, time);
    }

    @GetMapping("/get-contest-moss-result")
    @RequiresAuthentication
    public CommonResult<ContestMossResultVO> getMossResult(
            @RequestParam(value = "id", required = true) Long id,
            @RequestParam(value = "cid", required = true) Long cid) {
        return mossService.getMossResult(id, cid);
    }

}
