-- 角色初始数据（新库请优先执行 init-rbac.sql 建表 + 初始化）
INSERT IGNORE INTO role (role_code, role_name, status) VALUES
('USER', '普通用户', 1),
('ADMIN', '管理员', 1);
