package org.fjnu305.acm01.module.contest.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ContestMapper 批量写库辅助类。
 * <p>MyBatis XML 不便表达 JDBC BATCH 复用单条 UPDATE SQL，故在此封装。</p>
 */
@Component
@RequiredArgsConstructor
public class ContestMapperSupport {

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * 按 source + external_id 批量 UPDATE（JDBC BATCH，与 {@link ContestMapper#updateBySourceAndExternalId} 语义一致）。
     *
     * @param list 待更新记录，为空时返回 0
     * @return 实际更新条数
     */
    public int batchUpdateBySourceAndExternalId(List<ContestEntity> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        SqlSession batchSession = SqlSessionUtils.getSqlSession(sqlSessionFactory, ExecutorType.BATCH, null);
        try {
            ContestMapper mapper = batchSession.getMapper(ContestMapper.class);
            for (ContestEntity entity : list) {
                mapper.updateBySourceAndExternalId(entity);
            }
            batchSession.flushStatements();
            return list.size();
        } finally {
            SqlSessionUtils.closeSqlSession(batchSession, sqlSessionFactory);
        }
    }
}
