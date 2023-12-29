package top.hcode.hoj.pojo.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @Description:
 */
@Data
public class ContestMossResultVO {

    private Long id;

    private Long cid;

    private String username1;

    private Long percent1;

    private String username2;

    private Long percent2;

    @ApiModelProperty(value = "查重详情页")
    private String href;

    @ApiModelProperty(value = "代码")
    private String code1;

    @ApiModelProperty(value = "代码")
    private String code2;

    @ApiModelProperty(value = "引索列表")
    private List<ContestMossResultIndexVO> indexList;
}
