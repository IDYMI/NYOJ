package top.hcode.hoj.pojo.dto;

import lombok.Data;

/**
 *
 * @Date: 2022/3/11 17:26
 * @Description:
 */
@Data
public class ApplyResetPasswordDTO {

    private String captcha;

    private String captchaKey;

    private String email;
}