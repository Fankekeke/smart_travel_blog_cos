package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.dao.PostInfoMapper;
import cc.mrbird.febs.cos.service.*;
import cc.mrbird.febs.system.domain.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostInfoServiceImpl extends ServiceImpl<PostInfoMapper, PostInfo> implements IPostInfoService {

    private final IUserInfoService userInfoService;

    private final IUserLevelRulesService userLevelRulesService;

    private final ICollectInfoService collectInfoService;

    private final IFocusInfoService focusInfoService;


    @Scheduled(cron = "0 0/5 * * * ?")
    public void taskUserLevel() {
        log.info("开始执行用户等级计算任务");
        try {
            List<UserInfo> userInfoList = userInfoService.list();
            List<CollectInfo> collectInfoList = collectInfoService.list(Wrappers.<CollectInfo>lambdaQuery().eq(CollectInfo::getDeleteFlag, 0));
            List<FocusInfo> focusInfoList = focusInfoService.list(Wrappers.<FocusInfo>lambdaQuery().eq(FocusInfo::getDeleteFlag, 0));
            List<PostInfo> postInfoList = list(Wrappers.<PostInfo>lambdaQuery().eq(PostInfo::getDeleteFlag, 0));

            List<UserLevelRules> userLevelRules = userLevelRulesService.list();

            if (userLevelRules == null || userLevelRules.isEmpty()) {
                log.warn("用户等级规则为空，跳过等级计算");
                return;
            }

            Map<Long, Integer> fansCountMap = focusInfoList.stream()
                    .collect(Collectors.groupingBy(
                            FocusInfo::getCollectUserId,
                            Collectors.summingInt(e -> 1)
                    ));

            Map<Long, Integer> viewsCountMap = postInfoList.stream()
                    .filter(post -> post.getUserId() != null)
                    .collect(Collectors.groupingBy(
                            PostInfo::getUserId,
                            Collectors.summingInt(post -> post.getPageviews() != null ? post.getPageviews() : 0)
                    ));

            Map<Integer, Long> postCollectCountMap = collectInfoList.stream()
                    .collect(Collectors.groupingBy(
                            CollectInfo::getPostId,
                            Collectors.counting()
                    ));

            Map<Long, Integer> collectsCountMap = postInfoList.stream()
                    .filter(post -> post.getId() != null && post.getUserId() != null)
                    .collect(Collectors.groupingBy(
                            PostInfo::getUserId,
                            Collectors.summingInt(post -> {
                                Long count = postCollectCountMap.get(post.getId());
                                return count != null ? count.intValue() : 0;
                            })
                    ));

            for (UserInfo userInfo : userInfoList) {
                Long userId = userInfo.getUserId() != null ? userInfo.getUserId().longValue() : null;
                if (userId == null) {
                    continue;
                }

                int fansCount = fansCountMap.getOrDefault(userId, 0);
                int viewsCount = viewsCountMap.getOrDefault(userId, 0);
                int collectsCount = collectsCountMap.getOrDefault(userId, 0);

                String matchedLevel = matchUserLevel(userLevelRules, fansCount, viewsCount, collectsCount);

                if (!StringUtils.equals(userInfo.getUserLevel(), matchedLevel)) {
                    userInfo.setUserLevel(matchedLevel);
                    userInfoService.updateById(userInfo);
                    log.info("用户 {} (ID:{}) 等级更新为: {}, 粉丝数: {}, 浏览量: {}, 收藏数: {}",
                            userInfo.getName(), userId, matchedLevel, fansCount, viewsCount, collectsCount);
                }
            }

            log.info("用户等级计算任务执行完成，共处理 {} 个用户", userInfoList.size());
        } catch (Exception e) {
            log.error("用户等级计算任务执行失败", e);
        }
    }

    private String matchUserLevel(List<UserLevelRules> rules, int fansCount, int viewsCount, int collectsCount) {
        String matchedLevel = null;
        int maxScore = -1;

        for (UserLevelRules rule : rules) {
            int minFans = rule.getMinFans() != null ? rule.getMinFans() : 0;
            int minViews = rule.getMinViews() != null ? rule.getMinViews() : 0;
            int minCollects = rule.getMinCollects() != null ? rule.getMinCollects() : 0;

            if (fansCount >= minFans && viewsCount >= minViews && collectsCount >= minCollects) {
                int score = minFans + minViews + minCollects;
                if (score > maxScore) {
                    maxScore = score;
                    matchedLevel = rule.getLevelName();
                }
            }
        }

        return matchedLevel != null ? matchedLevel : "小白用户";
    }

    /**
     * 分页获取系统用户信息
     * @param page
     * @param user
     * @return
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> selectUserPage(Page page, User user) {
        return baseMapper.selectUserPage(page, user);
    }

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> queryUserDetail(Integer userId) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("user", userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId)));
                put("post", baseMapper.queryPostByUser(userId));
            }
        };
        return result;
    }

    @Override
    public IPage<LinkedHashMap<String, Object>> postInfoByPage(Page page, PostInfo postInfo) {
        return baseMapper.postInfoByPage(page, postInfo);
    }

    @Override
    public List<LinkedHashMap<String, Object>> getPostByTag(Integer tagId) {
        return baseMapper.getPostByTag(tagId);
    }

    // 获取模块下的贴子
    @Override
    public List<LinkedHashMap<String, Object>> getPostByTagUser(Integer tagId, Integer userId) {
        return baseMapper.getPostByTagUser(tagId, userId);
    }

    @Override
    public LinkedHashMap<String, Object> postDetail(Integer postId) {
        return baseMapper.postDetail(postId);
    }

    @Override
    public List<LinkedHashMap<String, Object>> postByKey(String key) {
        return baseMapper.postByKey(key);
    }

    // 搜索
    @Override
    public List<LinkedHashMap<String, Object>> querySearch(String key, String name, String vipFlag) {
        return baseMapper.querySearch(key, name, vipFlag);
    }

    // 模糊查询帖子信息
    @Override
    public List<LinkedHashMap<String, Object>> listByUser(String key, Integer userId) {
        return baseMapper.listByUser(key, userId);
    }

    @Override
    public List<LinkedHashMap<String, Object>> recommend(Integer tagId, List<Long> collectUserIds) {
        return baseMapper.recommend(tagId, StringUtils.join(collectUserIds.toArray(), ","));
    }
}
