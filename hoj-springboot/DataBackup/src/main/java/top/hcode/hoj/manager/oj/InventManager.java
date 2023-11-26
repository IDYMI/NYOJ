package top.hcode.hoj.manager.oj;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestRegisterEntityService;
import top.hcode.hoj.dao.contest.ContestSignEntityService;
import top.hcode.hoj.dao.discussion.CommentEntityService;
import top.hcode.hoj.dao.discussion.ReplyEntityService;
import top.hcode.hoj.dao.msg.MsgRemindEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.dao.user.UserSignEntityService;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.pojo.dto.CheckUsernameOrEmailDTO;
import top.hcode.hoj.pojo.dto.InventDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRegister;
import top.hcode.hoj.pojo.entity.contest.ContestSign;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.entity.user.Role;
import top.hcode.hoj.pojo.entity.user.UserSign;
import top.hcode.hoj.pojo.vo.CheckCnameOrEnameVO;
import top.hcode.hoj.pojo.vo.CheckUsernameOrEmailVO;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.pojo.vo.InventVO;
import top.hcode.hoj.pojo.vo.ReplyVO;
import top.hcode.hoj.pojo.vo.UserInventStatusVO;
import top.hcode.hoj.pojo.vo.UserInventVO;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.pojo.vo.UserSignVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.CommonValidator;
import top.hcode.hoj.utils.Constants;

import java.util.stream.Collectors;
import java.util.*;

import javax.annotation.Resource;

/**
 *
 * @Description:
 */
@Component
public class InventManager {

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    @Autowired
    private CommentEntityService commentEntityService;

    @Autowired
    private MsgRemindEntityService msgRemindEntityService;

    @Autowired
    private ReplyEntityService replyEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private UserSignEntityService userSignEntityService;

    @Autowired
    private ContestSignEntityService contestSignEntityService;

    @Autowired
    private ContestRegisterEntityService contestRegisterEntityService;

    @Resource
    private AccountManager accountManager;

    @Autowired
    private CommonValidator commonValidator;

