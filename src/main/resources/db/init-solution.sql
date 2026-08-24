-- 模块 8：题解分享
CREATE TABLE IF NOT EXISTS `solution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题解ID',
    `user_id` BIGINT NOT NULL COMMENT '作者用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `content` MEDIUMTEXT NOT NULL COMMENT 'Markdown 正文（入库前已消毒）',
    `problem_source` VARCHAR(32) NULL DEFAULT NULL COMMENT '题目来源平台',
    `problem_id` VARCHAR(128) NULL DEFAULT NULL COMMENT '题目编号',
    `tags` VARCHAR(500) NULL DEFAULT NULL COMMENT '标签，逗号分隔',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1已发布 0草稿 2下架',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_status_time` (`status`, `created_time`),
    INDEX `idx_problem` (`problem_source`, `problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题解表';

CREATE TABLE IF NOT EXISTS `solution_favorite` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `solution_id` BIGINT NOT NULL COMMENT '题解ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `solution_id`),
    INDEX `idx_solution` (`solution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题解收藏';

CREATE TABLE IF NOT EXISTS `solution_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `category` VARCHAR(50) NOT NULL COMMENT '分类：DP/图论/数据结构等',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建者，NULL 表示系统模板',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='算法模板库';

INSERT IGNORE INTO solution_template (id, name, category, content, created_by) VALUES
(1, '二分答案模板', '二分', 'bool check(int x) {\n    // TODO\n    return true;\n}\n\nint lo = 0, hi = 1e9;\nwhile (lo < hi) {\n    int mid = lo + (hi - lo) / 2;\n    if (check(mid)) hi = mid;\n    else lo = mid + 1;\n}', NULL),
(2, 'Dijkstra 最短路', '图论', 'priority_queue<pair<int,int>, vector<pair<int,int>>, greater<>> pq;\nvector<int> dist(n, INF);\ndist[s] = 0;\npq.push({0, s});\nwhile (!pq.empty()) {\n    auto [d, u] = pq.top(); pq.pop();\n    if (d > dist[u]) continue;\n    for (auto [v, w] : g[u]) {\n        if (dist[u] + w < dist[v]) {\n            dist[v] = dist[u] + w;\n            pq.push({dist[v], v});\n        }\n    }\n}', NULL);
