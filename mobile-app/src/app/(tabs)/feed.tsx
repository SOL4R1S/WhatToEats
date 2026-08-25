import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useAuth } from "@/context/AuthContext";
import { apiFetchJson } from "@/lib/api";
import type { FeedListResponse } from "@/lib/types";
import { Colors, Radius, Spacing } from "@/constants/theme";

export default function FeedScreen() {
  const scheme = useColorScheme();
  const dark = scheme === "dark";
  const c = Colors[dark ? "dark" : "light"];
  const { user } = useAuth();

  const [feeds, setFeeds] = useState<FeedListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (isRefresh = false) => {
    isRefresh ? setRefreshing(true) : setLoading(true);
    setError(null);
    // 추천 피드가 기본. 로그인 전이면 빈 목록 안내.
    const { ok, data, message } = await apiFetchJson<FeedListResponse[]>("/api/v1/feeds/recommend");
    if (ok && Array.isArray(data)) {
      setFeeds(data);
    } else if (!ok) {
      setError(message ?? "피드를 불러오지 못했어요.");
    }
    isRefresh ? setRefreshing(false) : setLoading(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const renderItem = ({ item }: { item: FeedListResponse }) => (
    <View style={[styles.card, { backgroundColor: c.card, borderColor: c.border }]}>
      {/* 헤더: 프로필 + 닉네임 + 맛집 */}
      <View style={styles.cardHeader}>
        <View style={[styles.avatar, { backgroundColor: c.primarySoft }]}>
          <Text style={{ fontSize: 16 }}>{item.profileImage ? "📷" : "🙂"}</Text>
        </View>
        <View style={{ flex: 1 }}>
          <Text style={[styles.nickname, { color: c.text }]}>{item.nickname}</Text>
          {item.restaurantName && (
            <Text style={[styles.restaurant, { color: c.primary }]}>📍 {item.restaurantName}</Text>
          )}
        </View>
      </View>

      {/* 내용 */}
      <Text style={[styles.content, { color: c.text }]} numberOfLines={5}>
        {item.content}
      </Text>

      {/* 액션 바 */}
      <View style={styles.actionBar}>
        <Text style={[styles.action, { color: item.isLikedByMe ? c.like : c.textSecondary }]}>
          ❤️ {item.likeCount}
        </Text>
        <Text style={[styles.action, { color: c.textSecondary }]}>💬 {item.commentCount}</Text>
        {item.moodTag && item.moodTag !== "NONE" && (
          <View style={[styles.moodPill, { backgroundColor: c.primarySoft }]}>
            <Text style={{ color: c.primary, fontSize: 11, fontWeight: "700" }}>{item.moodTag}</Text>
          </View>
        )}
      </View>
    </View>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: c.background }]} edges={["top"]}>
      {/* 헤더 */}
      <View style={styles.header}>
        <Text style={[styles.headerTitle, { color: c.text }]}>뭐먹지? 🍽️</Text>
        <Text style={{ fontSize: 13, color: c.textSecondary }}>
          {user ? `${user.nickname}님` : ""}
        </Text>
      </View>

      {loading ? (
        <ActivityIndicator style={{ marginTop: 80 }} size="large" color={c.primary} />
      ) : error ? (
        <View style={styles.center}>
          <Text style={{ color: c.textSecondary, textAlign: "center", lineHeight: 22 }}>{error}</Text>
          <Pressable onPress={() => load()} style={[styles.retryBtn, { backgroundColor: c.primary }]}>
            <Text style={{ color: "#fff", fontWeight: "700" }}>다시 시도</Text>
          </Pressable>
        </View>
      ) : feeds.length === 0 ? (
        <View style={styles.center}>
          <Text style={{ fontSize: 44 }}>🍽️</Text>
          <Text style={{ color: c.textSecondary, marginTop: 12 }}>아직 피드가 없어요</Text>
        </View>
      ) : (
        <FlatList
          data={feeds}
          keyExtractor={(f) => String(f.feedId)}
          renderItem={renderItem}
          contentContainerStyle={{ padding: Spacing.lg, gap: Spacing.lg }}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={() => load(true)} tintColor={c.primary} />
          }
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: "row",
    alignItems: "baseline",
    justifyContent: "space-between",
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
  },
  headerTitle: { fontSize: 24, fontWeight: "800" },
  center: { flex: 1, alignItems: "center", justifyContent: "center", padding: Spacing.xl },
  retryBtn: { marginTop: Spacing.lg, paddingHorizontal: 28, paddingVertical: 12, borderRadius: Radius.button },
  card: {
    borderRadius: Radius.card,
    borderWidth: 1,
    padding: Spacing.lg,
    gap: Spacing.md,
  },
  cardHeader: { flexDirection: "row", alignItems: "center", gap: Spacing.md },
  avatar: { width: 42, height: 42, borderRadius: 21, alignItems: "center", justifyContent: "center" },
  nickname: { fontSize: 15, fontWeight: "700" },
  restaurant: { fontSize: 12, fontWeight: "600", marginTop: 2 },
  content: { fontSize: 15, lineHeight: 22 },
  actionBar: { flexDirection: "row", alignItems: "center", gap: Spacing.lg },
  action: { fontSize: 14, fontWeight: "600" },
  moodPill: { marginLeft: "auto", paddingHorizontal: 10, paddingVertical: 4, borderRadius: Radius.pill },
});