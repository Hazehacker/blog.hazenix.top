package top.hazenix.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.hazenix.constant.RecommendConstants;
import top.hazenix.entity.UserBehavior;
import top.hazenix.mapper.UserBehaviorMapper;
import top.hazenix.service.impl.UserBehaviorServiceImpl;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserBehaviorServiceImplTest {

    private UserBehaviorMapper mapper;
    private UserBehaviorServiceImpl service;

    @BeforeEach
    public void setUp() {
        mapper = mock(UserBehaviorMapper.class);
        service = new UserBehaviorServiceImpl(mapper);
    }

    @Test
    public void recordView_should_insert_behavior_with_view_type() {
        service.recordView(1L, 100L, 45);
        verify(mapper).insert(argThat(b ->
                b.getUserId().equals(1L) &&
                b.getArticleId().equals(100L) &&
                b.getBehaviorType().equals(RecommendConstants.BEHAVIOR_VIEW) &&
                b.getDuration().equals(45)
        ));
    }

    @Test
    public void getUserArticleScore_should_return_max_score() {
        UserBehavior view = new UserBehavior();
        view.setBehaviorType(RecommendConstants.BEHAVIOR_VIEW);
        view.setDuration(10); // short read = 1

        UserBehavior like = new UserBehavior();
        like.setBehaviorType(RecommendConstants.BEHAVIOR_LIKE); // = 3

        when(mapper.getByUserIdAndArticleId(1L, 100L)).thenReturn(Arrays.asList(view, like));

        double score = service.getUserArticleScore(1L, 100L);
        assertEquals(3.0, score);
    }

    @Test
    public void getUserArticleScore_should_return_2_for_long_read() {
        UserBehavior view = new UserBehavior();
        view.setBehaviorType(RecommendConstants.BEHAVIOR_VIEW);
        view.setDuration(60); // > 30s = 2

        when(mapper.getByUserIdAndArticleId(1L, 100L)).thenReturn(Collections.singletonList(view));

        double score = service.getUserArticleScore(1L, 100L);
        assertEquals(2.0, score);
    }

    @Test
    public void getUserArticleScore_should_return_0_when_no_behaviors() {
        when(mapper.getByUserIdAndArticleId(1L, 100L)).thenReturn(Collections.emptyList());
        assertEquals(0.0, service.getUserArticleScore(1L, 100L));
    }
}