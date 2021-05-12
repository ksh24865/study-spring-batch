package com.springboot.community.batch.config;

import com.springboot.community.batch.oauth2.CustomOAuth2Provider;


import com.springboot.community.batch.domain.enums.SocialType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
// 웹에서 시큐리티 기능을 사용하겠다는 어노테이션
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    // 자동설정 사용할 수도 있지만 요청,권한,기타 설정에 대해서는 필수적으로 최적화한 설정이 들어가야 함
    // 그러기 위해 configure 오버라이드해서 설정
    protected void configure(HttpSecurity http) throws Exception{
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        http
                //인증 메커니즘을, 요청한 HttpServletRequest 기반으로 설정
                .authorizeRequests()
                    //요청 패턴 설정 및 누구나 접근허용
                    .antMatchers("/","/oauth2/**","/login/**","/css/**","/images/**","/js/**","/console/**").permitAll()
                    .antMatchers("/facebook").hasAuthority(SocialType.FACEBOOK.getRoleType())
                    .antMatchers("/google").hasAuthority(SocialType.GOOGLE.getRoleType())
                    .antMatchers("/kakao").hasAuthority(SocialType.KAKAO.getRoleType())
                    // 설정한 요청 이외의 리퀘스트 요청 허용, 인증된 사용자만 요청가능
                    .anyRequest().authenticated()
                .and()
                    // oauth2Login()만 추가하면 구글,페이스북 OAuth2인증방식 기본적용, 인증시 /oauth2/** 접근하므로 권한허용필요
                    .oauth2Login()
                    .defaultSuccessUrl("/loginSuccess")
                    .failureUrl("/loginFailure")
                .and()
                    // 헤더 설정 안할경우 디폴트, XFrameOptionHeaderWriter의 최적화 설정 불허용
                    .headers().frameOptions().disable()
                .and()
                    .exceptionHandling()
                    //인증의 진입점, 인증되지 않은 사용자가 허용되지 않은 경로로 리퀘스트 요청 시 '/login'으로 이동시킴
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .and()
                    //로그인 설정
                    .formLogin()
                    //로그인 성공 시 설정 경로로 이동
                    .successForwardUrl("/board/list")
                .and()
                    //로그아웃 설정
                    .logout()
                    //로그아웃 수행될 url
                    .logoutUrl("/logout")
                    //로그아웃 이후 이동할 url
                    .logoutSuccessUrl("/")
                    //로그아웃 후 삭제할 쿠키값
                    .deleteCookies("JSESSIONID")
                    //설정된 세션 무효화 true
                    .invalidateHttpSession(true)
                .and()
                    //첫 번째 인자보다 먼저 시작될 필터 두번째 인자에 등록
                    .addFilterBefore(filter, CsrfFilter.class)
                    .csrf().disable();
    }

    @Bean
    // OAuth2ClientProperties와 CustomOAuth2Provider에서 설정했던 카카오 클라이언트 ID를 불러옴
    // @Configuration으로 등록되어 있는 클래스에서 @Bean으로 등록된 메서드의 파라미터로 지정된 객체들은 오토와이어 가능
    // oAuth2ClientProperties에는 페북과구글, @Value~~에는 카카오 수동으로 가져옴
    public ClientRegistrationRepository clientRegistrationRepository(
            OAuth2ClientProperties oAuth2ClientProperties, @Value(
            "${custom.oauth2.kakao.client-id}") String kakaoClientId) {
        List<ClientRegistration> registrations = oAuth2ClientProperties
                .getRegistration().keySet().stream()
                // getRegistration이용해 구글과 페북 인증정보 빌드업
                .map(client -> getRegistration(oAuth2ClientProperties, client))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        registrations.add(CustomOAuth2Provider.KAKAO.getBuilder("kakao")
                .clientId(kakaoClientId)
                .clientSecret("test")
                .jwkSetUri("test")
                .build());
        return new InMemoryClientRegistrationRepository(registrations);


    }

    private ClientRegistration getRegistration(OAuth2ClientProperties clientProperties, String client) {
        if ("google".equals(client)){
            OAuth2ClientProperties.Registration registration =
                    clientProperties.getRegistration().get("google");
            return CommonOAuth2Provider.GOOGLE.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .scope("email","profile")
                    .build();
        }
        if ("facebook".equals(client)){
            OAuth2ClientProperties.Registration registration =
                    clientProperties.getRegistration().get("facebook");
            return CommonOAuth2Provider.FACEBOOK.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    //페북의 그래프 API의 scope는 필요한 필드를 반환 안해줘서 직접 파라미터 넣어 요청
                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,link")
                    .scope("email")
                    .build();
        }
        return null;
    }


}
