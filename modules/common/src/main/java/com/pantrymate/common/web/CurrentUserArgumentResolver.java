package com.pantrymate.common.web;

import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.common.exception.CommonErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        String userId = webRequest.getHeader(USER_ID_HEADER);
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        try {
            return new CurrentUser(Long.valueOf(userId));
        } catch (NumberFormatException e) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
