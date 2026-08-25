// 백엔드 RsData 래퍼와 공통 DTO 타입 정의 (backend src/main/kotlin 스펙 기반)

export interface RsData<T> {
  code: string;
  message: string;
  data: T;
}

// ---- auth ----
export interface AuthUserResponse {
  id: number;
  loginId: string | null;
  nickname: string;
  profileImage: string | null;
  email: string | null;
}

export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface SignUpRequest {
  loginId: string;
  password: string;
  nickname: string;
}

// ---- user ----
export interface UserProfileResponse {
  id: number;
  loginId: string | null;
  nickname: string;
  profileImage: string | null;
  email: string | null;
  createdAt: string;
  following: boolean;
  followerCount: number;
  followingCount: number;
  feedCount: number;
}

// ---- feed ----
export enum MoodTag {
  NONE = "NONE",
  HAPPY = "HAPPY",
  SAD = "SAD",
  ANGRY = "ANGRY",
  NERVOUS = "NERVOUS",
  SENTIMENTAL = "SENTIMENTAL",
  ROMANTIC = "ROMANTIC",
  EXCITED = "EXCITED",
  LONELY = "LONELY",
  REFRESHED = "REFRESHED",
  CALM = "CALM",
  TIRED = "TIRED",
}

export interface FeedListResponse {
  feedId: number | null;
  content: string;
  imageUrl: string | null;
  userId: number | null;
  nickname: string;
  profileImage: string | null;
  likeCount: number;
  isLikedByMe: boolean;
  commentCount: number;
  restaurantId: number | null;
  restaurantName: string | null;
  moodTag: MoodTag | null;
  createdAt: string | null; // ISO LocalDateTime
}