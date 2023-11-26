package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(value = "比赛报名信息", description = "")
@Data
@Accessors(chain = true)
public class ContestSignConfigVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "专业/班级")
    private String course;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "衣服尺寸")
    private String clothesSize;

    @ApiModelProperty(value = "联系方式")
    private String phoneNumber;
}
