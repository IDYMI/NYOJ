package top.hcode.hoj.pojo.dto;

import lombok.Data;

/**
 *
 * @Date: 2022/3/11 18:05
 * @Description:
 */
@Data
public class ChangeEmailDTO {

    private String password;

    private String newEmail;

    private String code;
}