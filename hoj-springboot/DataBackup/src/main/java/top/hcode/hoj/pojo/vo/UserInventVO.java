package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Date: 2021/5/5 22:30
 * @Description:
 */
@ApiModel(value = "被邀请人VO", description = "")
@Data
public class UserInventVO {

    @ApiModelProperty(value = "邀请id")
    private Integer id;

    @ApiModelProperty(value = "比赛的id")
    private Long cid;

    @ApiModelProperty(value = "邀请人的id")
    private String username;

    @ApiModelProperty(value = "被邀请人的id")
    private String toUsername;

    @ApiModelProperty(value = "被邀请人的信息")
    private UserSignVO userSignVO;

}