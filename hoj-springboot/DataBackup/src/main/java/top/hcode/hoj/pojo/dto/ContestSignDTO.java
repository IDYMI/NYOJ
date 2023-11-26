package top.hcode.hoj.pojo.dto;

import lombok.Data;
import top.hcode.hoj.pojo.vo.ContestSignConfigVO;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 *
 * @Date: 2021/9/20 13:00
 * @Description:
 */
@Data
public class ContestSignDTO {

    @NotBlank(message = "比赛id不能为空")
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
    private List<ContestSignConfigVO> teamConfig;

    @ApiModelProperty(value = "队伍人数")
    private Integer participants;

    @ApiModelProperty(value = "报名类型（0为正式名额，1为打星名额）")
    private Boolean type;
}