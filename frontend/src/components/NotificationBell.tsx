"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Bell } from "lucide-react";
import { apiFetchJson } from "@/lib/api";

interface NotificationItem {
  id: number;
  actorId: number;
  actorNickname: string;
  feedId: number | null;
  type: "NEW_FEED";
  message: string;
  isRead: boolean;
  createdAt: string;
}

function formatCreatedAt(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleString();
}

export function NotificationBell() {
  const router = useRouter();
  const menuRef = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);

  const unreadCount = notifications.filter((notification) => !notification.isRead).length;

  const loadNotifications = useCallback(async () => {
    setLoading(true);
    const res = await apiFetchJson<NotificationItem[]>("/api/v1/notifications");
    if (res.ok && res.data) {
      setNotifications(res.data);
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    const frame = requestAnimationFrame(() => {
      void loadNotifications();
    });

    return () => cancelAnimationFrame(frame);
  }, [loadNotifications]);

  useEffect(() => {
    if (open) {
      const frame = requestAnimationFrame(() => {
        void loadNotifications();
      });

      return () => cancelAnimationFrame(frame);
    }
  }, [loadNotifications, open]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (event.target instanceof Node && menuRef.current && !menuRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  const handleNotificationClick = async (notification: NotificationItem) => {
    if (!notification.isRead) {
      const res = await apiFetchJson<NotificationItem>(
        `/api/v1/notifications/${notification.id}/read`,
        { method: "PUT" }
      );

      if (res.ok && res.data) {
        const updatedNotification = res.data;
        setNotifications((prev) =>
          prev.map((item) => (item.id === notification.id ? updatedNotification : item))
        );
      }
    }

    setOpen(false);
    if (notification.feedId) {
      router.push("/feed");
    }
  };

  return (
    <div className="relative" ref={menuRef}>
      <button
        onClick={() => setOpen((prev) => !prev)}
        className="relative rounded-full p-2.5 text-muted transition-colors hover:bg-surface-soft hover:text-ink"
        aria-label="알림"
      >
        <Bell className="h-6 w-6" />
        {unreadCount > 0 && (
          <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold leading-none text-white">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 overflow-hidden rounded-xl border border-hairline-soft bg-surface shadow-lg animate-in fade-in-50 zoom-in-95">
          <div className="flex items-center justify-between border-b border-hairline-soft px-4 py-3">
            <p className="text-sm font-bold text-ink">알림</p>
            <span className="text-xs font-semibold text-muted">읽지 않음 {unreadCount}</span>
          </div>

          <div className="max-h-96 overflow-y-auto py-2">
            {loading ? (
              <p className="px-4 py-8 text-center text-sm text-muted">알림을 불러오는 중입니다.</p>
            ) : notifications.length === 0 ? (
              <p className="px-4 py-8 text-center text-sm text-muted">새 알림이 없습니다.</p>
            ) : (
              notifications.map((notification) => (
                <button
                  key={notification.id}
                  type="button"
                  onClick={() => handleNotificationClick(notification)}
                  className="flex w-full gap-3 px-4 py-3 text-left transition-colors hover:bg-surface-soft"
                >
                  <span
                    className={`mt-1 h-2 w-2 shrink-0 rounded-full ${
                      notification.isRead ? "bg-hairline" : "bg-primary"
                    }`}
                  />
                  <span className="min-w-0 flex-1">
                    <span className="block text-sm font-semibold text-ink">
                      {notification.actorNickname}
                    </span>
                    <span className="mt-0.5 block text-sm leading-5 text-body">
                      {notification.message}
                    </span>
                    <span className="mt-1 block text-xs text-muted-soft">
                      {formatCreatedAt(notification.createdAt)}
                    </span>
                  </span>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
