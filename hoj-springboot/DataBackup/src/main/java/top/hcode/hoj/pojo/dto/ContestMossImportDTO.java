package top.hcode.hoj.pojo.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;


/**
 *
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ContestMossImportDTO {

    @NotBlank(message = "比赛id不能为空")
    private Long cid;

    private List<String> modeList;

    private Boolean excludeAdmin;
}
