import { Redirect } from "expo-router";
import { useAuth } from "@/context/AuthContext";

export default function IndexRedirect() {
  const { token, initializing } = useAuth();
  if (initializing) return null;
  return <Redirect href={token ? "/(tabs)/feed" : "/login"} />;
}