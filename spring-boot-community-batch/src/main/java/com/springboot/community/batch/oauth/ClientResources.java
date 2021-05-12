//package com.example.springbootcommunityweb.oauth;
//
//import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
//import org.springframework.boot.context.properties.NestedConfigurationProperty;
//import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
//
//public class ClientResources {
//
//    @NestedConfigurationProperty // 해당 필드가 중복으로 바인딩 된다 표시하는 어노테이션
//    private AuthorizationCodeResourceDetails client =
//            new AuthorizationCodeResourceDetails();
//
//    @NestedConfigurationProperty
//    private ResourceServerProperties resource =
//            new ResourceServerProperties();
//
//    public AuthorizationCodeResourceDetails getClient(){
//        return client;
//    }
//
//    public ResourceServerProperties getResource() {
//        return resource;
//    }
//}
