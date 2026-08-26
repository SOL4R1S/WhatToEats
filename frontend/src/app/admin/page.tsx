"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { apiFetchJson } from "@/lib/api";

interface ReportView {
  reportId: number;
  targetType: string;
  targetId: number | null;
  targetContent: string | null;
  targetAuthorId: number | null;
  reporterId: number;
  reportedUserId: number | null;
  reason: string;
  detail: string | null;
  status: string;
  ruleScore: number;
  matchedRules: string | null;
  resolutionNote: string | null;
  resolvedAt: string | null;
  createdAt: string | null;
}

type Tab = "pending" | "resolved" | "rejected";

const REASON_LABEL: Record<string, string> = {
  SPAM: "스팸/광고", ABUSE: "욕설/비방", PORNOGRAPHY: "음란물",
  FRAUD: "사기", COPYRIGHT: "저작권", OTHER: "기타",
};

export default function AdminPage() {
  const [tab, setTab] = useState<Tab>("pending");
  const [reports, setReports] = useState<ReportView[]>([]);
  const [stats, setStats] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [working, setWorking] = useState<number | null>(null);

  const load = useCallback(async (t: Tab) => {
    setLoading(true);
    setError(null);
    const path = t === "pending" ? "/api/v1/admin/reports/pending" : `/api/v1/admin/reports/history?limit=50`;
    const { ok, data, message } = await apiFetchJson<ReportView[]>(path);
    if (ok && Array.isArray(data)) {
      setReports(data);
    } else {
      setError(message ?? "접근 권한이 없습니다. 관리자 계정으로 로그인해주세요.");
    }
    // stats
    const st = await apiFetchJson<Record<string, number>>("/api/v1/admin/reports/stats");
    if (st.ok && st.data) setStats(st.data);
    setLoading(false);
  }, []);

  useEffect(() => {
    load(tab);
  }, [tab, load]);

  const act = async (reportId: number, action: "resolve" | "reject" | "restore") => {
    setWorking(reportId);
    const { ok } = await apiFetchJson(`/api/v1/admin/reports/${reportId}/${action}`, { method: "POST" });
    setWorking(null);
    if (ok) load(tab);
  };

  const tabs: { key: Tab; label: string }[] = [
    { key: "pending", label: `검토 대기 (${stats.pending ?? 0})` },
    { key: "resolved", label: `삭제 처리 (${stats.resolved ?? 0})` },
    { key: "rejected", label: `기각 (${stats.rejected ?? 0})` },
  ];

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-ink">신고 검토 관리자</h1>
          <p className="text-sm text-muted">명확한 신고만 삭제하고, 애매한 것은 검토 대기로 남겨주세요. 삭제는 언제든 롤백됩니다.</p>
        </div>
        <Link href="/feed" className="text-sm text-muted hover:text-ink">← 서비스로</Link>
      </header>

      <div className="mt-6 flex gap-2 rounded-xl bg-surface-soft p-1">
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`flex-1 rounded-lg py-2 text-sm font-bold transition-all ${
              tab === t.key ? "bg-surface text-ink shadow-sm" : "text-muted hover:text-ink"
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {error && <p className="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-600">{error}</p>}

      {loading ? (
        <p className="mt-8 text-center text-muted">불러오는 중...</p>
      ) : reports.length === 0 ? (
        <p className="mt-8 text-center text-muted">항목이 없습니다.</p>
      ) : (
        <ul className="mt-4 space-y-3">
          {reports.map((r) => (
            <li key={r.reportId} className="rounded-2xl border border-hairline bg-surface p-4">
              <div className="flex items-center justify-between">
                <span className="inline-flex items-center gap-1 rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-bold text-primary">
                  {REASON_LABEL[r.reason] ?? r.reason}
                </span>
                <div className="flex items-center gap-2">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-bold ${
                    r.status === "RESOLVED" ? "bg-red-100 text-red-600"
                    : r.status === "REJECTED" ? "bg-gray-100 text-gray-500"
                    : "bg-amber-100 text-amber-600"
                  }`}>
                    {r.status === "RESOLVED" ? "삭제됨" : r.status === "REJECTED" ? "기각" : "검토대기"}
                  </span>
                  {r.status !== "RESOLVED" && (
                    <span className={`rounded-full px-2 py-0.5 text-xs font-bold ${
                      r.ruleScore >= 30 ? "bg-orange-100 text-orange-600" : "bg-gray-100 text-gray-500"
                    }`}>
                      룰점수 {r.ruleScore}
                    </span>
                  )}
                </div>
              </div>

              <p className="mt-2 text-sm text-ink whitespace-pre-wrap">
                {r.targetContent ?? "(콘텐츠가 이미 물리 삭제됨)"}
              </p>

              {(r.matchedRules || r.detail) && (
                <p className="mt-2 text-xs text-muted">
                  {r.matchedRules && <>⚙️ {r.matchedRules}</>}
                  {r.detail && <>{r.matchedRules ? " · " : ""}💬 {r.detail}</>}
                </p>
              )}
              <p className="mt-1 text-xs text-muted-soft">
                대상ID {r.targetId ?? "-"} · 신고자#{r.reporterId} · 작성자#{r.reportedUserId ?? "-"} ·{" "}
                {r.createdAt?.slice(0, 16).replace("T", " ")}
              </p>
              {r.resolutionNote && (
                <p className="mt-1 text-xs text-emerald-700">처리메모: {r.resolutionNote}</p>
              )}

              <div className="mt-3 flex gap-2">
                {r.status === "PENDING" ? (
                  <>
                    <button onClick={() => act(r.reportId, "resolve")} disabled={working === r.reportId}
                      className="rounded-lg bg-red-500 px-3 py-1.5 text-xs font-bold text-white disabled:opacity-60">
                      삭제 처리
                    </button>
                    <button onClick={() => act(r.reportId, "reject")} disabled={working === r.reportId}
                      className="rounded-lg border border-hairline px-3 py-1.5 text-xs font-bold text-ink hover:bg-surface-soft disabled:opacity-60">
                      기각
                    </button>
                  </>
                ) : r.status === "RESOLVED" ? (
                  <button onClick={() => act(r.reportId, "restore")} disabled={working === r.reportId}
                    className="rounded-lg border border-emerald-500 px-3 py-1.5 text-xs font-bold text-emerald-600 hover:bg-emerald-50 disabled:opacity-60">
                    복원 (롤백)
                  </button>
                ) : (
                  <span className="text-xs text-muted-soft">검토 종료</span>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}