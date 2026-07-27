package com.whattoeat.global.exception;

import com.whattoeat.global.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유저를 찾지 못한 경우 404 반환
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 피드를 찾지 못한 경우 404 반환
    @ExceptionHandler(FeedNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFeedNotFound(FeedNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 댓글을 찾지 못한 경우 404 반환
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 식당을 찾지 못한 경우 404 반환
    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRestaurantNotFound(RestaurantNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 맛집 리스트를 찾지 못한 경우 404 반환
    @ExceptionHandler(ListNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleListNotFound(ListNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 알림을 찾지 못한 경우 404 반환
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(NotificationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // @Valid 검증 실패 시 첫 번째 필드 오류 메시지를 400으로 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
    }

    // 팔로우 관계를 찾지 못한 경우 404 반환
    @ExceptionHandler(FollowNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFollowNotFound(FollowNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 좋아요 관계를 찾지 못한 경우 404 반환
    @ExceptionHandler(FeedLikeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFeedLikeNotFound(FeedLikeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 이미 팔로우 중인 경우 409 반환
    @ExceptionHandler(AlreadyFollowingException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyFollowing(AlreadyFollowingException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }

    // 이미 좋아요한 피드인 경우 409 반환
    @ExceptionHandler(AlreadyLikedFeedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyLikedFeed(AlreadyLikedFeedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }

    // 자기 자신을 팔로우하려는 경우 400 반환
    @ExceptionHandler(SelfFollowNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleSelfFollowNotAllowed(SelfFollowNotAllowedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "'" + e.getName() + "'의 값이 올바르지 않습니다: " + e.getValue();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        String message = "'" + e.getParameterName() + "' 파라미터가 필요합니다.";
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, "데이터 무결성 오류가 발생했습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
    }
    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLoginId(DuplicateLoginIdException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }
    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNickname(DuplicateNicknameException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
    @ExceptionHandler(KakaoApiException.class)
    public ResponseEntity<ErrorResponse> handleKakaoApiException(KakaoApiException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(HttpStatus.BAD_GATEWAY, e.getMessage()));
    }

    // 맛집 리스트 아이템을 찾지 못한 경우 404 반환
    @ExceptionHandler(RestaurantListItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRestaurantListItemNotFound(RestaurantListItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    // 맛집 리스트에 이미 같은 식당이 있는 경우 409 반환
    @ExceptionHandler(DuplicateRestaurantListItemException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRestaurantListItem(DuplicateRestaurantListItemException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }

    // 이미 저장한 식당리스트인 경우 409 반환
    @ExceptionHandler(AlreadySavedRestaurantListException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyLikedFeed(AlreadySavedRestaurantListException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage()));
    }

    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageFormat(InvalidImageFormatException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}
