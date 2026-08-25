import { useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { useColorScheme } from "react-native";
import { useAuth } from "@/context/AuthContext";
import { Colors } from "@/constants/theme";

export default function LoginScreen() {
  const { login } = useAuth();
  const scheme = useColorScheme();
  const dark = scheme === "dark";
  const c = Colors[dark ? "dark" : "light"];

  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async () => {
    if (!loginId.trim() || !password) {
      setError("아이디와 비밀번호를 입력해주세요.");
      return;
    }
    setSubmitting(true);
    setError(null);
    const msg = await login({ loginId: loginId.trim(), password });
    if (msg) setError(msg);
    setSubmitting(false);
  };

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: c.background }]}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <View style={styles.inner}>
        <Text style={[styles.title, { color: c.text }]}>WhatToEat</Text>
        <Text style={[styles.subtitle, { color: c.textSecondary }]}>뭐 먹지?</Text>

        <TextInput
          style={[styles.input, { borderColor: c.border, color: c.text }]}
          placeholder="아이디"
          placeholderTextColor={c.textSecondary}
          autoCapitalize="none"
          value={loginId}
          onChangeText={setLoginId}
        />
        <TextInput
          style={[styles.input, { borderColor: c.border, color: c.text }]}
          placeholder="비밀번호"
          placeholderTextColor={c.textSecondary}
          secureTextEntry
          value={password}
          onChangeText={setPassword}
        />

        {error && <Text style={{ color: c.danger, marginBottom: 8 }}>{error}</Text>}

        <Pressable
          style={[styles.button, { backgroundColor: c.primary, opacity: submitting ? 0.6 : 1 }]}
          onPress={submit}
          disabled={submitting}
        >
          {submitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>로그인</Text>}
        </Pressable>

        <Text style={[styles.note, { color: c.textSecondary }]}>
          ⚠️ 앱은 "앱용 API"(토큰을 JSON으로 반환)가 백엔드에 추가된 뒤 정식 연동됩니다.
        </Text>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  inner: { flex: 1, justifyContent: "center", padding: 24, gap: 12 },
  title: { fontSize: 36, fontWeight: "800", textAlign: "center" },
  subtitle: { fontSize: 18, textAlign: "center", marginBottom: 32 },
  input: {
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 16,
  },
  button: {
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: "center",
    marginTop: 8,
  },
  buttonText: { color: "#fff", fontSize: 17, fontWeight: "700" },
  note: { fontSize: 12, textAlign: "center", marginTop: 16, lineHeight: 18 },
});