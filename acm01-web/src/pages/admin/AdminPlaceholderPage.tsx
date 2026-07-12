import PageHeader from '../../components/PageHeader'

interface AdminPlaceholderPageProps {
  title: string
  description: string
}

export default function AdminPlaceholderPage({ title, description }: AdminPlaceholderPageProps) {
  return (
    <div className="page-content">
      <PageHeader title={title} description={description} />
      <div className="empty-panel">
        <div className="empty-icon">◇</div>
        <h2>功能开发中</h2>
        <p>该模块将在后续版本开放，当前可通过控制台使用已上线功能。</p>
      </div>
    </div>
  )
}
