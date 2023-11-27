package top.hcode.hoj.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserPreferences对象", description = "")
public class UserPreferences implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableId(value = "uid", type = IdType.UUID)
    private String uid;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "界面语言")
    private String uiLanguage;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "界面风格")
    private String uiTheme;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "代码语言")
    private String codeLanguage;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "字体大小")
    private String codeSize;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "编译器主题")
    private String ideTheme;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "个人代码模板")
    private String codeTemplate;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
