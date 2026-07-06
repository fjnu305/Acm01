-- 角色初始化（若尚未插入）
INSERT IGNORE INTO role (role_code, role_name, status) VALUES
('USER', '普通用户', 1),
('ADMIN', '管理员', 1);
