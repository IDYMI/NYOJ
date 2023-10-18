package top.hcode.hoj.pojo.dto;

import lombok.Data;

/**
 *
 * @Date: 2022/3/11 17:32
 * @Description:
 */

@Data
public class ResetPasswordDTO {

    private String username;

    private String password;

    private String code;
}