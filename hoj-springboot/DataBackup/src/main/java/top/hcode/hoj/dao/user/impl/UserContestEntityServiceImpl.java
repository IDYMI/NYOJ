package top.hcode.hoj.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.hcode.hoj.mapper.ContestRecordMapper;
import top.hcode.hoj.mapper.UserContestMapper;
import top.hcode.hoj.dao.user.UserContestEntityService;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class UserContestEntityServiceImpl extends ServiceImpl<UserContestMapper, ContestRecord>
                implements UserContestEntityService {

}
