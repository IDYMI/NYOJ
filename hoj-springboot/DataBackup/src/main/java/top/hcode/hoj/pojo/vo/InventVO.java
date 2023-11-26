package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 *
 * @Date: 2021/10/2 20:50
 * @Description:
 */
@ApiModel(value = "用户是否接受邀请VO", description = "")
@Data
public class InventVO {

    private String username;

    private Boolean isAccept;

    private UserMsgVO userMsg;

}