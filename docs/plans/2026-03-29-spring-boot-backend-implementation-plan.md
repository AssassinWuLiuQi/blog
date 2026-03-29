# Spring Boot Backend Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a complete Spring Boot 3.3 backend for The Scholar's Manuscript blog with JWT auth, article CRUD, categories, and user settings.

**Architecture:** Layered architecture (Controller → Service → Repository → Database). Spring Security 6.x with JWT access/refresh tokens. MySQL 8.x via Spring Data JPA.

**Tech Stack:** Spring Boot 3.3.x, Java 21, Spring Security 6.x, Spring Data JPA, MySQL 8.x, JWT 0.12.x, Lombok, MapStruct

---

## Phase 1: Project Foundation

### Task 1: Create Maven Project Structure

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/scholarsmanuscript/ScholarsManuscriptApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`

**Step 1: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.scholarsmanuscript</groupId>
    <artifactId>backend</artifactId>
    <version>1.0.0</version>
    <name>Scholars Manuscript Backend</name>
    <description>Spring Boot Backend for The Scholar's Manuscript Blog</description>

    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.5</jjwt.version>
        <mapstruct.version>3.3.0</mapstruct.version>
        <lombok.version>1.18.30</lombok.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 2: Create application.yml**

```yaml
spring:
  application:
    name: scholars-manuscript
  datasource:
    url: jdbc:mysql://localhost:3306/scholars_manuscript?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: root
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-long-enough-for-hs256}
  expiration: 900000
  refresh-expiration: 604800000

app:
  cors:
    allowed-origins: http://localhost:5173
```

**Step 3: Create application-dev.yml**

```yaml
spring:
  jpa:
    show-sql: true
  h2:
    console:
      enabled: false

jwt:
  secret: dev-secret-key-for-local-development-only-must-be-at-least-256-bits
```

**Step 4: Create main application class**

```java
package com.scholarsmanuscript;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScholarsManuscriptApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScholarsManuscriptApplication.class, args);
    }
}
```

**Step 5: Verify project compiles**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS (no output on success with -q flag)

---

### Task 2: Create Entity Classes

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/entity/User.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/entity/Article.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/entity/Category.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/entity/ArticleCategory.java`

**Step 1: Create User.java**

```java
package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String theme;

    @Column(length = 50)
    private String font;

    @Column(name = "voice_setting", length = 50)
    private String voiceSetting;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Step 2: Create Category.java**

```java
package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;
}
```

**Step 3: Create ArticleCategory.java (Join Table)**

```java
package com.scholarsmanuscript.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleCategory implements Serializable {

    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "category_id")
    private Long categoryId;
}
```

**Step 4: Create Article.java**

```java
package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    public enum Status {
        DRAFT, PUBLISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String excerpt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ArticleCategory> articleCategories = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public void addCategory(Category category) {
        ArticleCategory articleCategory = ArticleCategory.builder()
                .articleId(this.id)
                .categoryId(category.getId())
                .build();
        this.articleCategories.add(articleCategory);
    }

    public void removeCategory(Category category) {
        this.articleCategories.removeIf(ac -> ac.getCategoryId().equals(category.getId()));
    }
}
```

**Step 5: Verify entity compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

---

### Task 3: Create Repository Interfaces

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/repository/UserRepository.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/repository/ArticleRepository.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/repository/CategoryRepository.java`

**Step 1: Create UserRepository.java**

```java
package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
```

**Step 2: Create CategoryRepository.java**

```java
package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
```

**Step 3: Create ArticleRepository.java**

```java
package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByStatus(Article.Status status, Pageable pageable);

    Page<Article> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.articleCategories ac WHERE ac.categoryId = :categoryId")
    Page<Article> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword%")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    List<Article> findLatestPublished(Pageable pageable);

    @Query("SELECT YEAR(a.publishedAt) as year FROM Article a WHERE a.status = 'PUBLISHED' AND a.publishedAt IS NOT NULL GROUP BY YEAR(a.publishedAt)")
    List<Integer> findPublishedYears();
}
```

**Step 4: Verify repository compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

---

