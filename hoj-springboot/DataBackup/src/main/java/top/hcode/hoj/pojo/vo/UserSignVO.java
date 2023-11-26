package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Date: 2022/3/11 16:48
 * @Description:
 */
@Data
public class UserSignVO {

    private Long id;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "头像")
    private String avatar;

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

    @ApiModelProperty(value = "性别")
    private Boolean gender;

    @ApiModelProperty(value = "1 表示已经被成功邀请，2表示拒绝邀请且还未加入队伍，-1表示没有邀请")
    private Integer status;
}