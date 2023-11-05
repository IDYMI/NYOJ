package top.hcode.hoj.pojo.dto;

import lombok.Data;

@Data
public class ContestStatisticDTO {
    /***
     * @param cids    比赛cids
     * @param keyword 搜索关键词：学校或榜单显示名称
     */
    private String cids;

    private Integer limit;

    private Integer currentPage;

    private String keyword;

}
