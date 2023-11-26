package top.hcode.hoj.dao.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.MsgRemindMapper;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.UserInventStatusVO;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVO;
import top.hcode.hoj.dao.msg.MsgRemindEntityService;

import java.util.List;

import javax.annotation.Resource;

/**
 *
 * @Date: 2021/10/1 20:36
 * @Description:
 */
@Service
public class MsgRemindEntityServiceImpl extends ServiceImpl<MsgRemindMapper, MsgRemind>
        implements MsgRemindEntityService {

    @Resource
    private MsgRemindMapper msgRemindMapper;

    @Override
    public UserUnreadMsgCountVO getUserUnreadMsgCount(String uid) {
        return msgRemindMapper.getUserUnreadMsgCount(uid);
    }

    @Override
    public IPage<UserMsgVO> getUserMsg(Page<UserMsgVO> page, String uid, String action) {
        return msgRemindMapper.getUserMsg(page, uid, action);
    }

    @Override
    public List<UserInventStatusVO> getUserInventedStatus(Long cid, String uid, String toUid) {
        return msgRemindMapper.getUserInventedStatus(cid, uid, toUid);
    }

    @Override
    public List<UserInventStatusVO> getUserInventStatus(Long cid, String uid, String toUid) {
        return msgRemindMapper.getUserInventStatus(cid, uid, toUid);
    }
}