package com.fondant.user.application;

import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.exception.PointError;
import com.fondant.user.presentation.dto.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserService userService;

    @Transactional
    public void addPoint(Long userId, int amount) {
        if (amount <= 0) throw new ApiException(PointError.INVALID_POINT_AMOUNT);
        UserEntity user = userService.findUserById(userId);

        user.changePoint(user.getPoint() + amount);
    }

    @Transactional
    public void usePoint(Long userId, Integer amount) {
        if (amount <= 0) throw new ApiException(PointError.INVALID_POINT_AMOUNT);
        UserEntity user = userService.findUserById(userId);

        if (user.getPoint() < amount) throw new ApiException(PointError.INSUFFICIENT_POINTS);

        user.changePoint(user.getPoint() - amount);
    }

    public PointResponse getPoint(Long userId) {
        UserEntity user = userService.findUserById(userId);
        return new PointResponse(user.getPoint());
    }
}