    @Transactional
    public UserInventVO addInvent(InventDTO inventDto)
            throws StatusFailException, StatusForbiddenException, AccessException {

        Long cid = inventDto.getCid();
        String username = inventDto.getUsername();
        String toUsername = inventDto.getToUsername();
        String content = inventDto.getContent();

        commonValidator.validateContentLength(toUsername, "邀请人", 30);

        if (username == null
                || toUsername == null
                || (!StringUtils.isEmpty(username)
                        && !StringUtils.isEmpty(toUsername)
                        && username.equals(toUsername))) {
            throw new StatusFailException("发送邀请失败，请求参数错误！");
        }

        if (cid == null) {
            throw new StatusFailException("发送邀请失败，请在比赛中邀请");
        }
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("发送邀请失败，该比赛已不存在！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        // 检查是否可以使用邀请功能
        checkInvent(contest, isRoot);

        // 检查用户是否存在
        CheckUsernameOrEmailDTO CheckUsernameOrEmailDto = new CheckUsernameOrEmailDTO();
        CheckUsernameOrEmailDto.setUsername(toUsername);
        CheckUsernameOrEmailVO CheckUsernameOrEmailvo = accountManager
                .checkUsernameOrEmail(CheckUsernameOrEmailDto);
        if (!CheckUsernameOrEmailvo.getUsername()) {
            throw new StatusFailException("发送邀请失败，用户名 : " + toUsername + "不存在！");
        }

        // 检查用户是否为管理员
        List<String> roles = userRoleEntityService.getRolesByUid(getUserSign(toUsername).getUid()).stream()
                .map(Role::getRole)
                .collect(Collectors.toList()); // 获取该用户角色所有的权限
        boolean containsRoot = roles.contains("root") || roles.contains("admin") || roles.contains("problem_admin");
        if (containsRoot) {
            throw new StatusFailException("发送邀请失败，用户名 : " + toUsername + "为管理员！");
        }

        UserSignVO toUserSignVo = getUserSignVO(cid, null, toUsername, false);

        // 判断被邀请者是否加入过队伍
        if (toUserSignVo.getStatus() == 1) {
            throw new StatusFailException("发送邀请失败，邀请者已报名过比赛！");
        }

        QueryWrapper<ContestSign> contestSignQueryWrapper = new QueryWrapper<>();
        contestSignQueryWrapper.eq("cid", cid).like("team_names", username);
        ContestSign contestSign = contestSignEntityService.getOne(contestSignQueryWrapper, false);

        if (contestSign != null) {

            // 将被邀请的加入到队伍中
            String teamNames = contestSign.getTeamNames();
            Integer teamNumber = contestSign.getParticipants();
            Integer maxParticipants = contest.getMaxParticipants();

            // 如果当前队伍已经达到最大人数，不能邀请
            if (teamNumber > maxParticipants) {
                throw new StatusFailException("发送邀请失败，该比赛队伍要求最多" + maxParticipants.toString() + "人！");
            }

            List<String> team_names = getTeamNames(teamNames);

            team_names.add(toUsername);

            List<UserSignVO> userSignVoList = checkContestSignList(cid, team_names);

            JSONObject configJson = new JSONObject();
            configJson.set("config", userSignVoList);

            contestSign.setParticipants(teamNumber + 1);
            contestSign.setTeamNames(String.join("$", team_names));
            contestSign.setTeamConfig(configJson.toString());
            contestSign.setStatus(0); // 改为审核中

            boolean isOk2 = contestSignEntityService.updateById(contestSign);

            if (!isOk2) {
                throw new StatusFailException("发送邀请失败，请重新尝试！");
            }
            // 回收该队伍的所有进入比赛权限
            removeTeamContestResigter(cid, team_names);
        }

        // 判断发出者是否发送过邀请
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id")
                .eq("cid", cid)
                .eq("from_name", username)
                .eq("status", 2);
        Comment comment = commentEntityService.getOne(queryWrapper, false);

        if (comment == null) {
            // 首次发送邀请
            String fromRole = "user";
            if (SecurityUtils.getSubject().hasRole("root")) {
                fromRole = "root";
            } else if (isRoot) {
                fromRole = "admin";
            }

            comment = new Comment()
                    .setFromAvatar(userRolesVo.getAvatar())
                    .setFromName(userRolesVo.getUsername())
                    .setFromUid(userRolesVo.getUid())
                    .setCid(cid)
                    .setStatus(2)// 设置消息类型为邀请信息)
                    .setContent("<< " + username + " >> Invent Action")
                    .setFromRole(fromRole);

            boolean isOk = commentEntityService.saveOrUpdate(comment);

            if (!isOk) {
                throw new StatusFailException("发送邀请失败，请重新尝试！");
            }
        }

        // 获取邀请人的用户信息
        UserSign userSign = getUserSign(username);
        UserSign toUserSign = getUserSign(toUsername);

        // 发送邮箱提醒邀请信息
        sentInvent(cid, userSign, toUserSign, content, comment);

        // 返回被邀请者的信息和邀请的id
        UserInventVO userInventVo = new UserInventVO();
        userInventVo.setId(comment.getId());
        userInventVo.setUsername(username);
        userInventVo.setToUsername(toUsername);
        userInventVo.setUserSignVO(toUserSignVo);
        return userInventVo;
    }

    @Transactional
    public Integer getInventStatus(Long cid, String username, String toUsername)
            throws StatusFailException, StatusForbiddenException, AccessException {

        if (username == null
                || toUsername == null
                || (!StringUtils.isEmpty(username)
                        && !StringUtils.isEmpty(toUsername)
                        && username.equals(toUsername))) {
            throw new StatusFailException("发送邀请失败，请求参数错误！");
        }

        if (cid == null) {
            throw new StatusFailException("获取邀请状态失败，请在比赛中获取邀请状态");
        }
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("获取邀请状态失败，该比赛已不存在！");
        }

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        // 检查是否可以使用邀请功能
        checkInvent(contest, isRoot);

        UserSign userSign = getUserSign(username);
        UserSign toUserSign = getUserSign(toUsername);

        if (userSign == null || userSign == null) {
            throw new StatusFailException("发送邀请失败，邀请人不存在！");
        }

        // 查询对应比赛,该用户接受邀请对应的处理状态
        return getInventedStatusBl(cid, userSign.getUid(), toUserSign.getUid());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteInvent(Long cid, String username, String toUsername)
            throws StatusForbiddenException, StatusFailException, AccessException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        if (cid == null) {
            throw new StatusFailException("删除邀请失败，请在比赛中删除邀请");
        }
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("删除邀请失败，该比赛已不存在！");
        }

        // 检查是否可以使用邀请功能
        checkInvent(contest, isRoot);

        UserSign userSign = getUserSign(username);
        UserSign toUserSign = getUserSign(toUsername);

        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.select("id", "from_uid")
                .eq("cid", cid)
                .eq("from_uid", userSign.getUid())
                .eq("status", 2);
        Comment comment = commentEntityService.getOne(commentQueryWrapper, false);

