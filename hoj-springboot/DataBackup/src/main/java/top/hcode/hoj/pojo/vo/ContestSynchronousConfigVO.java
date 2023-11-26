package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(value = "比赛同步赛设置", description = "")
@Data
@Accessors(chain = true)
public class ContestSynchronousConfigVO {

    @ApiModelProperty(value = "同步赛学校")
    private String school;

    @ApiModelProperty(value = "同步赛链接")
    private String link;

    @ApiModelProperty(value = "同步赛的 username")
    private String username;

    @ApiModelProperty(value = "同步赛的 password")
    private String password;
}
