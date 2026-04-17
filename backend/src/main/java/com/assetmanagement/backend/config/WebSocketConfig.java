package com.assetmanagement.backend.config;

import com.assetmanagement.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        // Extract JWT token from cookie during SockJS handshake
                        if (request instanceof ServletServerHttpRequest servletRequest) {
                            HttpServletRequest httpRequest = servletRequest.getServletRequest();
                            String token = resolveTokenFromRequest(httpRequest);
                            if (token != null && jwtUtil.validateToken(token)) {
                                String email = jwtUtil.getEmailFromToken(token);
                                attributes.put("email", email);
                                log.debug("WebSocket handshake: authenticated user {}", email);
                            }
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }

                    private String resolveTokenFromRequest(HttpServletRequest request) {
                        // 1. Try cookie first
                        if (request.getCookies() != null) {
                            for (Cookie cookie : request.getCookies()) {
                                if ("jwt_token".equals(cookie.getName())) {
                                    return cookie.getValue();
                                }
                            }
                        }
                        // 2. Fallback to Authorization header
                        String bearer = request.getHeader("Authorization");
                        if (bearer != null && bearer.startsWith("Bearer ")) {
                            return bearer.substring(7);
                        }
                        return null;
                    }
                })
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Option A: token passed via STOMP CONNECT header (Authorization: Bearer <token>)
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    log.debug("WebSocket CONNECT: Authorization header present={}", authToken != null);

                    String resolvedEmail = null;

                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);
                        if (jwtUtil.validateToken(token)) {
                            resolvedEmail = jwtUtil.getEmailFromToken(token);
                        }
                    }

                    // Option B: email pre-resolved from cookie during SockJS handshake
                    if (resolvedEmail == null) {
                        Object emailAttr = accessor.getSessionAttributes() != null
                                ? accessor.getSessionAttributes().get("email")
                                : null;
                        if (emailAttr instanceof String) {
                            resolvedEmail = (String) emailAttr;
                        }
                    }

                    if (resolvedEmail != null) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(resolvedEmail, null, List.of());
                        accessor.setUser(auth);
                        log.info("WebSocket CONNECT authorized: user={}", resolvedEmail);
                    } else {
                        log.warn("WebSocket CONNECT: No valid auth token found. Connection allowed but unauthenticated.");
                    }
                }
                return message;
            }
        });
    }
}
