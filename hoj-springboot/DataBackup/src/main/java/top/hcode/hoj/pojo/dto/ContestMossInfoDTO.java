package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ContestMossInfoDTO {

    public ContestMossInfoDTO(String displayId, String title, Long submitId, Integer score, String code) {
        this.displayId = displayId;
        this.title = title;
        this.submitId = submitId;
        this.score = score;
        this.code = code;
    }

    @ApiModelProperty(value = "题目展示id")
    private String displayId;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "提交id")
    private Long submitId;

    @ApiModelProperty(value = "分数")
    private Integer score;

    @ApiModelProperty(value = "代码")
    private String code;

}