### Task 4: Create Security Configuration

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/security/UserDetailsServiceImpl.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/config/JwtConfig.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/config/CorsConfig.java`

**Step 1: Create JwtConfig.java**

```java
package com.scholarsmanuscript.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    private String secret;
    private long expiration;
    private long refreshExpiration;
}
```

**Step 2: Create JwtTokenProvider.java**

```java
package com.scholarsmanuscript.security;

import com.scholarsmanuscript.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), jwtConfig.getExpiration());
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), jwtConfig.getRefreshExpiration());
    }

    public String generateAccessToken(String username) {
        return generateToken(username, jwtConfig.getExpiration());
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, jwtConfig.getRefreshExpiration());
    }

    private String generateToken(String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
```

**Step 3: Create UserDetailsServiceImpl.java**

```java
package com.scholarsmanuscript.security;

import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
```

**Step 4: Create JwtAuthenticationFilter.java**

```java
package com.scholarsmanuscript.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**Step 5: Create CorsConfig.java**

```java
package com.scholarsmanuscript.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**Step 6: Create SecurityConfig.java**

```java
package com.scholarsmanuscript.config;

import com.scholarsmanuscript.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(new CorsConfig().corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

**Step 7: Verify security compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

---

## Phase 2: DTOs

### Task 5: Create Auth DTOs

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/LoginRequest.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/RegisterRequest.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/AuthResponse.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/UserResponse.java`

**Step 1: Create LoginRequest.java**

```java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
```

**Step 2: Create RegisterRequest.java**

```java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
```

**Step 3: Create UserResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
}
```

**Step 4: Create AuthResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private UserResponse user;
}
```

---

### Task 6: Create Article DTOs

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/ArticleRequest.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/ArticleResponse.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/ArticleDetailResponse.java`

**Step 1: Create ArticleRequest.java**

```java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ArticleRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    private String content;

    @Size(max = 500, message = "Excerpt must be less than 500 characters")
    private String excerpt;

    private String status;

    private List<Long> categoryIds;
}
```

**Step 2: Create ArticleResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private Long id;
    private String title;
    private String excerpt;
    private String status;
    private String category;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String readTime;
    private List<String> categories;
}
```

**Step 3: Create ArticleDetailResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String status;
    private String author;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<String> categories;
}
```

---

### Task 7: Create User and Category DTOs

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/UserSettingsRequest.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/UserSettingsResponse.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/CategoryResponse.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/ApiResponse.java`

**Step 1: Create ApiResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
```

**Step 2: Create UserSettingsRequest.java**

```java
package com.scholarsmanuscript.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettingsRequest {

    private String theme;
    private String font;
    private String voiceSetting;
}
```

**Step 3: Create UserSettingsResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {

    private String theme;
    private String font;
    private String voiceSetting;
}
```

**Step 4: Create CategoryResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
}
```

---

## Phase 3: Exception Handling

### Task 8: Create Exception Classes

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/exception/BusinessException.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/exception/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/exception/ErrorCode.java`

**Step 1: Create ErrorCode.java**

```java
package com.scholarsmanuscript.exception;

public enum ErrorCode {

    USER_NOT_FOUND("USER_001", "User not found"),
    USERNAME_ALREADY_EXISTS("USER_002", "Username already exists"),
    EMAIL_ALREADY_EXISTS("USER_003", "Email already exists"),
    INVALID_CREDENTIALS("AUTH_001", "Invalid username or password"),
    TOKEN_EXPIRED("AUTH_002", "Token has expired"),
    TOKEN_INVALID("AUTH_003", "Token is invalid"),
    ARTICLE_NOT_FOUND("ARTICLE_001", "Article not found"),
    CATEGORY_NOT_FOUND("CATEGORY_001", "Category not found"),
    UNAUTHORIZED("AUTH_000", "Unauthorized access"),
    FORBIDDEN("AUTH_001", "Forbidden access");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

**Step 2: Create BusinessException.java**

```java
package com.scholarsmanuscript.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
```

**Step 3: Create GlobalExceptionHandler.java**

```java
package com.scholarsmanuscript.exception;

import com.scholarsmanuscript.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Invalid username or password"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Authentication failed"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(400)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }

    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND, ARTICLE_NOT_FOUND, CATEGORY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USERNAME_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_INVALID, UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
```

---

## Phase 4: Auth Module

### Task 9: Create AuthService and AuthController

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/service/AuthService.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/controller/AuthController.java`

**Step 1: Create AuthService.java**

```java
package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.LoginRequest;
import com.scholarsmanuscript.dto.request.RegisterRequest;
import com.scholarsmanuscript.dto.response.AuthResponse;
import com.scholarsmanuscript.dto.response.UserResponse;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.UserRepository;
import com.scholarsmanuscript.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUsername());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
```

**Step 2: Create AuthController.java**

```java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.LoginRequest;
import com.scholarsmanuscript.dto.request.RegisterRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.AuthResponse;
import com.scholarsmanuscript.dto.response.UserResponse;
import com.scholarsmanuscript.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken.replace("\"", ""));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        UserResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

