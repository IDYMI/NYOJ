package top.hcode.hoj.service.oj;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestMossImportDTO;
import top.hcode.hoj.pojo.entity.contest.ContestMoss;
import top.hcode.hoj.pojo.vo.ContestMossListVO;
import top.hcode.hoj.pojo.vo.ContestMossResultVO;

public interface MossService {

    public CommonResult<List<String>> getContestLanguage(Long cid, Boolean excludeAdmin);

    public CommonResult<List<ContestMossListVO>> getMossDateList(Long cid, String language);

    public CommonResult<List<String>> addMoss(ContestMossImportDTO contestMossImportDTO);

    public CommonResult<IPage<ContestMoss>> getMoss(Long cid, Integer currentPage, Integer limit, String keyword,
            String language, String time);

    public CommonResult<ContestMossResultVO> getMossResult(Long id, Long cid);

}
