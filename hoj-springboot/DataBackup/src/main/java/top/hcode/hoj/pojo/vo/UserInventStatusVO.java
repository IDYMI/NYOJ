package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Description:
 */
@ApiModel(value = "改用户的被邀请信息VO", description = "")
@Data
public class UserInventStatusVO {

    @ApiModelProperty(value = "邀请的源comment id")
    private Long commentId;

    @ApiModelProperty(value = "邀请发出者的用户名")
    private String senderUsername;

    @ApiModelProperty(value = "被邀请的用户名")
    private String recipientUsername;

    @ApiModelProperty(value = "该用户的处理历史")
    private String content;
}