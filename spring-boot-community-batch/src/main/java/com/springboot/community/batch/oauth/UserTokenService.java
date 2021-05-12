//package com.example.springbootcommunityweb.oauth;
//
//import com.example.springbootcommunityweb.domain.enums.SocialType;
//import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
//import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
//
//import java.util.List;
//import java.util.Map;
//
//public class UserTokenService extends UserInfoTokenServices {
//    //User 정보를 얻어오기 위해 소셜 서버와 통신하는 역할 수행, URI와 clientID 필요
//    public UserTokenService(ClientResources resources, SocialType socialType){
//        //3개의 소셜 미디어 정보를 SocialType을 기준으로 관리하기 위해 super이용해서 인자로 받은 각 resources로 부터 소셜미디어정보 주입
//        // super는 부모 객체의미, 아래는 생성자인 것임
//        super(resources.getResource().getUserInfoUri(), resources.getClient().getClientId());
//        setAuthoritiesExtractor(new OAuth2AuthoriesExtractor(socialType));
//    }
//
//    private class OAuth2AuthoriesExtractor implements AuthoritiesExtractor {
//        private String socialType;
//
//
//
//        public OAuth2AuthoriesExtractor(SocialType socialType) {
//            //권한 생성 방식을 ROLE_FACEBOOK 처럼 하기 위함
//            this.socialType = socialType.getRoleType();
//        }
//        @Override
//        public List<GrantedAuthority> extractAuthorities(Map<String,Object> map){
//            // 권한을 리스트 형식으로 생성하여 반환하도록
//            return AuthorityUtils.createAuthorityList(this.socialType);
//        }
//    }
//}
//
