"use client";

import { useEffect, useState } from "react";
import { X, Send, Trash2 } from "lucide-react";
import { apiFetchJson } from "@/lib/api";
import { getStoredUser } from "@/lib/user";

interface Comment {
  id: number;
  content: string;
  userId: number;
  nickname: string;
  createdAt: string;
}

interface CommentModalProps {
  feedId: number;
  onClose: () => void;
  onCountChange?: (count: number) => void;
}

export default function CommentModal({ feedId, onClose, onCountChange }: CommentModalProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const currentUser = getStoredUser();

  useEffect(() => {
    const loadComments = async () => {
      setLoading(true);
      setError("");
      const res = await apiFetchJson<Comment[]>(`/api/v1/feeds/${feedId}/comments`);
      if (res.ok && res.data) {
        setComments(res.data);
        onCountChange?.(res.data.length);
      } else {
        setError(res.message || "댓글을 불러오지 못했습니다.");
      }
      setLoading(false);
    };

    loadComments();
  }, [feedId, onCountChange]);

  const handleSubmit = async () => {
    if (!content.trim() || submitting) return;
    setSubmitting(true);
    const res = await apiFetchJson<Comment>(`/api/v1/feeds/${feedId}/comments`, {
      method: "POST",
      body: JSON.stringify({ content: content.trim() }),
    });
    if (res.ok && res.data) {
      setContent("");
      setComments((prev) => [...prev, res.data!]);
      onCountChange?.(comments.length + 1);
    } else {
      alert(res.message || "댓글 작성에 실패했습니다.");
    }
    setSubmitting(false);
  };

  const handleDelete = async (commentId: number) => {
    if (!confirm("댓글을 삭제하시겠습니까?")) return;
    const res = await apiFetchJson(`/api/v1/feeds/${feedId}/comments/${commentId}`, {
      method: "DELETE",
    });
    if (res.ok) {
      setComments((prev) => prev.filter((c) => c.id !== commentId));
      onCountChange?.(comments.length - 1);
    } else {
      alert(res.message || "댓글 삭제에 실패했습니다.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/55 backdrop-blur-xs sm:items-center sm:p-4">
      <div className="flex h-[80vh] w-full flex-col rounded-t-2xl bg-surface shadow-xl sm:h-auto sm:max-h-[600px] sm:w-full sm:max-w-lg sm:rounded-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-hairline-soft px-5 py-4">
          <h3 className="text-base font-bold text-ink">댓글 {comments.length}개</h3>
          <button onClick={onClose} className="text-muted hover:text-ink">
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto px-5 py-4">
          {loading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex gap-3">
                  <div className="h-8 w-8 shrink-0 rounded-full bg-surface-soft animate-pulse" />
                  <div className="flex-1 space-y-2">
                    <div className="h-3 w-20 rounded bg-surface-soft animate-pulse" />
                    <div className="h-3 w-full rounded bg-surface-soft animate-pulse" />
                  </div>
                </div>
              ))}
            </div>
          ) : error ? (
            <p className="py-10 text-center text-sm text-red-500">{error}</p>
          ) : comments.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-muted">
              <MessageCircleIcon className="mb-2 h-8 w-8 opacity-40" />
              <p className="text-sm">첫 댓글을 남겨보세요</p>
            </div>
          ) : (
            <div className="space-y-5">
              {comments.map((comment) => (
                <div key={comment.id} className="flex gap-3">
                  <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                    {comment.nickname.charAt(0)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-sm font-bold text-ink">{comment.nickname}</span>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-soft">
                          {new Date(comment.createdAt).toLocaleString()}
                        </span>
                        {currentUser?.userId === comment.userId && (
                          <button
                            onClick={() => handleDelete(comment.id)}
                            className="text-muted-soft hover:text-red-500 transition-colors"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        )}
                      </div>
                    </div>
                    <p className="mt-0.5 text-sm text-body leading-relaxed">{comment.content}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Input */}
        <div className="border-t border-hairline-soft px-5 py-4">
          {currentUser ? (
            <div className="flex items-center gap-3">
              <input
                type="text"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
                placeholder="댓글을 입력하세요"
                maxLength={500}
                className="flex-1 rounded-xl border border-hairline bg-surface-soft px-4 py-2.5 text-sm text-ink placeholder:text-muted-soft focus:border-primary focus:outline-none"
              />
              <button
                onClick={handleSubmit}
                disabled={!content.trim() || submitting}
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary text-white hover:bg-primary-active disabled:opacity-50 transition-colors"
              >
                <Send className="h-4 w-4" />
              </button>
            </div>
          ) : (
            <p className="text-center text-sm text-muted">로그인 후 댓글을 작성할 수 있습니다</p>
          )}
        </div>
      </div>
    </div>
  );
}

function MessageCircleIcon({ className }: { className?: string }) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z" />
    </svg>
  );
}
