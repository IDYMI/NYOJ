package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @Description:
 */
@Data
@Accessors(chain = true)
public class MossResultContentDTO {

    private String href;
    private String displayId1;
    private String title1;
    private String col1;
    private String href1;
    private String icon1;
    private Long submitId1;
    private String code1;
    private String displayId2;
    private String title2;
    private String col2;
    private String href2;
    private String icon2;
    private Long submitId2;
    private String code2;

    public MossResultContentDTO(
            String href,
            String displayId1, String title1, String col1, String href1, String icon1, Long submitId1, String code1,
            String displayId2, String title2, String col2, String href2, String icon2, Long submitId2, String code2) {

        this.href = href;
        this.displayId1 = displayId1;
        this.title1 = title1;
        this.col1 = col1;
        this.href1 = href1;
        this.icon1 = icon1;
        this.submitId1 = submitId1;
        this.code1 = code1;
        this.displayId2 = displayId2;
        this.title2 = title2;
        this.col2 = col2;
        this.href2 = href2;
        this.icon2 = icon2;
        this.submitId2 = submitId2;
        this.code2 = code2;
    }

}