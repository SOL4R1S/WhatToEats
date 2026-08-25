import { useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  useColorScheme,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { apiFetchJson } from "@/lib/api";
import { Colors, Radius, Spacing } from "@/constants/theme";

interface RestaurantItem {
  id: number | null;
  name: string;
  address?: string | null;
  roadAddress?: string | null;
  categoryName?: string | null;
}

export default function SearchScreen() {
  const scheme = useColorScheme();
  const dark = scheme === "dark";
  const c = Colors[dark ? "dark" : "light"];

  const [query, setQuery] = useState("");
  const [results, setResults] = useState<RestaurantItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Enter/버튼 제출 방식 — 웹 프론트와 동일한 정책 (타이핑마다 API 호출 안 함)
  const submit = async () => {
    const trimmed = query.trim();
    if (!trimmed) return;
    setLoading(true);
    setError(null);
    const { ok, data, message } = await apiFetchJson<RestaurantItem[]>(
      `/api/v1/restaurants/search?name=${encodeURIComponent(trimmed)}`,
    );
    if (ok && Array.isArray(data)) {
      setResults(data);
    } else if (!ok) {
      setError(message ?? "검색에 실패했어요.");
    }
    setSearched(true);
    setLoading(false);
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: c.background }]} edges={["top"]}>
      <View style={styles.header}>
        <Text style={[styles.headerTitle, { color: c.text }]}>맛집 검색 🔍</Text>
        <View style={styles.searchRow}>
          <TextInput
            style={[styles.input, { backgroundColor: c.card, borderColor: c.border, color: c.text }]}
            placeholder="식당 이름으로 검색"
            placeholderTextColor={c.textSecondary}
            value={query}
            onChangeText={setQuery}
            onSubmitEditing={submit}
            returnKeyType="search"
            autoCapitalize="none"
          />
          <Pressable
            style={[styles.searchBtn, { backgroundColor: c.primary }]}
            onPress={submit}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator size="small" color="#fff" />
            ) : (
              <Text style={{ fontSize: 18 }}>🔍</Text>
            )}
          </Pressable>
        </View>
      </View>

      {error && <Text style={{ color: c.danger, textAlign: "center" }}>{error}</Text>}

      <FlatList
        data={results}
        keyExtractor={(r, i) => String(r.id ?? i)}
        contentContainerStyle={{ padding: Spacing.lg, gap: Spacing.md }}
        ListEmptyComponent={
          searched && !loading ? (
            <View style={styles.empty}>
              <Text style={{ fontSize: 44 }}>🕵️</Text>
              <Text style={{ color: c.textSecondary, marginTop: 12 }}>검색 결과가 없어요</Text>
            </View>
          ) : null
        }
        renderItem={({ item }) => (
          <View style={[styles.card, { backgroundColor: c.card, borderColor: c.border }]}>
            <Text style={{ fontSize: 15, fontWeight: "700", color: c.text }}>{item.name}</Text>
            {!!(item.roadAddress || item.address) && (
              <Text style={{ fontSize: 13, color: c.textSecondary, marginTop: 4 }}>
                📍 {item.roadAddress ?? item.address}
              </Text>
            )}
          </View>
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingHorizontal: Spacing.lg, paddingTop: Spacing.md, gap: Spacing.md },
  headerTitle: { fontSize: 24, fontWeight: "800" },
  searchRow: { flexDirection: "row", gap: Spacing.sm },
  input: {
    flex: 1,
    borderWidth: 1,
    borderRadius: Radius.button,
    paddingHorizontal: Spacing.lg,
    paddingVertical: 12,
    fontSize: 15,
  },
  searchBtn: {
    width: 48,
    borderRadius: Radius.button,
    alignItems: "center",
    justifyContent: "center",
  },
  card: { borderRadius: Radius.card, borderWidth: 1, padding: Spacing.lg },
  empty: { alignItems: "center", marginTop: 80 },
});