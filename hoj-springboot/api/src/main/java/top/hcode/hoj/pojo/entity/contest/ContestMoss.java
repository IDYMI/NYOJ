package top.hcode.hoj.pojo.entity.contest;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ContestMoss对象", description = "")
public class ContestMoss implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long cid;

    @ApiModelProperty(value = "查重预览页")
    private String html;

    @ApiModelProperty(value = "用户名")
    private String username1;

    private String uid1;

    @ApiModelProperty(value = "重复片段占代码总长度百分比")
    private Long percent1;

    @ApiModelProperty(value = "用户名")
    private String username2;

    private String uid2;

    @ApiModelProperty(value = "重复片段占代码总长度百分比")
    private Long percent2;

    @ApiModelProperty(value = "重复片段长度")
    private Long length;

    @ApiModelProperty(value = "查重详情页")
    private String href;

    @ApiModelProperty(value = "查重语言")
    private String language;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
