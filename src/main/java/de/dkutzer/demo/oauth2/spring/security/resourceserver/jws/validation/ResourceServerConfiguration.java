package de.dkutzer.demo.oauth2.spring.security.resourceserver.jws.validation;

import java.util.Collection;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.StringUtils;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${security.oauth2.resource.id:}")
    private String clientId;
    @Value("${security.oauth2.resource.jwt.key-value:}")
    private String realmKey;


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/person/**").hasRole("DEMO_SPRING_SECURITY")
            .and().csrf().disable();
    }
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(clientId);

    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new KeycloakAccessTokenConverter();
        if(!StringUtils.isEmpty(realmKey)){
            converter.setVerifierKey(realmKey);
        }

        return converter;
    }

    //This is necesarry because the roles/authorities are packed in a claim "realm_access->roles".
    // Spring Security expects it in "authorities".
    //works only for Realm-Roles
    //if client-roles are needed, this needs to be modified
    private class KeycloakAccessTokenConverter extends JwtAccessTokenConverter {

        @Override
        public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
            OAuth2Authentication oAuth2Authentication = super.extractAuthentication(map);
            Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) oAuth2Authentication.getOAuth2Request().getAuthorities();
            if (map.containsKey("realm_access")) {
                Map<String, Object> realm_access = (Map<String, Object>) map.get("realm_access");
                if(realm_access.containsKey("roles")) {
                    ((Collection<String>) realm_access.get("roles")).forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
                }
            }
            return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(),oAuth2Authentication.getUserAuthentication());
        }
    }

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter accessTokenConverter) {
        return new JwtTokenStore(accessTokenConverter);
    }


}
