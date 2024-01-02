package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(value = "比赛同步赛设置", description = "")
@Data
@Accessors(chain = true)
public class ContestFileConfigVO {

    private Long id;

    @ApiModelProperty(value = "下载地址")
    private String url;

    @ApiModelProperty(value = "显示名称")
    private String hint;

}
