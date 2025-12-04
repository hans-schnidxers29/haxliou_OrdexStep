package com.example.demo.Seguridad;

import com.example.demo.Login.Servicio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class Securityconfigure {

    @Autowired
    private ServicioUsuario usuarioServicio;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;  // ✅ Cambiar aquí

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(usuarioServicio);
        auth.setPasswordEncoder(passwordEncoder);  // ✅ Ya no necesitas el cast
        return auth;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔹 Autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/StyleLogin/**", "/IMG/**", "/js/**").permitAll()
                        .requestMatchers("/registro**", "/js/**", "/css/**", "/img/**","/h2-console/**","/registro/nuevo").permitAll()
                        .anyRequest().authenticated()
                )

                // 🔹 Configuración del login
                .formLogin(form -> form
                        .loginPage("/login")      // Página personalizada de login
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // 🔹 Configuración del logout
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}