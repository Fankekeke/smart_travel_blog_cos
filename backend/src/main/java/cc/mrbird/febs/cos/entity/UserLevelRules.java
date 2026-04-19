package cc.mrbird.febs.cos.entity;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户等级规则配置表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserLevelRules implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 最低粉丝数要求
     */
    private Integer minFans;

    /**
     * 最低帖子总浏览量要求
     */
    private Integer minViews;

    /**
     * 最低帖子总收藏数要求
     */
    private Integer minCollects;

    private LocalDateTime createdAt;


}
