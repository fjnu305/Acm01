package org.fjnu305.acm01.module.contest.crawler;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;

import java.util.List;

/**
 * 赛事爬虫策略接口（Strategy Pattern）。
 * <p>
 * <b>设计意图：</b>7 大 OJ / 竞赛平台的数据格式各不相同（JSON API、HTML 页面、字段命名各异），
 * 若写在一个类里会导致代码臃肿且难以维护。每个平台独立实现本接口，
 * 由 {@link org.fjnu305.acm01.module.contest.service.ContestCrawlService} 统一调度，
 * 实现「开闭原则」：新增平台只需新增实现类，不修改编排逻辑。
 * </p>
 *
 * <pre>
 * 调用关系：
 *   ContestCrawlService
 *       └── for (ContestCrawler crawler : crawlers)
 *               └── crawler.fetchContests()  → List&lt;ContestDTO&gt;
 * </pre>
 *
 * @see AbstractContestCrawler 推荐继承抽象基类，复用模板方法与公共工具
 * @see org.fjnu305.acm01.module.contest.crawler.strategy.CodeforcesCrawler 迭代 1 优先实现
 */
public interface ContestCrawler {

    /**
     * 返回本平台对应的来源标识。
     * <p>与 {@code contest.source} 字段及 {@link ContestSource} 枚举保持一致。</p>
     *
     * @return 平台枚举，如 {@link ContestSource#CODEFORCES}
     */
    ContestSource getSource();

    /**
     * 拉取并解析赛事数据，返回归一化后的 DTO 列表。
     * <p>
     * 实现类职责：
     * <ol>
     *   <li>发起 HTTP 请求或调用官方 API</li>
     *   <li>解析 JSON / HTML 为 {@link ContestDTO}</li>
     *   <li>计算 {@code rawHash} 供增量更新使用</li>
     * </ol>
     * 不建议在本方法内直接写库，入库由 ContestCrawlService 统一处理。
     * </p>
     *
     * @return 归一化赛事列表；无数据或平台暂不可用时返回空列表，不要返回 null
     */
    List<ContestDTO> fetchContests();

    /**
     * 当前爬虫是否启用。
     * <p>
     * 可读 {@code contest_source.crawl_enabled} 配置；
     * 迭代 1 默认返回 true，迭代 2 接入数据库配置。
     * </p>
     *
     * @return {@code true} 表示参与定时调度与手动触发
     */
    boolean enabled();
}
