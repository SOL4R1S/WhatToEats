package com.whattoeat.domain.notification.repository;

import com.whattoeat.domain.notification.entity.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"actor", "feed"})
    Page<Notification> findByReceiverIdOrderByIdDesc(Long receiverId, Pageable pageable);

    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);

    long countByReceiverIdAndReadFalse(Long receiverId);
}
