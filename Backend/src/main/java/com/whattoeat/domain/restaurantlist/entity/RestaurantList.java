package com.whattoeat.domain.restaurantlist.entity;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant_list",
    indexes = {
        @Index(name = "idx_restaurant_list_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor
public class RestaurantList extends BaseEntity {
    // 리스트 만든 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 리스트 제목
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 리스트 설명
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    // 분위기 태그
    @Column(name = "mood_tag", length = 50)
    private MoodTag moodTag;

    // 수정일
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "restaurantList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RestaurantListItem> items = new ArrayList<>();

    public RestaurantList(User user, String title, String description, MoodTag moodTag) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.moodTag = moodTag;
    }

    public void update(String title, String description, MoodTag moodTag) {
        this.title = title;
        this.description = description;
        this.moodTag = moodTag;
    }
}
