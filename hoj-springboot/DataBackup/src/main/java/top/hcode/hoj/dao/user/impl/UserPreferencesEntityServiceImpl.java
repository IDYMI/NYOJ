package top.hcode.hoj.dao.user.impl;

import top.hcode.hoj.pojo.entity.user.UserPreferences;
import top.hcode.hoj.mapper.UserPreferencesMapper;
import top.hcode.hoj.dao.user.UserPreferencesEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class UserPreferencesEntityServiceImpl extends ServiceImpl<UserPreferencesMapper, UserPreferences>
        implements UserPreferencesEntityService {

}