---

## Phase 5: Article Module

### Task 10: Create ArticleService

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/service/ArticleService.java`

**Step 1: Create ArticleService.java**

```java
package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.ArticleRequest;
import com.scholarsmanuscript.dto.response.ArticleDetailResponse;
import com.scholarsmanuscript.dto.response.ArticleResponse;
import com.scholarsmanuscript.entity.Article;
import com.scholarsmanuscript.entity.Category;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.ArticleRepository;
import com.scholarsmanuscript.repository.CategoryRepository;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public Page<ArticleResponse> getArticles(int page, int size, String status, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Article> articles;
        if (categoryId != null) {
            articles = articleRepository.findByCategoryId(categoryId, pageable);
        } else if (status != null && !status.isEmpty()) {
            articles = articleRepository.findByStatus(Article.Status.valueOf(status.toUpperCase()), pageable);
        } else {
            articles = articleRepository.findAll(pageable);
        }

        return articles.map(this::mapToArticleResponse);
    }

    public ArticleDetailResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
        return mapToArticleDetailResponse(article);
    }

    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .status(Article.Status.valueOf(request.getStatus().toUpperCase()))
                .author(author)
                .build();

        if (article.getStatus() == Article.Status.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
                article.addCategory(category);
            }
        }

        Article savedArticle = articleRepository.save(article);
        return mapToArticleResponse(savedArticle);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        if (!article.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setExcerpt(request.getExcerpt());

        Article.Status newStatus = Article.Status.valueOf(request.getStatus().toUpperCase());
        if (newStatus == Article.Status.PUBLISHED && article.getStatus() != Article.Status.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        article.setStatus(newStatus);

        if (request.getCategoryIds() != null) {
            article.getArticleCategories().clear();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
                article.addCategory(category);
            }
        }

        Article updatedArticle = articleRepository.save(article);
        return mapToArticleResponse(updatedArticle);
    }

    @Transactional
    public void deleteArticle(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        if (!article.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        articleRepository.delete(article);
    }

    public Page<ArticleResponse> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleRepository.searchByKeyword(keyword, pageable).map(this::mapToArticleResponse);
    }

    private ArticleResponse mapToArticleResponse(Article article) {
        List<String> categories = article.getArticleCategories().stream()
                .map(ac -> {
                    Category cat = categoryRepository.findById(ac.getCategoryId()).orElse(null);
                    return cat != null ? cat.getName() : null;
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());

        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .excerpt(article.getExcerpt())
                .status(article.getStatus().name())
                .author(article.getAuthor().getUsername())
                .createdAt(article.getCreatedAt())
                .publishedAt(article.getPublishedAt())
                .categories(categories)
                .readTime(calculateReadTime(article.getContent()))
                .build();
    }

    private ArticleDetailResponse mapToArticleDetailResponse(Article article) {
        List<String> categories = article.getArticleCategories().stream()
                .map(ac -> {
                    Category cat = categoryRepository.findById(ac.getCategoryId()).orElse(null);
                    return cat != null ? cat.getName() : null;
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());

        return ArticleDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .excerpt(article.getExcerpt())
                .status(article.getStatus().name())
                .author(article.getAuthor().getUsername())
                .authorId(article.getAuthor().getId())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .categories(categories)
                .build();
    }

    private String calculateReadTime(String content) {
        if (content == null || content.isEmpty()) {
            return "1分钟";
        }
        int wordCount = content.length() / 500;
        return Math.max(1, wordCount) + "分钟";
    }
}
```

---

### Task 11: Create ArticleController

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/controller/ArticleController.java`

**Step 1: Create ArticleController.java**

```java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.ArticleRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ArticleDetailResponse;
import com.scholarsmanuscript.dto.response.ArticleResponse;
import com.scholarsmanuscript.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long category) {
        Page<ArticleResponse> articles = articleService.getArticles(page, size, status, category);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleById(@PathVariable Long id) {
        ArticleDetailResponse article = articleService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@Valid @RequestBody ArticleRequest request) {
        ArticleResponse article = articleService.createArticle(request);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request) {
        ArticleResponse article = articleService.updateArticle(id, request);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = articleService.searchArticles(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
```

---

## Phase 6: Category Module

### Task 12: Create CategoryService and CategoryController

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/service/CategoryService.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/controller/CategoryController.java`

**Step 1: Create CategoryService.java**

```java
package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.response.ArticleResponse;
import com.scholarsmanuscript.dto.response.CategoryResponse;
import com.scholarsmanuscript.entity.Category;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.ArticleRepository;
import com.scholarsmanuscript.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return mapToCategoryResponse(category);
    }

    public Page<ArticleResponse> getArticlesByCategory(String slug, int page, int size) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleRepository.findByCategoryId(category.getId(), pageable)
                .map(article -> ArticleResponse.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .excerpt(article.getExcerpt())
                        .status(article.getStatus().name())
                        .author(article.getAuthor().getUsername())
                        .createdAt(article.getCreatedAt())
                        .publishedAt(article.getPublishedAt())
                        .build());
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }
}
```

**Step 2: Create CategoryController.java**

```java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ArticleResponse;
import com.scholarsmanuscript.dto.response.CategoryResponse;
import com.scholarsmanuscript.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/{slug}/articles")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getArticlesByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = categoryService.getArticlesByCategory(slug, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
```

---

## Phase 7: User Settings Module

### Task 13: Create UserService and UserController

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/service/UserService.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/controller/UserController.java`

