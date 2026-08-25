import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { apiFetchJson } from "@/lib/api";
import { Colors, Radius, Spacing } from "@/constants/theme";

interface RestaurantRecommend {
  id: number | null;
  name: string;
  categoryName?: string | null;
  address?: string | null;
  roadAddress?: string | null;
  region1?: string | null;
  region2?: string | null;
}

export default function HotPlaceScreen() {
  const scheme = useColorScheme();
  const dark = scheme === "dark";
  const c = Colors[dark ? "dark" : "light"];

  const [places, setPlaces] = useState<RestaurantRecommend[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async (isRefresh = false) => {
    isRefresh ? setRefreshing(true) : setLoading(true);
    const { ok, data } = await apiFetchJson<RestaurantRecommend[]>("/api/v1/restaurants/today-hot");
    if (ok && Array.isArray(data)) setPlaces(data);
    isRefresh ? setRefreshing(false) : setLoading(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: c.background }]} edges={["top"]}>
      <View style={styles.header}>
        <Text style={[styles.headerTitle, { color: c.text }]}>오늘의 핫플 🔥</Text>
        <Text style={{ fontSize: 12, color: c.textSecondary }}>최근 24시간 인기 TOP 3</Text>
      </View>

      {loading ? (
        <ActivityIndicator style={{ marginTop: 80 }} size="large" color={c.primary} />
      ) : (
        <FlatList
          data={places}
          keyExtractor={(p, i) => String(p.id ?? i)}
          contentContainerStyle={{ padding: Spacing.lg, gap: Spacing.lg }}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => load(true)} tintColor={c.primary} />}
          renderItem={({ item, index }) => (
            <View style={[styles.card, { backgroundColor: c.card, borderColor: c.border }]}>
              <View style={[styles.rank, { backgroundColor: c.primary }]}>
                <Text style={{ color: "#fff", fontWeight: "800", fontSize: 15 }}>{index + 1}</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={{ fontSize: 16, fontWeight: "700", color: c.text }}>{item.name}</Text>
                {!!item.categoryName && (
                  <Text style={{ fontSize: 12, color: c.textSecondary, marginTop: 2 }}>{item.categoryName}</Text>
                )}
                {!!(item.roadAddress || item.address) && (
                  <Text style={{ fontSize: 13, color: c.textSecondary, marginTop: 4 }} numberOfLines={1}>
                    📍 {item.roadAddress ?? item.address}
                  </Text>
                )}
              </View>
            </View>
          )}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={{ fontSize: 44 }}>🔥</Text>
              <Text style={{ color: c.textSecondary, marginTop: 12 }}>아직 핫플 데이터가 없어요</Text>
            </View>
          }
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md },
  headerTitle: { fontSize: 24, fontWeight: "800" },
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: Spacing.lg,
    borderRadius: Radius.card,
    borderWidth: 1,
    padding: Spacing.lg,
  },
  rank: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: "center",
    justifyContent: "center",
  },
  empty: { alignItems: "center", marginTop: 100 },
});