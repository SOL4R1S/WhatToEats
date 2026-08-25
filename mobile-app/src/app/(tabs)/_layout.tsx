import { Tabs } from "expo-router";
import { Text, useColorScheme } from "react-native";
import { Colors } from "@/constants/theme";

const ICONS: Record<string, string> = {
  feed: "🍜",
  hot: "🔥",
  search: "🔍",
  profile: "👤",
};

export default function TabsLayout() {
  const scheme = useColorScheme();
  const c = Colors[scheme === "dark" ? "dark" : "light"];

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: c.primary,
        tabBarInactiveTintColor: c.tabInactive,
        tabBarStyle: {
          backgroundColor: c.card,
          borderTopColor: c.border,
          height: 84,
          paddingBottom: 22,
          paddingTop: 8,
        },
        tabBarLabelStyle: { fontSize: 11, fontWeight: "600" },
      }}
    >
      <Tabs.Screen
        name="feed"
        options={{
          title: "피드",
          tabBarIcon: ({ color }) => <Text style={{ fontSize: 22, opacity: color === c.primary ? 1 : 0.55 }}>🍜</Text>,
        }}
      />
      <Tabs.Screen
        name="hot"
        options={{
          title: "핫플",
          tabBarIcon: () => <Text style={{ fontSize: 22 }}>🔥</Text>,
        }}
      />
      <Tabs.Screen
        name="search"
        options={{
          title: "검색",
          tabBarIcon: () => <Text style={{ fontSize: 22 }}>🔍</Text>,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: "마이",
          tabBarIcon: () => <Text style={{ fontSize: 22 }}>👤</Text>,
        }}
      />
    </Tabs>
  );
}