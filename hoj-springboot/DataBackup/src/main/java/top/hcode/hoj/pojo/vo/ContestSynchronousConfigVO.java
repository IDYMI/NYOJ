package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "比赛同步赛设置", description = "")
@Data
public class ContestSynchronousConfigVO {

    @ApiModelProperty(value = "同步赛学校")
    private String school;

    @ApiModelProperty(value = "同步赛链接")
    private String link;

    @ApiModelProperty(value = "同步赛的 authorization 值")
    private String authorization;
}
