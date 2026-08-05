package com.millenniumitesp.productinventoryservice.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    public static JwtAuthenticationToken unauthenticated(String rawToken) {
        JwtAuthenticationToken token = new JwtAuthenticationToken(null, rawToken, null);
        token.setAuthenticated(false);
        return token;
    }

    public static JwtAuthenticationToken authenticated(Object principal, Collection<? extends GrantedAuthority> authorities) {
        JwtAuthenticationToken token = new JwtAuthenticationToken(principal, null, authorities);
        token.setAuthenticated(true);
        return token;
    }

    private JwtAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public Object getCredentials() { return credentials; }

    @Override
    public Object getPrincipal() { return principal; }
}