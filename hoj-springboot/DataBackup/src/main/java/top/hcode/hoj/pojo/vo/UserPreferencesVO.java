package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Date: 2022/3/11 16:48
 * @Description:
 */
@Data
public class UserPreferencesVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

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

}