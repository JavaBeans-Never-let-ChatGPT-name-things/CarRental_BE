package com.example.backend.config.filter;

import com.example.backend.repository.LogoutTokenRepository;
import com.example.backend.service.CustomUserDetailsService;
import com.example.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ApplicationContext context;
    private final LogoutTokenRepository logoutTokenRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
        }
        if (logoutTokenRepository.findByToken(token).isPresent())
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User has logged out.");
            return;
        }
        if (token!= null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            if (jwtService.validateToken(token)) {
                String userName = jwtService.extractUserName(token);
                UserDetails userDetails = context.getBean(CustomUserDetailsService.class).loadUserByUsername(userName);
                if (!userDetails.isEnabled())
                {
                    log.warn("User {} is disabled. Rejecting authentication.", userDetails.getUsername());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Account is disabled.");
                    return;
                }
                log.info("User {} has authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());

                UsernamePasswordAuthenticationToken authToken
                        = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