**Step 1: Create UserService.java**

```java
package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.UserSettingsRequest;
import com.scholarsmanuscript.dto.response.UserSettingsResponse;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserSettingsResponse getUserSettings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return mapToUserSettingsResponse(user);
    }

    @Transactional
    public UserSettingsResponse updateUserSettings(UserSettingsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getTheme() != null) {
            user.setTheme(request.getTheme());
        }
        if (request.getFont() != null) {
            user.setFont(request.getFont());
        }
        if (request.getVoiceSetting() != null) {
            user.setVoiceSetting(request.getVoiceSetting());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserSettingsResponse(updatedUser);
    }

    private UserSettingsResponse mapToUserSettingsResponse(User user) {
        return UserSettingsResponse.builder()
                .theme(user.getTheme())
                .font(user.getFont())
                .voiceSetting(user.getVoiceSetting())
                .build();
    }
}
```

**Step 2: Create UserController.java**

```java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.UserSettingsRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.UserSettingsResponse;
import com.scholarsmanuscript.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings() {
        UserSettingsResponse settings = userService.getUserSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateUserSettings(
            @RequestBody UserSettingsRequest request) {
        UserSettingsResponse settings = userService.updateUserSettings(request);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}
```

---

## Phase 8: Final Verification

### Task 14: Verify Full Build

**Step 1: Run full Maven build**

Run: `cd backend && mvn clean package -DskipTests`
Expected: BUILD SUCCESS

**Step 2: Verify main class can start (quick smoke test)**

Run: `cd backend && mvn spring-boot:run -q` (cancel after 10 seconds)
Expected: Application starts without immediate errors

---

**Plan complete and saved to `docs/plans/2026-03-29-spring-boot-backend-implementation-plan.md`.**

Two execution options:

**1. Subagent-Driven (this session)** — I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** — Open new session with executing-plans, batch execution with checkpoints

Which approach?
