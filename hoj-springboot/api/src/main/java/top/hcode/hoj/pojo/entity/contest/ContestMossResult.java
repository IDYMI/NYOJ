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
@ApiModel(value = "ContestMossResult对象", description = "")
public class ContestMossResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long cid;

    @ApiModelProperty(value = "查重详情页")
    private String href;

    @ApiModelProperty(value = "重复片段行数位置列表")
    private String col1;

    @ApiModelProperty(value = "重复率按键列表")
    private String icon1;

    @ApiModelProperty(value = "代码")
    private String code1;

    @ApiModelProperty(value = "重复片段行数位置列表")
    private String col2;

    @ApiModelProperty(value = "重复率按键列表")
    private String icon2;

    @ApiModelProperty(value = "代码")
    private String code2;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
