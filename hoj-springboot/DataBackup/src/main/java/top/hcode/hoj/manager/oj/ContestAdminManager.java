package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.json.JSONObject;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestPrintEntityService;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.contest.ContestRegisterEntityService;
import top.hcode.hoj.dao.contest.ContestSignEntityService;
import top.hcode.hoj.dao.user.UserSignEntityService;
import top.hcode.hoj.pojo.dto.CheckACDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestPrint;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.contest.ContestRegister;
import top.hcode.hoj.pojo.entity.contest.ContestSign;
import top.hcode.hoj.pojo.entity.user.UserSign;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.pojo.vo.UserSignVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.GroupValidator;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @Date: 2022/3/11 19:40
 * @Description:
 */
@Component
public class ContestAdminManager {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Autowired
    private ContestPrintEntityService contestPrintEntityService;

    @Autowired
    private ContestSignEntityService contestSignEntityService;

    @Autowired
    private UserSignEntityService userSignEntityService;

    @Autowired
    private ContestRegisterEntityService contestRegisterEntityService;

    @Autowired
    private InventManager inventManager;

    @Autowired
    private GroupValidator groupValidator;

    public IPage<ContestRecord> getContestACInfo(Long cid, Integer currentPage, Integer limit)
            throws StatusForbiddenException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取当前比赛的，状态为ac，未被校验的排在前面
        return contestRecordEntityService.getACInfo(currentPage,
                limit,
                Constants.Contest.RECORD_AC.getCode(),
                cid,
                contest.getUid(),
                contest.getStartTime(),
                contest.getEndTime());

    }

    public void checkContestACInfo(CheckACDTO checkACDto) throws StatusFailException, StatusForbiddenException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(checkACDto.getCid());

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = contestRecordEntityService.updateById(
                new ContestRecord().setChecked(checkACDto.getChecked()).setId(checkACDto.getId()));

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }

    }

    public IPage<ContestPrint> getContestPrint(Long cid, Integer currentPage, Integer limit)
            throws StatusForbiddenException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取当前比赛的，未被确定的排在前

        IPage<ContestPrint> contestPrintIPage = new Page<>(currentPage, limit);

        QueryWrapper<ContestPrint> contestPrintQueryWrapper = new QueryWrapper<>();
        contestPrintQueryWrapper.select("id", "cid", "username", "realname", "status", "gmt_create")
                .eq("cid", cid)
                .orderByAsc("status")
                .orderByDesc("gmt_create");

        return contestPrintEntityService.page(contestPrintIPage, contestPrintQueryWrapper);
    }

    public void checkContestPrintStatus(Long id, Long cid) throws StatusFailException, StatusForbiddenException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = contestPrintEntityService.updateById(new ContestPrint().setId(id).setStatus(1));

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }

    public IPage<ContestSign> getContestSign(Long cid, Integer currentPage, Integer limit,
            Boolean type, Boolean gender, Integer status, String keyword)
            throws StatusForbiddenException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取当前比赛的，未被确定的排在前

        IPage<ContestSign> contestContestIPage = new Page<>(currentPage, limit);

        QueryWrapper<ContestSign> contestSignQueryWrapper = new QueryWrapper<>();
        contestSignQueryWrapper.select("id", "cid", "cname", "ename", "school", "team_names",
                "type", "gender", "status", "gmt_create")
                .eq("cid", cid)
                .eq(type != null, "type", type) // 筛选是否打星
                .eq(gender != null, "gender", gender) // 刷选是否为女队
                .eq(status != null, "status", status) // 刷选审核状态
                .orderByAsc("status")
                .orderByDesc("gmt_create");

        // 关键词搜索
        if (!StringUtils.isEmpty(keyword)) {
            final String key = keyword.trim();

            contestSignQueryWrapper.and(wrapper -> wrapper.like("team_names", key)
                    .or()
                    .like("cname", key)
                    .or()
                    .like("ename", key));
        }

        return contestSignEntityService.page(contestContestIPage, contestSignQueryWrapper);
    }

    public ContestSignVO getContestSignInfo(Long cid, Long id) throws StatusForbiddenException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot
                && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        QueryWrapper<ContestSign> contestSignQueryWrapper = new QueryWrapper<>();
        contestSignQueryWrapper.eq("id", id);

        ContestSign contestSign = contestSignEntityService.getOne(contestSignQueryWrapper, false);

        List<UserSignVO> userSignVo = new ArrayList<>();

        if (contestSign != null) {
            userSignVo = inventManager.getTeamConfig(contestSign);
        }

        ContestSignVO contestSignVo = new ContestSignVO();

        contestSignVo.setId(contestSign.getId());
        contestSignVo.setCname(contestSign.getCname());
        contestSignVo.setEname(contestSign.getEname());
        contestSignVo.setSchool(contestSign.getSchool());
        contestSignVo.setTeamConfig(userSignVo);
        contestSignVo.setType(contestSign.getType());
        return contestSignVo;
    }

    public void checkContestSignStatus(Map<String, Object> params)
            throws StatusFailException, StatusForbiddenException {

        List<Integer> ids = (List<Integer>) params.get("ids");
        Long cid = Long.valueOf((String) params.get("cid"));
        Integer status = (Integer) params.get("status");
        String msg = (String) params.get("msg");

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        // 遍历要处理的 contestSign 列表
        for (Integer id_number : ids) {
            Long id = Long.valueOf(id_number);
            updateContestSignStatus(id, cid, status, msg);
        }
    }

    public void updateContestSign(ContestSignVO contestSignVo)
            throws StatusFailException, StatusForbiddenException {

        Long id = contestSignVo.getId();
        Long cid = contestSignVo.getCid();

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        // 队列中的竞赛信息
        List<UserSignVO> userSignVoList = contestSignVo.getTeamConfig();

        // 更新队列中人的竞赛信息
        for (UserSignVO userSignVo : userSignVoList) {
            userSignEntityService.updateById(new UserSign()
                    .setId(userSignVo.getId())
                    .setUid(userSignVo.getUid())
                    .setUsername(userSignVo.getUsername())
                    .setRealname(userSignVo.getRealname())
                    .setSchool(userSignVo.getSchool())
                    .setCourse(userSignVo.getCourse())
                    .setNumber(userSignVo.getNumber())
                    .setClothesSize(userSignVo.getClothesSize())
                    .setPhoneNumber(userSignVo.getPhoneNumber()));
        }

        List<String> team_names = userSignVoList.stream()
                .map(UserSignVO::getUsername)
                .collect(Collectors.toList());

        // 更新该比赛的队伍信息
        JSONObject configJson = new JSONObject();
        configJson.set("config", userSignVoList);
        boolean isOk = contestSignEntityService
                .updateById(new ContestSign()
                        .setId(id)
                        .setSchool(contestSignVo.getSchool())
                        .setTeamNames(String.join("$", team_names))
                        .setTeamConfig(configJson.toString())
                        .setParticipants(team_names.size())
                        .setType(contestSignVo.getType())
                        .setGender(contestSignVo.getGender()));

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }

    public void updateContestSignStatus(Long id, Long cid, Integer status, String msg)
            throws StatusFailException {

        // 批量处理用户的进入比赛的权限
        QueryWrapper<ContestSign> contestSignQueryWrapper = new QueryWrapper<>();
        contestSignQueryWrapper.select("team_names").eq("id", id);
        ContestSign contestSign = contestSignEntityService.getOne(contestSignQueryWrapper);

        if (contestSign != null) { // 如果报名状态存在
            List<String> team_names = Arrays.stream(contestSign.getTeamNames().split("\\$"))
                    .collect(Collectors.toList());

            for (String username : team_names) {
                if (status == 1) {
                    UserSign userSign = inventManager.getUserSign(username);
                    if (userSign != null) {
                        // 添加用户进入比赛的权限
                        addContestResigter(cid, userSign);
                    }
                } else {
                    // 删除用户进入比赛的权限
                    inventManager.removeContestResigter(cid, username);
                }
            }
        }

        boolean isOk = contestSignEntityService
                .updateById(new ContestSign()
                        .setId(id)
                        .setStatus(status)
                        .setMsg(msg != null ? msg : null));

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }

    }

    public void addContestResigter(Long cid, UserSign userSign) {
        // 判断是否有比赛通过
        QueryWrapper<ContestRegister> contestRegisterQueryWrapper = new QueryWrapper<>();
        contestRegisterQueryWrapper.eq("cid", cid).eq("uid", userSign.getUid());
        ContestRegister contestRegister = contestRegisterEntityService
                .getOne(contestRegisterQueryWrapper);

        if (contestRegister == null) {
            // 如果状态为通过，更新该组用户进入比赛资格
            contestRegister = new ContestRegister()
                    .setCid(cid)
                    .setUid(userSign.getUid())
                    .setStatus(0); // 设置为通过状态

            contestRegisterEntityService.saveOrUpdate(contestRegister);
        }
    }
}