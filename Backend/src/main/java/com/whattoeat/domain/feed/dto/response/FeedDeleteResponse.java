package com.whattoeat.domain.feed.dto.response;

public record FeedDeleteResponse(String message){
    public static  FeedDeleteResponse of(){
        return new FeedDeleteResponse("글이 성공적으로 삭제되었습니다.");
    }
}
