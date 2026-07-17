package matchuri.backend.global.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.MatchuriProperties.Auth;
import matchuri.backend.global.security.JwtAuthenticationFilter;
import matchuri.backend.global.security.MatchuriAccessDeniedHandler;
import matchuri.backend.global.security.MatchuriAuthenticationEntryPoint;
import matchuri.backend.global.security.MatchuriOAuth2AuthorizationRequestRepository;
import matchuri.backend.global.security.OAuth2AuthenticationFailureHandler;
import matchuri.backend.global.security.OAuth2AuthenticationSuccessHandler;
import matchuri.backend.global.security.RequiredAgreementAccessFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MatchuriProperties matchuriProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MatchuriAuthenticationEntryPoint authenticationEntryPoint;
    private final MatchuriAccessDeniedHandler accessDeniedHandler;
    private final MatchuriOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
    private final RequiredAgreementAccessFilter requiredAgreementAccessFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(formLogin -> formLogin
                        .loginPage("/admin/login")
                        .defaultSuccessUrl("/admin", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/login").permitAll()
                        .anyRequest().hasRole("ADMIN")
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Auth authProps = matchuriProperties.getAuth();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(realtimeAsyncRequestMatcher()).permitAll()
                        .requestMatchers(authProps.getPublicApiPatterns().toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.GET, authProps.getPublicGetApiPatterns().toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.POST, authProps.getPublicPostApiPatterns().toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, authProps.getPublicOptionsApiPatterns().toArray(String[]::new)).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization.authorizationRequestRepository(authorizationRequestRepository))
                        .successHandler(oauth2AuthenticationSuccessHandler)
                        .failureHandler(oauth2AuthenticationFailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requiredAgreementAccessFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private RequestMatcher realtimeAsyncRequestMatcher() {
        return request -> DispatcherType.ASYNC.equals(request.getDispatcherType())
                && HttpMethod.GET.matches(request.getMethod())
                && ("/api/v1/realtime/events".equals(request.getRequestURI())
                || request.getRequestURI().matches("^/api/v1/groups/[^/]+/realtime/events$"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        MatchuriProperties.Cors corsProps = matchuriProperties.getAuth().getCors();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
        configuration.setAllowedMethods(corsProps.getAllowedMethods());
        configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
        configuration.setExposedHeaders(corsProps.getExposedHeaders());
        configuration.setAllowCredentials(corsProps.isAllowCredentials());
        configuration.setMaxAge(corsProps.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
