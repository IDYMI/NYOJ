package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.contest.ContestSign;

/**
 *
 * @Date: 2021/9/19 21:04
 * @Description:
 */
@Mapper
@Repository
public interface ContestSignMapper extends BaseMapper<ContestSign> {
}
