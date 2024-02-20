package top.hcode.hoj.manager.admin.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.pojo.dto.AnnouncementDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestAnnouncement;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.dao.common.AnnouncementEntityService;
import top.hcode.hoj.dao.contest.ContestAnnouncementEntityService;
import top.hcode.hoj.dao.contest.ContestEntityService;

/**
 *
 * @Date: 2022/3/9 11:19
 * @Description:
 */
@Component
public class AdminContestAnnouncementManager {

    @Autowired
    private AnnouncementEntityService announcementEntityService;

    @Autowired
    private ContestAnnouncementEntityService contestAnnouncementEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    public IPage<AnnouncementVO> getAnnouncementList(Integer limit, Integer currentPage, Long cid)
            throws StatusForbiddenException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        // 获取本场比赛的信息
        Contest contest = contestEntityService.getById(cid);
        // 只有超级管理员和题目管理员、题目创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(contest.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限查看公告！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        return announcementEntityService.getContestAnnouncement(cid, false, limit, currentPage, null);
    }

    public void deleteAnnouncement(Long aid) throws StatusFailException {
        boolean isOk = announcementEntityService.removeById(aid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addAnnouncement(AnnouncementDTO announcementDto) throws StatusFailException {
        boolean saveAnnouncement = announcementEntityService.save(announcementDto.getAnnouncement());
        boolean saveContestAnnouncement = contestAnnouncementEntityService.saveOrUpdate(new ContestAnnouncement()
                .setAid(announcementDto.getAnnouncement().getId())
                .setCid(announcementDto.getCid()));
        if (!saveAnnouncement || !saveContestAnnouncement) {
            throw new StatusFailException("添加失败");
        }
    }

    public void updateAnnouncement(AnnouncementDTO announcementDto) throws StatusFailException {
        boolean isOk = announcementEntityService.saveOrUpdate(announcementDto.getAnnouncement());
        if (!isOk) { // 删除成功
            throw new StatusFailException("更新失败！");
        }
    }
}