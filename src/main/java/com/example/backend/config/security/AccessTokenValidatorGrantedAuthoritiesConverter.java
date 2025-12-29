package com.example.backend.config.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class AccessTokenValidatorGrantedAuthoritiesConverter {

    private final JwtGrantedAuthoritiesConverter delegate;

    public AccessTokenValidatorGrantedAuthoritiesConverter() {
        delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthorityPrefix("");
        // delegate.setAuthoritiesClaimName("permission");
        delegate.setAuthoritiesClaimName("authorities");
    }

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String type = jwt.getClaimAsString("type");
        if (!"access".equals(type)) {
            throw new JwtException("Không thể dùng refresh token để gọi API");
        }
        return delegate.convert(jwt);
    }
}
