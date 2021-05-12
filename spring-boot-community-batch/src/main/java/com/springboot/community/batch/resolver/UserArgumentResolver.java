package com.springboot.community.batch.resolver;

import com.springboot.community.batch.annotation.SocialUser;
import com.springboot.community.batch.domain.User;
import com.springboot.community.batch.domain.enums.SocialType;
import com.springboot.community.batch.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.springboot.community.batch.domain.enums.SocialType.*;

@Component
@SuppressWarnings("unchecked")
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    private UserRepository userRepository;

    public UserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        //MethodParameter로 해당 파라미터의 정보를 받아
        // "파라미터에 @SocilUser 어노테이션이 있고 타입이 User인 파라미터"만 true 반환
        return parameter.getParameterAnnotation(SocialUser.class) != null &&
                parameter.getParameterType().equals(User.class);
    }

    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer container, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        // 이미 검증되어 세션에 해당 User객체가 있으면 User객체 구성 로직을 수행하지 않도록 세션을 먼저 확인
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest().getSession();
        User user = (User) session.getAttribute("user");
        return getUser(user, session);
    }

    //인증된 User 객체를 만드는 메인 메서드
    public User getUser(User user, HttpSession session) {
        if (user == null) {
            try {
                OAuth2AuthenticationToken authentication =
                        (OAuth2AuthenticationToken) SecurityContextHolder
                        .getContext().getAuthentication();
                Map<String, Object> map = authentication.getPrincipal().getAttributes();
                User convertUser = convertUser(authentication.getAuthorizedClientRegistrationId(), map);
                user = userRepository.findByEmail(convertUser.getEmail());
                if (user == null) {
                    user = userRepository.save(convertUser);
                }
                setRoleIfNotSame(user, authentication, map);
                session.setAttribute("user", user);
            } catch (ClassCastException e){
                return user;
            }
        }
        return user;
    }


    //소셜 미디어 타입에 따라서 맞는 get~User함수 호출
    private User convertUser(String authority, Map<String, Object> map){
        if(FACEBOOK.getValue().equals(authority)) return getModernUser(FACEBOOK, map);
        else if (GOOGLE.getValue().equals(authority)) return getModernUser(GOOGLE,map);
        else if (KAKAO.getValue().equals(authority)) return getKakaoUser(map);
        return null;
    }

    private User getKakaoUser(Map<String, Object> map) {
        HashMap<String, String> propertyMap = (HashMap<String, String>) map.get("properties");
        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(String.valueOf(map.get("kaccount_email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(KAKAO)
                .createdDate(LocalDateTime.now())
                .build();
    }

    private User getModernUser(SocialType socialType, Map<String, Object> map) {
        return User.builder()
                .name(String.valueOf(map.get("name")))
                .email(String.valueOf(map.get("email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }

    // 인증된 authentication이 권한을 갖고 있는지 체크
    private void setRoleIfNotSame(User user, OAuth2AuthenticationToken authentication, Map<String, Object> map) {
        if(!authentication.getAuthorities().contains(
                new SimpleGrantedAuthority(user.getSocialType().getRoleType()))
        ){
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(map, "N/A",
                    AuthorityUtils.createAuthorityList(user.getSocialType().getRoleType()))
            );
        }
    }
}