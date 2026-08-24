-- 开发环境：重建组队模块表（未上线可直接执行）
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `match_record`;
DROP TABLE IF EXISTS `team_member_log`;
DROP TABLE IF EXISTS `team_member`;
DROP TABLE IF EXISTS `team_post`;
SET FOREIGN_KEY_CHECKS = 1;

SOURCE init-team.sql;
