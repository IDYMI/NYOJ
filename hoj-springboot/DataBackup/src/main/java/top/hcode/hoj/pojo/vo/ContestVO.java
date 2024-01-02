package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @Date: 2020/10/27 21:53
 * @Description:
 */
@ApiModel(value = "比赛信息", description = "")
@Data
public class ContestVO implements Serializable {

    @TableId(value = "比赛id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "创建者用户名")
    private String author;

    @ApiModelProperty(value = "比赛标题")
    private String title;

    @ApiModelProperty(value = "0为acm赛制，1为比分赛制")
    private Integer type;

    @ApiModelProperty(value = "比赛说明")
    private String description;

    @ApiModelProperty(value = "-1为未开始，0为进行中，1为已结束")
    private Integer status;

    @ApiModelProperty(value = "比赛来源，原创为0，克隆赛为比赛id")
    private Integer source;

    @ApiModelProperty(value = "0为公开赛，1为私有赛（访问有密码），2为保护赛（提交有密码），3为正式赛（访问要报名）， 4为同步公开赛， 5为同步私有赛（访问有密码）")
    private Integer auth;

    @ApiModelProperty("当前服务器系统时间，为了前端统一时间")
    private Date now;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "比赛时长（秒）")
    private Integer duration;

    @ApiModelProperty(value = "是否开启封榜")
    private Boolean sealRank;

    @ApiModelProperty(value = "是否打开打印功能")
    private Boolean openPrint;

    @ApiModelProperty(value = "封榜起始时间，一直到比赛结束，不刷新榜单")
    private Date sealRankTime;

    @ApiModelProperty(value = "排行榜显示（username、nickname、realname）")
    private String rankShowName;

    @ApiModelProperty(value = "是否开放比赛榜单")
    private Boolean openRank;

    @ApiModelProperty(value = "oi排行榜得分方式，Recent、Highest（最近一次提交、最高得分提交）")
    private String oiRankScoreType;

    @ApiModelProperty(value = "比赛的报名人数")
    private Integer count;

    @ApiModelProperty(value = "团队ID")
    private Long gid;

    @ApiModelProperty(value = "是否允许比赛结束后继续交题")
    private Boolean allowEndSubmit;

    @ApiModelProperty(value = "是否开启同步赛")
    private Boolean synchronous;

    @ApiModelProperty(value = "报名开始时间")
    private Date signStartTime;

    @ApiModelProperty(value = "报名结束时间")
    private Date signEndTime;

    @ApiModelProperty(value = "报名时长（s）")
    private Long signDuration;

    @ApiModelProperty(value = "队员上限(最大为3)")
    private Integer maxParticipants;

    @ApiModelProperty(value = "是否开启文件柜")
    private Boolean openFile;

}