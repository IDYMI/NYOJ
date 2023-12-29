package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @Description:
 */
@Data
@Accessors(chain = true)
public class MossContentDTO {
    private String username1;
    private String username2;
    private String href;
    private Long percent1;
    private Long percent2;
    private Long length;

    // 构造函数，用于初始化 MossContentDTO 对象
    public MossContentDTO(String username1, Long percent1, String username2, Long percent2, String href, Long length) {
        this.username1 = username1;
        this.percent1 = percent1;
        this.username2 = username2;
        this.percent2 = percent2;
        this.href = href;
        this.length = length;
    }

}