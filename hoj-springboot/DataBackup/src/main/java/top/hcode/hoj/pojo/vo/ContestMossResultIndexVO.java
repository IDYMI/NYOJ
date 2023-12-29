package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Description:
 */
@Data
public class ContestMossResultIndexVO {

    @ApiModelProperty(value = "重复片段行数位置列表")
    private String col1;

    @ApiModelProperty(value = "重复率按键列表")
    private String icon1;

    @ApiModelProperty(value = "重复片段行数位置列表")
    private String col2;

    @ApiModelProperty(value = "重复率按键列表")
    private String icon2;

    public ContestMossResultIndexVO(String col1, String icon1, String col2, String icon2) {
        this.col1 = col1;
        this.icon1 = icon1;
        this.col2 = col2;
        this.icon2 = icon2;
    }
}
