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
public class UserInfoVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "头衔名称")
    private String titleName;

    @ApiModelProperty(value = "头衔背景颜色")
    private String titleColor;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "专业")
    private String course;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "github地址")
    private String github;

    @ApiModelProperty(value = "博客地址")
    private String blog;

    @ApiModelProperty(value = "cf的username")
    private String cfUsername;

    @ApiModelProperty(value = "界面语言")
    private String uiLanguage;

    @ApiModelProperty(value = "界面风格")
    private String uiTheme;

    @ApiModelProperty(value = "代码语言")
    private String codeLanguage;

    @ApiModelProperty(value = "字体大小")
    private String codeSize;

    @ApiModelProperty(value = "编译器主题")
    private String ideTheme;

    @ApiModelProperty(value = "个人代码模板")
    private String codeTemplate;

    @ApiModelProperty(value = "衣服尺寸")
    private String clothesSize;

    @ApiModelProperty(value = "联系方式")
    private String phoneNumber;

    @ApiModelProperty(value = "角色列表")
    private List<String> roleList;

}