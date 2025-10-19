export type TagDto = {
  id: number;
  keyword: string;
  createdAt: string;
};

export type PinDto = {
  id: number;
  latitude: number;
  longitude: number;
  content: string;
  userId: number;
  likeCount: number;
  isPublic: boolean;
  createdAt: string;
  modifiedAt: string;
  // 아래는 프론트에서 보강 (옵션)
  tags?: string[];         // /api/tags/filter 결과에 포함 (문자열 배열)
  _tagsLoaded?: boolean;   // 개별 핀 태그를 /pins/{id}/tags로 가져왔는지 표시
};

export type LikeToggleResponse = {
  isLiked: boolean;
  likeCount: number;
};

export type BookmarkDto = {
  id: number;
  pin: PinDto;
  createdAt: string;
};