        if (comment == null) {
            throw new StatusFailException("删除失败，当前邀请已不存在！");
        }

        // 如果不是评论本人 或者不是管理员 无权限删除该邀请
        if (!comment.getFromUid().equals(userRolesVo.getUid())
                && !isRoot
                && !contest.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("无权删除该邀请");
        }

        // 如果被邀请用户同意该用户，回收该用户的进比赛权限
        if (getInventedStatusBl(cid, userSign.getUid(), toUserSign.getUid()) == 1) {
            removeContestResigter(cid, toUsername);
        }

        // 删除对该用户的所有邀请数据
        msgRemindEntityService.remove(new UpdateWrapper<MsgRemind>()
                .eq("quote_id", comment.getId())
                .eq("action", "Invent")
                .eq("sender_id", userSign.getUid())
                .eq("recipient_id", toUserSign.getUid()));

        replyEntityService.remove(new UpdateWrapper<Reply>()
                .eq("comment_id", comment.getId())
                .eq("to_uid", userSign.getUid())
                .eq("from_uid", toUserSign.getUid()));

        // 更新报名表
        QueryWrapper<ContestSign> wrapper = new QueryWrapper<ContestSign>()
                .select("id", "team_names")
                .likeRight("team_names", username)
                .eq("cid", cid);
        ContestSign contestSign = contestSignEntityService.getOne(wrapper, false);

