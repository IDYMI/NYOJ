package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Description:
 */
@Data
public class ContestMossImportVO {

    @ApiModelProperty(value = "提交Moss的名称")
    private String filename;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "语言")
    private String language;

    public ContestMossImportVO(String filename, String code, String language) {
        this.filename = filename;
        this.code = code;
        this.language = language;
    }
}
