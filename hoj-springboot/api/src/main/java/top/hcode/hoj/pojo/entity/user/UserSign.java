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
@ApiModel(value = "UserSign对象", description = "")
public class UserSign implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableId(value = "uid", type = IdType.UUID)
    private String uid;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "用户名")
    private String username;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "学校")
    private String school;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "专业/班级")
    private String course;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "学号")
    private String number;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "衣服尺寸")
    private String clothesSize;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "联系方式")
    private String phoneNumber;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
