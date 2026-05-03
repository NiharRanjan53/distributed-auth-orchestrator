package com.mro.orchestrator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.lang.NonNull;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j // Added for better production debugging
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Skip filter if no Bearer token is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtUtils.getUserNameFromJwtToken(jwt);

            // 2. If we have a username and no current authentication in context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // This calls your ApplicationConfig bean which now maps Entity -> UserDetails (with roles)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 3. Validate token against the secret key
                if (jwtUtils.validateJwtToken(jwt)) {
                    // 4. Create authToken INCLUDING authorities (roles)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities() // CRITICAL: This enables @PreAuthorize checks
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Finalize the "Security Guard" check-in
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User {} authenticated with roles: {}", username, userDetails.getAuthorities());
                }
            }
        } catch (Exception e) {
            // In a product-based environment, we log the reason for failure (Expired, Malformed, etc.)
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}