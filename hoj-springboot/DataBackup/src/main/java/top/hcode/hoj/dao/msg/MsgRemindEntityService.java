package top.hcode.hoj.dao.msg;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.UserInventStatusVO;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVO;

/**
 *
 * @Date: 2021/10/1 20:32
 * @Description:
 */
public interface MsgRemindEntityService extends IService<MsgRemind> {

    UserUnreadMsgCountVO getUserUnreadMsgCount(String uid);

    IPage<UserMsgVO> getUserMsg(Page<UserMsgVO> page, String uid, String action);

    List<UserInventStatusVO> getUserInventedStatus(Long cid, String uid, String toUid);

    List<UserInventStatusVO> getUserInventStatus(Long cid, String uid, String toUid);

}