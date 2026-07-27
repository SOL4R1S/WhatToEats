package com.whattoeat.domain.follow.service;

import com.whattoeat.global.exception.AlreadyFollowingException;
import com.whattoeat.global.exception.FollowNotFoundException;
import com.whattoeat.global.exception.SelfFollowNotAllowedException;
import com.whattoeat.domain.follow.entity.Follow;
import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        validateSelfFollow(followerId, followingId);

        User follower = getUser(followerId);
        User following = getUser(followingId);

        if (followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId)) {
            throw new AlreadyFollowingException();
        }

        return followRepository.save(Follow.of(follower, following));
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        getUser(followerId);
        getUser(followingId);

        Follow follow = followRepository.findByFollower_IdAndFollowing_Id(followerId, followingId)
                .orElseThrow(FollowNotFoundException::new);

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public Page<Follow> getFollowings(Long userId, Pageable pageable) {
        getUser(userId);
        return followRepository.findByFollower_Id(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Follow> getFollowers(Long userId, Pageable pageable) {
        getUser(userId);
        return followRepository.findByFollowing_Id(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        getUser(userId);
        return followRepository.countByFollowing_Id(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        getUser(userId);
        return followRepository.countByFollower_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
    }

    private void validateSelfFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new SelfFollowNotAllowedException();
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
