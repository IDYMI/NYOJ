package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 *
 * @Date: 2021/1/18 14:55
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ACMStatisticContestVO {

    @ApiModelProperty(value = "排名,排名为-1则为打星队伍")
    private Integer rank;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realname;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "提交总罚时")
    private Long totalTime;

    @ApiModelProperty(value = "总提交数")
    private Integer total;

    @ApiModelProperty(value = "ac题目数")
    private Integer ac;

    @ApiModelProperty(value = "每场比赛对应的AC提交详情")
    private HashMap<String, HashMap<String, Object>>contestInfo;

}