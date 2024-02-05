package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @Date: 2022/3/11 10:55
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ProblemCaseDTO {

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "测试样例的输入")
    private String input;

    @ApiModelProperty(value = "测试样例的输出")
    private String output;

    @ApiModelProperty(value = "文件名")
    private String name;
}