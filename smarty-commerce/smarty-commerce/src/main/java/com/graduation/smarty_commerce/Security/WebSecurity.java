package com.graduation.smarty_commerce.Security;

import com.graduation.smarty_commerce.Service.UserService;
import com.graduation.smarty_commerce.io.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableMethodSecurity(
        prePostEnabled = true,   // enables @PreAuthorize, @PostAuthorize, etc.
        securedEnabled = true   // enables @Secured
        //jsr250Enabled = true     // enables @RolesAllowed, @PermitAll, @DenyAll
)
@Configuration
@EnableWebSecurity
public class WebSecurity {

    private final UserRepository userRepository;
    private final UserService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public WebSecurity(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {

        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
    }


    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {


        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);


        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();


        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager);

        authenticationFilter.setFilterProcessesUrl("/users/login");

        http.cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz
                        // Allow browser preflight requests without authentication.
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        // Accept both /users and /users/ for signup.
                        .requestMatchers(HttpMethod.POST, SecurityConstants.SIGN_UP_URL, SecurityConstants.SIGN_UP_URL + "/")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityConstants.VERIFICATION_EMAIL_URL)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_REQUEST_URL)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_URL)
                        .permitAll()
                        .requestMatchers(SecurityConstants.H2_CONSOLE)
                        .permitAll()
                        .requestMatchers(
                                "/api-docs",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .authenticationManager(authenticationManager).addFilter(authenticationFilter).addFilter(new AuthorizationFilter(authenticationManager, userRepository))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));



        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource(){




        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