        // 如果存在报名表
        if (contestSign != null) {
            // 更新对应的人员名单，人员人数
            String teamNames = contestSign.getTeamNames();

            // 检查队伍名称是否非空
            if (!StringUtils.isEmpty(teamNames)) {
                List<String> team_names = getTeamNames(teamNames);

                // 将 toUsername 从队伍中去除
                List<String> newTeamNames = team_names.stream()
                        .filter(name -> !name.equals(toUsername))
                        .collect(Collectors.toList());

                List<UserSignVO> newUserSignVoList = checkContestSignList(cid, newTeamNames);

                if (newUserSignVoList.size() > 1) {
                    UpdateWrapper<ContestSign> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", contestSign.getId())
                            .set("team_names", String.join("$", newTeamNames))
                            .set("team_config", new JSONObject().set("config", newUserSignVoList).toString())
                            .set("participants", newUserSignVoList.size())
                            .set("status", newUserSignVoList.size() > 1 ? 0 : -1);
                    contestSignEntityService.update(updateWrapper);
                } else {
                    // 队伍少于两人删除报名信息
                    contestSignEntityService.remove(new UpdateWrapper<ContestSign>()
                            .eq("id", contestSign.getId()));

                    // 收回进入比赛权限
                    removeContestResigter(cid, username);
                }
            }
        }
    }

    public ReplyVO handleInvent(InventVO inventvo)
            throws StatusFailException, StatusForbiddenException, AccessException {

        UserMsgVO UserMsgvo = inventvo.getUserMsg();
        Boolean isAccept = inventvo.getIsAccept();
        String username = inventvo.getUsername();

        if (UserMsgvo == null || username == null || isAccept == null) {
            throw new StatusFailException("回复邀请失败，当前请求的参数错误！");
        }
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        Long cid = UserMsgvo.getSourceId().longValue();
        Integer id = UserMsgvo.getQuoteId();

        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("回复邀请失败，该比赛已不存在！");
        }

        // 检查是否可以使用邀请功能
        checkInvent(contest, isRoot);

        // 获取被邀请人的用户信息
        UserSign userSign = getUserSign(username);
        if (userSign == null) {
            throw new StatusFailException("回复邀请失败，被邀请人不存在！");
        }

        QueryWrapper<MsgRemind> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.select("id")
                .eq("quote_id", id)
                .eq("source_id", cid)
                .eq("recipient_id", userSign.getUid());
        MsgRemind msgRemind = msgRemindEntityService.getOne(queryWrapper2, false);

        // 不存在邀请
        if (msgRemind == null && !isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("对不起，您无权限回复！");
        }

        // 判断是否存在处理邀请记录
        QueryWrapper<Reply> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id")
                .eq("comment_id", id)
                .eq("from_name", username)
                .eq("to_name", UserMsgvo.getSenderUsername());
        Reply reply = replyEntityService.getOne(queryWrapper, false);

        if (reply == null) {
            // 未处理过邀请
            String fromRole = "user";
            if (SecurityUtils.getSubject().hasRole("root")) {
                fromRole = "root";
            } else if (isRoot) {
                fromRole = "admin";
            }

            reply = new Reply()
                    .setCommentId(id)
                    .setFromAvatar(userRolesVo.getAvatar())
                    .setFromName(userRolesVo.getUsername())
                    .setFromUid(userRolesVo.getUid())
                    .setToAvatar(UserMsgvo.getSenderAvatar())
                    .setToName(UserMsgvo.getSenderUsername())
                    .setToUid(UserMsgvo.getSenderId())
                    .setContent(isAccept.toString())
                    .setStatus(2) // 设置为邀请信息
                    .setFromRole(fromRole);

            boolean isOk = replyEntityService.saveOrUpdate(reply);

            if (!isOk) {
                throw new StatusFailException("回复邀请失败，请重新尝试！");
            }

        } else {
            UpdateWrapper<Reply> replyUpdateWrapper = new UpdateWrapper<>();
            replyUpdateWrapper.eq("comment_id", id)
                    .eq("from_name", username)
                    .eq("to_name", UserMsgvo.getSenderUsername())
                    .set("content", isAccept.toString());
            replyEntityService.update(replyUpdateWrapper);
        }

        // 如果同意
        if (isAccept) {
            QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.select("id")
                    .eq("status", 2)
                    .eq("cid", cid)
                    .eq("from_name", username);
            Comment comment = commentEntityService.getOne(commentQueryWrapper, false);
            if (comment != null) {
                // 查询自己发出邀请的队伍
                msgRemindEntityService.remove(new UpdateWrapper<MsgRemind>().eq("quote_id", comment.getId()));
                replyEntityService.remove(new UpdateWrapper<Reply>().eq("comment_id", comment.getId()));

            }

            // 如果存在报名表
            QueryWrapper<ContestSign> wrapper = new QueryWrapper<ContestSign>()
                    .likeRight("team_names", UserMsgvo.getSenderUsername())
                    .eq("cid", cid);
            ContestSign contestSign = contestSignEntityService.getOne(wrapper, false);

            if (contestSign != null) {
                // 回收该用户队长，对应队伍的进比赛权限

                List<String> team_names = Arrays.stream(contestSign.getTeamNames().split("\\$"))
                        .collect(Collectors.toList());

                // 回收该队伍的所有进入比赛权限
                removeTeamContestResigter(cid, team_names);

                // 添加该用户
                team_names.add(username);

                List<UserSignVO> newUserSignVoList = checkContestSignList(cid, team_names);

                // 更新对应的人员名单，人员人数
                UpdateWrapper<ContestSign> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", contestSign.getId())
                        .set("team_names", String.join("$", team_names))
                        .set("team_config", new JSONObject().set("config", newUserSignVoList).toString())
                        .set("participants", newUserSignVoList.size())
                        .set("status", newUserSignVoList.size() > 1 ? 0 : -1);
                contestSignEntityService.update(updateWrapper);

            }
        }

        ReplyVO replyVo = new ReplyVO();
        BeanUtil.copyProperties(reply, replyVo);
        replyVo.setFromTitleName(userRolesVo.getTitleName());
        replyVo.setFromTitleColor(userRolesVo.getTitleColor());
        return replyVo;
    }

    @Transactional
    public ContestSignVO addSign(ContestSignVO ContestSignvo)
            throws StatusFailException, StatusForbiddenException, AccessException {

        Long cid = ContestSignvo.getCid();
        String teamNames = ContestSignvo.getTeamNames();
        String cname = ContestSignvo.getCname();
        String ename = ContestSignvo.getEname();
        String school = ContestSignvo.getSchool();

        commonValidator.validateContentLength(cname, "队伍中文名称", 20);
        commonValidator.validateContentLength(ename, "队伍英文名称", 20);
        commonValidator.validateContentLength(school, "学校", 20);
        commonValidator.validateContentLength(teamNames, "队伍用户id", 150);

        if (cid == null) {
            throw new StatusFailException("报名失败，请在比赛中报名");
        }
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("报名失败，该比赛已不存在！");
        }

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        // 检查是否可以使用邀请功能
        checkInvent(contest, isRoot);

        // 取出所有的用户名
        List<String> team_names = getTeamNames(teamNames);
        if (CollectionUtils.isEmpty(team_names)) {
            throw new StatusFailException("报名失败，没有传入报名用户名！");
        }

        List<UserSignVO> userSignVoList = checkContestSignList(cid, team_names); // 所有队员的竞赛信息

        // 所有人已经同意
        if (userSignVoList.stream().anyMatch(userSignVo -> userSignVo.getStatus() != 1)) {
            throw new StatusFailException("报名失败，请等待所有人同意！");
        }

        if (userSignVoList.size() > contest.getMaxParticipants()) {
            throw new StatusFailException("报名失败，队伍人数超过比赛人数上限！");
        }

        JSONObject configJson = new JSONObject();
        configJson.set("config", userSignVoList);

        // 找到队伍队长是否报名过比赛
        QueryWrapper<ContestSign> wrapper = new QueryWrapper<ContestSign>()
                .likeRight("team_names", team_names.get(0))
                .eq("cid", cid);
        ContestSign contestSign = contestSignEntityService.getOne(wrapper, false);

        if (contestSign == null) {
            // 提交报名要求队伍名称不重复
            CheckCnameOrEnameVO CheckCnameOrEnameVo = checkCnameOrEname(cid, cname, ename);
            if (CheckCnameOrEnameVo.getCname()) {
                throw new StatusFailException("报名失败，队伍中文名称已被报名！");
            }
            if (CheckCnameOrEnameVo.getEname()) {
                throw new StatusFailException("报名失败，队伍英文名称已被报名！");
            }

            Boolean isOk = contestSignEntityService.saveOrUpdate(new ContestSign()
                    .setCid(cid)
                    .setCname(ContestSignvo.getCname())
                    .setEname(ContestSignvo.getEname())
                    .setSchool(ContestSignvo.getSchool())
                    .setTeamNames(ContestSignvo.getTeamNames())
                    .setTeamConfig(configJson.toString())
                    .setParticipants(userSignVoList.size())
                    .setType(ContestSignvo.getType())
                    .setGender(ContestSignvo.getGender())
                    .setStatus(0)); // 设置为审核中
            if (isOk) {
                ContestSignvo.setTeamConfig(userSignVoList);
                return ContestSignvo;
            } else {
                throw new StatusFailException("提交报名失败！");
            }
        } else { // 如果队长提交过报名
            UpdateWrapper<ContestSign> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", contestSign.getId())
                    .set("cname", ContestSignvo.getCname())
                    .set("ename", ContestSignvo.getEname())
                    .set("school", ContestSignvo.getSchool())
                    .set("team_names", ContestSignvo.getTeamNames())
                    .set("team_config", configJson.toString())
                    .set("participants", userSignVoList.size())
                    .set("type", ContestSignvo.getType())
                    .set("gender", ContestSignvo.getGender())
                    .set("status", 0);// 设置为审核中
            // 更新操作
            Boolean isOk = contestSignEntityService.update(updateWrapper);
            if (isOk) {
                ContestSignvo.setTeamConfig(userSignVoList);
                return ContestSignvo;
            } else {
                throw new StatusFailException("修改报名失败！");
            }
        }
    }

    @Transactional
    public ContestSignVO getSign(Long cid, String username)
            throws StatusFailException, StatusForbiddenException, AccessException {

        if (cid == null) {
            throw new StatusFailException("获取报名失败，请在比赛中获取报名");
        }
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("获取报名失败，该比赛已不存在！");
        }

        // 是否为超级管理员或者题目管理或者普通管理
        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");

        // 检查是否可以使用邀请功能
        checkInvent(contest, isRoot);

        // 未提交报名返回个人报名信息，提交报名的返回团队报名信息
        ContestSignVO ContestSignvo = getOwnTeam(cid, username, contest);

        return ContestSignvo;
    }

    /**
     * @MethodName getOwnTeam
     * @Description 获取队员所在的队伍
     */
    public ContestSignVO getOwnTeam(Long cid, String username, Contest contest)
            throws StatusFailException, StatusForbiddenException, AccessException {
        QueryWrapper<ContestSign> contestSignQueryWrapper = new QueryWrapper<>();
        contestSignQueryWrapper.eq("cid", cid).like("team_names", username);
        ContestSign contestSign = contestSignEntityService.getOne(contestSignQueryWrapper, false);

        ContestSignVO ContestSignvo = new ContestSignVO();

        if (contestSign != null) {

            List<String> teamNames = getTeamNames(contestSign.getTeamNames());
            // 比赛更改最大人数限制后的满足成员
            List<String> team_names = teamNames.subList(0,
                    Math.min(teamNames.size(), contest.getMaxParticipants()));

            List<UserSignVO> userSign = checkContestSignList(cid, team_names);
            ContestSignvo.setId(contestSign.getId());
            ContestSignvo.setParticipants(contest.getMaxParticipants());
            ContestSignvo.setTeamNames(String.join("$", team_names));
            ContestSignvo.setTeamConfig(userSign);

            // 存在报名队伍
            ContestSignvo.setCid(contestSign.getCid());
            ContestSignvo.setCname(contestSign.getCname());
            ContestSignvo.setEname(contestSign.getEname());
            ContestSignvo.setSchool(contestSign.getSchool());
            ContestSignvo.setGender(contestSign.getGender());
            ContestSignvo.setType(contestSign.getType());
            ContestSignvo.setStatus(contestSign.getStatus());
            ContestSignvo.setMsg(contestSign.getMsg());

            JSONObject configJson = new JSONObject();
            configJson.set("config", userSign);

            contestSign.setParticipants(contest.getMaxParticipants());
            contestSign.setTeamNames(String.join("$", team_names));
            contestSign.setTeamConfig(configJson.toString());

            // 更新报名表
            contestSignEntityService.updateById(contestSign);
        } else {
            // 不存在报名的队伍
            UserSign userSign = getUserSign(username);

            // 查询自己被邀请的状态
            List<UserInventStatusVO> inventStatusList = msgRemindEntityService.getUserInventedStatus(cid, null,
                    userSign.getUid());

            List<String> team_names;
            if (!CollectionUtils.isEmpty(inventStatusList)) // 自己被邀请过，且同意的队伍
            {
                String content = inventStatusList.get(0).getContent();
                if (content != null && inventStatusList.get(0).getContent().equals("true")) { // 找到发出者的邀请人
                    String topUsername = inventStatusList.get(0).getSenderUsername();
                    UserSign topUserSign = getUserSign(topUsername);
                    // 找到该邀请人的队伍
                    inventStatusList = msgRemindEntityService.getUserInventStatus(cid, topUserSign.getUid(), null);
                } else {
                    // 找自己邀请的队友组成的队伍
                    inventStatusList = msgRemindEntityService.getUserInventStatus(cid, userSign.getUid(), null);
                }
            } else {
                // 找自己邀请的队友组成的队伍
                inventStatusList = msgRemindEntityService.getUserInventStatus(cid, userSign.getUid(), null);
            }

            if (!CollectionUtils.isEmpty(inventStatusList)) { // 如果属于队伍
                String content = inventStatusList.get(0).getContent();

                // 状态排序
                inventStatusList.sort(Comparator.comparing(UserInventStatusVO::getContent,
                        Comparator.nullsLast(Comparator.reverseOrder())));

                // 取出所有的用户名
                team_names = inventStatusList.stream()
                        .map(UserInventStatusVO::getRecipientUsername)
                        .filter(Objects::nonNull) // 过滤掉为null的值
                        .collect(Collectors.toList());

                // 将队长插入
                team_names.add(0, (!StringUtils.isEmpty(content) && content.equals("true"))
                        ? inventStatusList.get(0).getSenderUsername()
                        : username);

            } else { // 不属于任何队伍，目前一个人
                team_names = Arrays.asList(username);
            }

            // 所有队员的报名信息
            List<UserSignVO> userSignList = checkContestSignList(cid, team_names);
            String teamNames = String.join("$", team_names);
            ContestSignvo.setCid(cid);
            ContestSignvo.setTeamNames(teamNames);
            ContestSignvo.setTeamConfig(userSignList);
            ContestSignvo.setParticipants(userSignList.size());
            ContestSignvo.setStatus(-1); // 设置为未报名
        }

        return ContestSignvo;
    }

    /**
     * @MethodName checkContestSignList
     * @Description 确定符合规范的队伍成员（队员不同名）
     */
    public List<UserSignVO> checkContestSignList(Long cid, List<String> team_names)
            throws StatusFailException {

        // // 判断用户是否重复
        // List<String> uniqueList = team_names.stream().distinct().collect(Collectors.toList());
        // if (team_names.size() != uniqueList.size()) {
        //     throw new StatusFailException("报名失败，队伍中有用户名重复的队员！");
        // }

        // 判断用户是否存在
        for (String username : team_names) {
            UserSign userSign = getUserSign(username);
            if (userSign == null) {
                throw new StatusFailException(username + " 用户不存在！");
            }
        }

        return team_names.stream()
                .map(username -> getUserSignVO(cid, team_names.get(0), username, team_names.indexOf(username) == 0))
                .collect(Collectors.toList());
    }

    /**
     * @MethodName sentInvent
     * @Description 比赛中，发送邀请信息
     */
    public void sentInvent(Long cid, UserSign userSign, UserSign toUserSign, String content, Comment comment) {

        QueryWrapper<MsgRemind> msgRemindQueryWrapper = new QueryWrapper<>();
        msgRemindQueryWrapper.select("id")
                .eq("source_id", String.valueOf(cid))
                .eq("sender_id", userSign.getUid())
                .eq("recipient_id", toUserSign.getUid())
                .eq("action", "Invent")
                .eq("source_type", "Invent");
        MsgRemind msgRemind = msgRemindEntityService.getOne(msgRemindQueryWrapper, false);

        if (msgRemind == null) {
            // 发送邀请消息
            commentEntityService.updateInventMsg(
                    toUserSign.getUid(),
                    userSign.getUid(),
                    cid.intValue(),
                    content,
                    comment.getId());
        } else {
            // 同时需要删除该评论的回复表数据
            replyEntityService.remove(new UpdateWrapper<Reply>()
                    .eq("content", "false")
                    .eq("comment_id", comment.getId()));

            // 更新邀请信息
            UpdateWrapper<MsgRemind> msgRemindUpdateWrapper = new UpdateWrapper<>();
            msgRemindUpdateWrapper.eq("id", msgRemind.getId())
                    .set("source_content", content)
                    .set("state", 0); // 更改为未读
            msgRemindEntityService.update(msgRemindUpdateWrapper);
        }
    }

    /**
     * @MethodName getUserSignVO
     * @Description 获取被邀请对应的报名信息VO
     */
    public UserSignVO getUserSignVO(Long cid, String username, String toUsername, Boolean isToper) {
        UserSignVO userSignVO = new UserSignVO();
        UserSign toUserSign = getUserSign(toUsername);

        Integer status = (!StringUtils.isEmpty(username))
                ? getInventedStatusBl(cid, getUserSign(username).getUid(), toUserSign.getUid())
                : getInventedStatusBl(cid, null, toUserSign.getUid());

        if (status == 1 || isToper) {
            BeanUtil.copyProperties(toUserSign, userSignVO);
            userSignVO.setStatus(1);
        } else {
            userSignVO.setStatus(status);
        }

        UserRolesVO userRolesVO = userRoleEntityService.getUserRoles(null, toUsername);
        userSignVO.setAvatar(userRolesVO.getAvatar());
        userSignVO.setUid(toUserSign.getUid());
        userSignVO.setUsername(toUsername);
        userSignVO.setGender(userRolesVO.getGender().equals("female") ? true : false);

        return userSignVO;
    }

    /**
     * @MethodName getUserSign
     * @Description 获取被邀请对应的报名信息
     */
    public UserSign getUserSign(String username) {
        // 得到发出者的邀请信息
        QueryWrapper<UserSign> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserSign userSign = userSignEntityService.getOne(queryWrapper, false);

        return userSign;
    }

    /**
     * @MethodName getInventedStatusBl
     * @Description 查询对应比赛,该用户接受邀请对应的处理状态
     * @return 1 表示已经被成功邀请，2表示拒绝邀请且还未加入队伍，-1表示邀请未处理
     */
    public Integer getInventedStatusBl(Long cid, String uid, String toUid) {
        List<UserInventStatusVO> inventStatusList = msgRemindEntityService.getUserInventedStatus(cid, uid, toUid);

        // 加入队伍
        if (!CollectionUtils.isEmpty(inventStatusList)) {

            String statusList = inventStatusList.get(0).getContent();

            // 如果全是空值，说明该用户接受邀请未处理
            if (StringUtils.isEmpty(statusList)) {
                return -1;
            }
            // 如果其中有true值，说明已经被邀请
            if ("true".equals(statusList)) {
                return 1;
            }
            // 如果其中有false值，说明该用户拒绝加入队伍，并且没有新的邀请
            if (inventStatusList.stream().anyMatch(statusVo -> "false".equals(statusVo.getContent()))) {
                return 2;
            }
        }
        return -1;
    }

    /**
     * @MethodName getInventStatusBl
     * @Description 查询对应比赛,该用户发送邀请对应的处理状态
     * @return 1 表示已经接受，2表示拒绝邀请，-1表示未处理
     */
    public Integer getInventStatusBl(Long cid, String uid, String toUid) {
        List<UserInventStatusVO> inventStatusList = msgRemindEntityService.getUserInventStatus(cid, uid, toUid);

        // 加入队伍
        if (!CollectionUtils.isEmpty(inventStatusList)) {

            String statusList = inventStatusList.get(0).getContent();
            // 如果全是空值，说明该用户接受邀请未处理
            if (StringUtils.isEmpty(statusList)) {
                return -1;
            }
            // 如果其中有true值，说明已经被邀请
            if ("true".equals(statusList)) {
                return 1;
            }
            // 如果其中有false值，说明该用户拒绝加入队伍，并且没有新的邀请
            if (inventStatusList.stream().anyMatch(statusVo -> "false".equals(statusVo.getContent()))) {
                return 2;
            }
        }
        return -1;
    }

    /**
     * @MethodName checkCnameOrEname
     * @Description 查询报名是否有重复的中文/英文名称
     */
    public CheckCnameOrEnameVO checkCnameOrEname(Long cid, String cname, String ename) {
        CheckCnameOrEnameVO cnameOrEnameVo = new CheckCnameOrEnameVO();

        boolean rightCname = false;
        boolean rightEname = false;

        if (!StringUtils.isEmpty(cname)) {
            cname = cname.trim();
            QueryWrapper<ContestSign> wrapper = new QueryWrapper<ContestSign>().eq("cname", cname).eq("cid", cid);
            ContestSign contestSign = contestSignEntityService.getOne(wrapper, false);
            if (contestSign != null) {
                rightCname = true;
            } else {
                rightCname = false;
            }
        }

        if (!StringUtils.isEmpty(ename)) {
            ename = ename.trim();
            QueryWrapper<ContestSign> wrapper = new QueryWrapper<ContestSign>().eq("ename", ename).eq("cid", cid);
            ContestSign contestSign = contestSignEntityService.getOne(wrapper, false);
            if (contestSign != null) {
                rightEname = true;
            } else {
                rightEname = false;
            }
        }

        cnameOrEnameVo.setCname(rightCname);
        cnameOrEnameVo.setEname(rightEname);
        return cnameOrEnameVo;
    }

    public void checkInvent(Contest contest, Boolean isRoot) throws StatusForbiddenException {
        // 检查比赛是否结束
        if (!isRoot && contest.getStatus().intValue() == Constants.Contest.STATUS_ENDED.getCode()) {
            throw new StatusForbiddenException("超过报名时间，您无权访问该比赛！");
        }
    }

    /**
     * @MethodName removeTeamContestResigter
     * @Description 回收团队进入比赛权限
     */
    public void removeTeamContestResigter(Long cid, List<String> team_names) {
        if (!CollectionUtils.isEmpty(team_names)) {
            String username = team_names.get(0);
            UserSign userSign = getUserSign(username);
            removeContestResigter(cid, username);
            for (int i = 1; i < team_names.size(); i++) {
                String toUsername = team_names.get(i);
                UserSign toUserSign = getUserSign(toUsername);

                if (getInventedStatusBl(cid, userSign.getUid(), toUserSign.getUid()) == 1) {
                    removeContestResigter(cid, toUsername);
                }
            }
        }
    }

    /**
     * @MethodName removeContestResigter
     * @Description 回收用户进入比赛权限（当被邀请的队伍处于审核阶段，或者队伍解散）
     */
    public void removeContestResigter(Long cid, String username) {

        UserSign userSign = getUserSign(username);
        if (userSign != null) {
            QueryWrapper<ContestRegister> contestRegisterwrapper = new QueryWrapper<>();
            contestRegisterwrapper.eq("uid", userSign.getUid())
                    .eq("cid", cid);

            ContestRegister contestRegister = contestRegisterEntityService
                    .getOne(contestRegisterwrapper, false);

            // 如果存在进入比赛权限
            if (contestRegister != null) {
                contestRegisterEntityService.remove(contestRegisterwrapper);
            }
        }
    }

    public List<String> getTeamNames(String teamNames) {
        // 获取报名表中的，报名的人员
        List<String> team_names = Arrays.stream(teamNames.split("\\$"))
                .collect(Collectors.toList());
        return team_names;
    }

    public List<UserSignVO> getTeamConfig(ContestSign contestSign) {
        JSONObject jsonObject = JSONUtil.parseObj(contestSign.getTeamConfig());
        List<JSONObject> configList = jsonObject.get("config", List.class);

        List<UserSignVO> userSignvo = new ArrayList();
        for (JSONObject object : configList) {
            UserSignVO UserSignvo = JSONUtil.toBean(object, UserSignVO.class);
            userSignvo.add(UserSignvo);
        }
        return userSignvo;
    }
}