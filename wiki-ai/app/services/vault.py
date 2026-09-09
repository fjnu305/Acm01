from __future__ import annotations

import re
from pathlib import Path

from app.config import settings

WIKI_LINK_RE = re.compile(r"\[\[([^\]|#]+)(?:[|#][^\]]*)?\]\]")
FRONTMATTER_RE = re.compile(r"^---\s*\n(.*?)\n---\s*\n", re.DOTALL)


def vault_root() -> Path:
    root = settings.resolved_vault()
    if not root.exists():
        raise FileNotFoundError(f"Vault not found: {root}")
    return root


def wiki_dir() -> Path:
    return vault_root() / "wiki"


def safe_wiki_path(rel: str) -> Path:
    """Resolve a path under wiki/, reject traversal."""
    rel = rel.replace("\\", "/").lstrip("/")
    if not rel.startswith("wiki/"):
        if rel.startswith("wiki"):
            rel = "wiki/" + rel[4:].lstrip("/")
        else:
            rel = f"wiki/{rel}"
    base = vault_root()
    target = (base / rel).resolve()
    wiki = wiki_dir().resolve()
    if not str(target).startswith(str(wiki)) or not target.is_file():
        raise FileNotFoundError(f"Page not found: {rel}")
    if target.suffix.lower() != ".md":
        raise FileNotFoundError(f"Not a markdown page: {rel}")
    return target


def parse_frontmatter(text: str) -> tuple[dict, str]:
    meta: dict = {}
    body = text
    m = FRONTMATTER_RE.match(text)
    if m:
        raw = m.group(1)
        body = text[m.end() :]
        for line in raw.splitlines():
            if ":" not in line:
                continue
            key, _, val = line.partition(":")
            key = key.strip()
            val = val.strip().strip("\"'")
            if key:
                meta[key] = val
    return meta, body


def page_title(path: Path, meta: dict, body: str) -> str:
    if meta.get("title"):
        return meta["title"]
    for line in body.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return path.stem


def list_markdown_files() -> list[Path]:
    wiki = wiki_dir()
    if not wiki.exists():
        return []
    return sorted(p for p in wiki.rglob("*.md") if p.is_file())


def rel_from_vault(path: Path) -> str:
    return path.resolve().relative_to(vault_root().resolve()).as_posix()


def read_page(rel: str) -> dict:
    path = safe_wiki_path(rel)
    text = path.read_text(encoding="utf-8")
    meta, body = parse_frontmatter(text)
    return {
        "path": rel_from_vault(path),
        "title": page_title(path, meta, body),
        "frontmatter": meta,
        "content": body,
        "raw": text,
    }


def vault_status() -> dict:
    root = vault_root()
    wiki = wiki_dir()
    hot = wiki / "hot.md"
    index = wiki / "index.md"
    bm25 = root / ".vault-meta" / "bm25" / "index.json"
    return {
        "vaultRoot": str(root),
        "wikiExists": wiki.is_dir(),
        "hotExists": hot.is_file(),
        "indexExists": index.is_file(),
        "retrieveReady": bm25.is_file(),
        "pageCount": len(list_markdown_files()),
    }


def build_title_index() -> dict[str, str]:
    """Map lowercased title / stem -> vault-relative path."""
    index: dict[str, str] = {}
    for path in list_markdown_files():
        rel = rel_from_vault(path)
        try:
            text = path.read_text(encoding="utf-8")
        except OSError:
            continue
        meta, body = parse_frontmatter(text)
        title = page_title(path, meta, body)
        index[title.lower()] = rel
        index[path.stem.lower()] = rel
    return index


def resolve_wikilink(name: str, title_index: dict[str, str]) -> str | None:
    key = name.strip().lower()
    if key in title_index:
        return title_index[key]
    # try path-like
    candidate = name.strip().replace("\\", "/")
    if not candidate.endswith(".md"):
        candidate = f"{candidate}.md"
    try:
        return rel_from_vault(safe_wiki_path(candidate))
    except FileNotFoundError:
        return None


def build_graph() -> dict:
    title_index = build_title_index()
    nodes: list[dict] = []
    edges: list[dict] = []
    seen_edges: set[tuple[str, str]] = set()

    for path in list_markdown_files():
        rel = rel_from_vault(path)
        try:
            text = path.read_text(encoding="utf-8")
        except OSError:
            continue
        meta, body = parse_frontmatter(text)
        title = page_title(path, meta, body)
        parts = Path(rel).parts
        group = parts[1] if len(parts) > 2 else "root"
        nodes.append({"id": rel, "label": title, "group": group})

        for match in WIKI_LINK_RE.finditer(text):
            target_name = match.group(1).strip()
            target = resolve_wikilink(target_name, title_index)
            if not target or target == rel:
                continue
            key = (rel, target)
            if key in seen_edges:
                continue
            seen_edges.add(key)
            edges.append({"source": rel, "target": target, "type": "wikilink"})

        related = meta.get("related", "")
        if related:
            for part in re.split(r"[,，]", related):
                name = part.strip().strip("\"'[]")
                if not name:
                    continue
                target = resolve_wikilink(name, title_index)
                if not target or target == rel:
                    continue
                key = (rel, target)
                if key in seen_edges:
                    continue
                seen_edges.add(key)
                edges.append({"source": rel, "target": target, "type": "related"})

    return {"nodes": nodes, "edges": edges}


def load_context_for_ask(mode: str, question: str, limit: int = 5) -> list[dict]:
    """Return list of {path, title, excerpt} for prompting."""
    pages: list[dict] = []
    wiki = wiki_dir()

    def add_file(path: Path, max_chars: int = 2500) -> None:
        if not path.is_file():
            return
        rel = rel_from_vault(path)
        if any(p["path"] == rel for p in pages):
            return
        text = path.read_text(encoding="utf-8")
        meta, body = parse_frontmatter(text)
        title = page_title(path, meta, body)
        excerpt = body.strip()
        if len(excerpt) > max_chars:
            excerpt = excerpt[:max_chars] + "\n…"
        pages.append({"path": rel, "title": title, "excerpt": excerpt})

    add_file(wiki / "hot.md", 2000)
    if mode == "quick":
        add_file(wiki / "index.md", 1500)
        return pages

    # standard / deep: keyword overlap over wiki pages
    tokens = [t.lower() for t in re.findall(r"[\w\u4e00-\u9fff]{2,}", question)]
    scored: list[tuple[int, Path]] = []
    for path in list_markdown_files():
        if path.name in {"hot.md", "log.md"}:
            continue
        try:
            text = path.read_text(encoding="utf-8").lower()
        except OSError:
            continue
        score = sum(text.count(t) for t in tokens)
        if score > 0:
            scored.append((score, path))
    scored.sort(key=lambda x: (-x[0], x[1].as_posix()))
    top_n = 8 if mode == "deep" else limit
    for _, path in scored[:top_n]:
        add_file(path, 3000 if mode == "deep" else 2200)

    if len(pages) <= 1:
        add_file(wiki / "index.md", 1500)
        add_file(wiki / "overview.md", 2000)

    return pages
