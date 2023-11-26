package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @Date: 2022/3/11 16:48
 * @Description:
 */
@Data
public class ContestSignVO {

    private Long id;
    
    private Long cid;

    @ApiModelProperty(value = "队伍中文名称")
    private String cname;

    @ApiModelProperty(value = "队伍英文名称")
    private String ename;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "队员用户id")
    private String teamNames;

    @ApiModelProperty(value = "队伍信息")
    private List<UserSignVO> teamConfig;

    @ApiModelProperty(value = "队伍人数")
    private Integer participants;

    @ApiModelProperty(value = "报名类型（0为正式名额，1为打星名额）")
    private Boolean type;

    @ApiModelProperty(value = "报名类型（0为正式队伍，1为女生队伍）")
    private Boolean gender;

    @ApiModelProperty(value = "审核不通过原因")
    private String msg;

    @ApiModelProperty(value = "报名审核状态（-1表示未报名，0表示审核中，1为审核通过，2为审核不通过。）")
    private Integer status;

}
