// 앱 디자인 토큰 — 웜 오렌지 컨셉 (모바일 우선)
export const Colors = {
  light: {
    text: "#191919",
    background: "#FAFAF8",
    card: "#ffffff",
    border: "#EDEAE5",
    primary: "#FF6B35",
    primarySoft: "#FFF0E8",
    primaryForeground: "#ffffff",
    textSecondary: "#8A877F",
    danger: "#E5484D",
    like: "#FF4D67",
    tabInactive: "#B8B5AD",
  },
  dark: {
    text: "#F5F4F1",
    background: "#121212",
    card: "#1E1E1C",
    border: "#2C2B28",
    primary: "#FF7E4D",
    primarySoft: "#33221A",
    primaryForeground: "#ffffff",
    textSecondary: "#9C9990",
    danger: "#FF6369",
    like: "#FF6B80",
    tabInactive: "#6E6B64",
  },
} as const;

export type ThemeColorName = keyof typeof Colors.light;

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
} as const;

export const Radius = { card: 16, button: 12, pill: 999 } as const;