import { Pressable, StyleSheet, Text, useColorScheme, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useAuth } from "@/context/AuthContext";
import { Colors, Radius, Spacing } from "@/constants/theme";

export default function ProfileScreen() {
  const { user, logout } = useAuth();
  const scheme = useColorScheme();
  const dark = scheme === "dark";
  const c = Colors[dark ? "dark" : "light"];

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: c.background }]} edges={["top"]}>
      <View style={styles.header}>
        <Text style={[styles.headerTitle, { color: c.text }]}>마이 👤</Text>
      </View>

      <View style={[styles.card, { backgroundColor: c.card, borderColor: c.border }]}>
        <View style={[styles.avatar, { backgroundColor: c.primarySoft }]}>
          <Text style={{ fontSize: 34 }}>{user?.profileImage ? "📷" : "🙂"}</Text>
        </View>
        <Text style={[styles.nickname, { color: c.text }]}>{user?.nickname ?? "게스트"}</Text>
        {user?.email ? (
          <Text style={{ fontSize: 13, color: c.textSecondary }}>{user.email}</Text>
        ) : null}
      </View>

      <Pressable
        style={[styles.logoutBtn, { borderColor: c.danger }]}
        onPress={() => {
          void logout();
        }}
      >
        <Text style={{ color: c.danger, fontWeight: "700", fontSize: 15 }}>로그아웃</Text>
      </Pressable>

      <Text style={{ textAlign: "center", fontSize: 11, color: c.textSecondary, paddingHorizontal: Spacing.xl, lineHeight: 17 }}>
        WhatToEat v1.0.0 · 앱용 API 연동 후 프로필 수정·내 피드·알림 기능이 추가됩니다.
      </Text>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md },
  headerTitle: { fontSize: 24, fontWeight: "800" },
  card: {
    margin: Spacing.lg,
    borderRadius: Radius.card,
    borderWidth: 1,
    alignItems: "center",
    paddingVertical: 32,
    gap: Spacing.sm,
  },
  avatar: {
    width: 84,
    height: 84,
    borderRadius: 42,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 4,
  },
  nickname: { fontSize: 19, fontWeight: "800" },
  logoutBtn: {
    marginHorizontal: Spacing.lg,
    borderWidth: 1.5,
    borderRadius: Radius.button,
    paddingVertical: 14,
    alignItems: "center",
    marginBottom: Spacing.lg,
  },
});