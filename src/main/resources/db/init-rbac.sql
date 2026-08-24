-- 模块 1：RBAC 角色与权限表
USE acm;

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `status` TINYINT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `created_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `role_code` (`role_code`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `perm_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `perm_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `created_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `perm_code` (`perm_code`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='用户角色表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='角色权限表';

-- 角色初始数据
INSERT IGNORE INTO role (role_code, role_name, status) VALUES
('USER', '普通用户', 1),
('ADMIN', '管理员', 1);
