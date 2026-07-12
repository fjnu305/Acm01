import { useState } from 'react'
import { ApiError } from '../../api/auth'
import { CONTEST_SOURCES, triggerContestCrawl } from '../../api/contest'
import PageHeader from '../../components/PageHeader'

interface CrawlFeedback {
  sourceCode: string
  logId: number | null
  at: string
}

export default function AdminCrawlPage() {
  const [crawlingSource, setCrawlingSource] = useState<string | null>(null)
  const [crawlError, setCrawlError] = useState('')
  const [crawlFeedback, setCrawlFeedback] = useState<CrawlFeedback | null>(null)

  const handleCrawl = async (sourceCode: string) => {
    setCrawlingSource(sourceCode)
    setCrawlError('')
    setCrawlFeedback(null)
    try {
      const logId = await triggerContestCrawl(sourceCode)
      setCrawlFeedback({
        sourceCode,
        logId,
        at: new Date().toLocaleString('zh-CN'),
      })
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : '爬取失败，请稍后重试'
      setCrawlError(msg)
    } finally {
      setCrawlingSource(null)
    }
  }

  return (
    <div className="page-content">
      <PageHeader
        title="爬虫管理"
        description="手动触发各 OJ 平台赛事数据同步。Codeforces、AtCoder 已接入，其余平台为占位实现。"
      />

      <section className="platform-crawl-grid">
        {CONTEST_SOURCES.map((source) => (
          <article key={source.code} className="platform-crawl-card">
            <div className="platform-crawl-top">
              <span className={`source-badge source-${source.code}`}>{source.short}</span>
              <div>
                <h3>{source.label}</h3>
                <p className="platform-crawl-code">{source.code}</p>
              </div>
            </div>
            {!source.implemented && <span className="stub-tag">开发中</span>}
            <button
              type="button"
              className="btn-primary admin-crawl-btn"
              disabled={crawlingSource !== null}
              onClick={() => void handleCrawl(source.code)}
            >
              {crawlingSource === source.code ? '同步中…' : '立即同步'}
            </button>
          </article>
        ))}
      </section>

      {crawlError && <div className="alert alert-error">{crawlError}</div>}

      {crawlFeedback && (
        <div className="alert alert-success">
          <p>
            <strong>{crawlFeedback.sourceCode}</strong> 同步请求已提交 · {crawlFeedback.at}
          </p>
          {crawlFeedback.logId != null ? (
            <p>日志编号：{crawlFeedback.logId}</p>
          ) : (
            <p>该平台暂未启用或本次未产生日志记录</p>
          )}
        </div>
      )}
    </div>
  )
}
