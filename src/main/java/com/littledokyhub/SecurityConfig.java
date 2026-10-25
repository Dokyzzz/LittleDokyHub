package com.littledokyhub;

import com.littledokyhub.repository.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
                return new LegacyAwarePasswordEncoder();
        }

        @Bean
        UserDetailsService users(UsuarioRepository repo) {
                return email -> repo.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                                .map(u -> User.withUsername(u.getEmail()).password(u.getPassword())
                                                .roles(u.getRol().getNombre())
                                                .build())
                                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
        }

        @Bean
        SecurityFilterChain security(HttpSecurity http, UsuarioRepository repo, PasswordEncoder passwordEncoder) throws Exception {
                return http.authorizeHttpRequests(a -> a
                                .requestMatchers("/administracion", "/administracion/**").hasRole("ADMIN")
                                .requestMatchers("/checkout", "/checkout/**", "/pedidos/**", "/cuenta", "/cuenta/**")
                                .authenticated()
                                .anyRequest().permitAll())
                                .formLogin(f -> f.loginPage("/login").usernameParameter("email")
                                                .successHandler((request, response, authentication) -> {
                                                        String submittedPassword = request.getParameter("password");
                                                        if (submittedPassword != null) {
                                                                repo.findByEmail(authentication.getName())
                                                                                .filter(usuario -> passwordEncoder
                                                                                                .upgradeEncoding(usuario
                                                                                                                .getPassword()))
                                                                                .ifPresent(usuario -> {
                                                                                        usuario.setPassword(passwordEncoder
                                                                                                        .encode(submittedPassword));
                                                                                        repo.save(usuario);
                                                                                });
                                                        }
                                                        boolean admin = authentication.getAuthorities().stream()
                                                                        .anyMatch(a -> "ROLE_ADMIN"
                                                                                        .equals(a.getAuthority()));
                                                        response.sendRedirect(request.getContextPath()
                                                                        + (admin ? "/administracion" : "/categorias"));
                                                }).permitAll())
                                .logout(l -> l.logoutUrl("/logout").deleteCookies("DOKY_CART", "JSESSIONID")
                                                .logoutSuccessUrl("/login?logout"))
                                .build();
        }
}
