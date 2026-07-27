package com.whattoeat.domain.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.whattoeat.domain.comment.entity.Comment;
import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.config.JpaConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User createAndSaveUser(String loginId, String nickname, String email) {
        User user = User.builder()
                .loginId(loginId)
                .nickname(nickname)
                .email(email)
                .provider(Provider.LOCAL)
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Feed createAndSaveFeed(User user, String content) {
        Feed feed = Feed.builder()
                .user(user)
                .content(content)
                .build();
        return entityManager.persistAndFlush(feed);
    }

    private Comment createAndSaveComment(Feed feed, User user, String content) {
        Comment comment = Comment.builder()
                .feed(feed)
                .user(user)
                .content(content)
                .build();
        return entityManager.persistAndFlush(comment);
    }

    @Test
    void findByFeedId_성공() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");
        Feed feed = createAndSaveFeed(user, "피드 내용");
        createAndSaveComment(feed, user, "댓글1");
        createAndSaveComment(feed, user, "댓글2");

        List<Comment> result = commentRepository.findByFeedId(feed.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting("content").containsExactlyInAnyOrder("댓글1", "댓글2");
    }

    @Test
    void findByFeedId_다른_피드의_댓글은_조회되지_않는다() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");
        Feed feed1 = createAndSaveFeed(user, "피드1");
        Feed feed2 = createAndSaveFeed(user, "피드2");
        createAndSaveComment(feed1, user, "피드1 댓글");
        createAndSaveComment(feed2, user, "피드2 댓글");

        List<Comment> result = commentRepository.findByFeedId(feed1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("피드1 댓글");
    }

    @Test
    void findByFeedId_댓글이_없으면_빈_리스트_반환() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");
        Feed feed = createAndSaveFeed(user, "피드 내용");

        List<Comment> result = commentRepository.findByFeedId(feed.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void save_시_createdAt이_자동_설정된다() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");
        Feed feed = createAndSaveFeed(user, "피드 내용");
        Comment comment = createAndSaveComment(feed, user, "댓글");

        assertThat(comment.getCreatedAt()).isNotNull();
    }
}
