package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.UserLevelRules;
import cc.mrbird.febs.cos.service.IUserLevelRulesService;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/user-level-rules")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserLevelRulesController {

    private final IUserLevelRulesService userLevelRulesService;

    /**
     * 分页查询用户等级规则信息
     *
     * @param page
     * @param userLevelRules
     * @return
     */
    @GetMapping("/page")
    public R page(Page page, UserLevelRules userLevelRules) {
        return R.ok(userLevelRulesService.page(page, Wrappers.<UserLevelRules>lambdaQuery()));
    }

    @GetMapping("/list")
    public R list() {
        return R.ok(userLevelRulesService.list());
    }

    /**
     * 新增用户等级规则信息
     *
     * @param userLevelRules
     * @return
     */
    @PostMapping
    public R save(UserLevelRules userLevelRules) {
        return R.ok(userLevelRulesService.save(userLevelRules));
    }

    /**
     * 修改用户等级规则信息
     *
     * @param userLevelRules
     * @return
     */
    @PutMapping
    public R edit(UserLevelRules userLevelRules) {
        return R.ok(userLevelRulesService.updateById(userLevelRules));
    }

    /**
     * 删除用户等级规则信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(userLevelRulesService.removeByIds(ids));
    }
}
