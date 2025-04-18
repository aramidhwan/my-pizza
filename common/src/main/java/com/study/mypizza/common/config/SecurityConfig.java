package com.study.mypizza.common.config;

import com.study.mypizza.common.handler.JwtAuthenticationEntryPoint;
import com.study.mypizza.common.filter.JwtAuthenticationFilter;
import com.study.mypizza.common.filter.LoginFilter;
import com.study.mypizza.common.handler.LoginFailureHandler;
import com.study.mypizza.common.handler.LoginSuccessHandler;
import com.study.mypizza.common.handler.JwtAccessDeniedHandler;
import com.study.mypizza.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터체인에 등록이 됨.
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
// @EnableGlobalMethodSecurity // deprecated 됨. EnableMethodSecurity 를 대신 사용하라고 한다.
// EnableMethodSecurity : 함수에 권한 처리 애노테이션을 붙일 수 있게 할지 말지에 관한 설정을 담당한다.
// securedEnabled => Secured 애노테이션 사용 여부
// prePostEnabled => PreAuthorize 어노테이션 사용 여부.
// 위 2개의 default는 true로 되어있다. 따라서 위의 두 애노테이션을 사용할 생각이라면 굳이 속성을 안 적어도 된다.

// @Secured("ROLE_ADMIN") : Controller의 메소드에 붙인다. ROLE_ADMIN 권한이 유저에게 있을 때만 실행하도록 할 수 있다.
// 단, @Secured는 권한만 체크하지 '로그인 사용자 식별'은 하지 않는다.(예:게시글 수정/삭제는 본인만 가능해야 함)
// 따라서 '로그인 사용자 식별' 같은 기능은 아래와 같은 어노테이션으로 간단히 해결할 수 있다.
// @PostAuthorize("isAuthenticated() and (( returnObject.name == principal.name ) or hasRole('ROLE_ADMIN'))")
// 따라서 이런 복잡한 권한 인증처리는 @Pre/PostAuthorize 로, 그렇지 않고 권한에 대한 인증만 필요할 땐 @Secured 를 사용하도록 하자.
// 결론 :
// @Secured는 간단한 설정만 가능(표현식 사용할 수 없음)
// @PreAuthroize, @PostAuthorize는 표현식 사용을 사용하여 디테일한 설정이 가능

// @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')") : Controller의 메소드에 붙인다. 메서드가 실행되기 전에 인증을 거친다.
// @PostAuthorize("hasRole('ROLE_MANAGER')") : Controller의 메소드에 붙인다. 메서드가 실행되고 나서 응답을 보내기 전에 인증을 거친다.
@Slf4j
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    @Value("${mypizza.cors.allowedOrigins}")
    private String allowedOrigins ;

    /**
     * 이 메서드는 정적 자원에 대해 보안을 적용하지 않도록 설정한다.
     * 정적 자원은 보통 HTML, CSS, JavaScript, 이미지 파일 등을 의미하며, 이들에 대해 보안을 적용하지 않음으로써 성능을 향상시킬 수 있다.
     */
    // WebSecurity 는 HttpSecurity 상위에 존재하며,
    // WebSecurity 의 ignoring 에 API나 URL을 등록하면,
    // Spring Security 의 필터 체인이 적용되지 않습니다.
    // 따라서 Spring security의 보호를 받을 수 없기 때문에
    // 아래와 같은 경고문구가 나오니 다른 방법으로 처리하는것을 권고한다.
    // You are asking Spring Security to ignore Mvc [pattern='/v3/api-docs/**'].
    // This is not recommended -- please use permitAll via HttpSecurity#authorizeHttpRequests instead.

    // WebSecurityCustomizer 설정보다 HttpSecurity 설정이 권장됨
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring()
//                // 아래에서 /css/**, /js/**, /images/** 걸러줌
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
//                ;
//    }

    // SecurityFilterChain에 Bean으로 등록하는 과정.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                // CSRF 공격 (https://jaykaybaek.tistory.com/29)
                // token을 사용하는 방식이기 때문에 csrf를 disable (교차 사이트 요청 위조)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS란? Cross-Origin Resource Sharing
                // 서로 다른 Origin끼리 요청을 주고받을 수 있게 정해둔 표준이다.
                // 백엔드와 프론트의 Origin이 다를 경우 사용해야 한다.
                // 예를 들어 UI 앱에서 서로 다른 도메인인 API를 호출할 시 CORS로 인해 기본적으로 차단됩니다.
                // 이는 대부분의 브라우저에서 구현되는 W3C의 스펙입니다.
                // ex) front.world.com ---(api호출)--> backend.world.com 호출 안됨
                // 이럴때는 backend.world.com에서 cors에 config.setAllowedOrigins("http://front.world.com") 처리해주어야 한다.

                // CORS설정 (아래 2가지를 해줘야 한다)
                // 1. Cors 커스텀 허용하기
                // 2. Cors 설정하기
                // 기본적으로 아무것도 설정하지 않으면, 모든 외부 요청에 대해서 CORS Block을 한다.
                // 그래서 CORS에 대한 설정을 커스텀해야하는데, 그렇게 하려면 커스텀을 허용해주어야한다.
                // http.cors() : CORS를 커스텀 하려면 이렇게 함
                // cors()를 실행하면 CorsFilter, CorsConfiguration등등을 활용하여 상세한 CORS설정을 해줄 수 있다고 한다.
                // 여기서는 CORS관련 설정파일을 따로 만들었다.(CorsConfig.java)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                // JWT(Json Web Token)를 사용할 때 formLogin을 비활성화하는 이유는, 다른 로그인 방식을 사용하기 때문입니다.
                // formLogin() 을 추가해줘야만 로그인 화면(기본값:localhost:8090/login)이 나온다.
                // formLogin을 disable 시키면 UsernamePasswordAuthenticationFilter 는 동작하지 않기 때문에 Custom Filter를 만들어줘야 한다.
                .formLogin(AbstractHttpConfigurer::disable)
//                .formLogin(form -> form
//                        .loginPage("/auth/loginPage")     // 기본값:localhost:8090/login
//                        .defaultSuccessUrl("/mainPage")
//                        .failureUrl("/loginFail")
//                        .usernameParameter("customerId")
//                        .passwordParameter("password")
//                        .loginProcessingUrl("/api/auth/sign-in")
//                        .permitAll()
//                )
                .httpBasic(AbstractHttpConfigurer::disable)
                // 권한정보 (인증이 아님!)
                // 이곳에 정의하는 CASE : Controller를 통하지 않고 화면에 직접 보여주는 url은 여기에서 hasRole()을 정의!!
                // Interceptor 권한검사는 2곳에서 가능하다.
                // FilterSecurityInterceptor 혹은 MethodSecurityInterceptor 이며 각각 필터, 메서드에서 검사한다.
                .authorizeHttpRequests(authorize  -> authorize
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                    .requestMatchers("/","/favicon.ico","/html/index","/common/layout").permitAll()
                    .requestMatchers("/html/auth/loginPage", "/html/auth/loginPage.html").permitAll()
                    .requestMatchers("/api/auth/logout", "/api/auth/sign-up", "/api/auth/sign-in", "/api/auth/refresh-token").permitAll()
                    .requestMatchers("/html/**").permitAll()
                    .anyRequest().authenticated()   // 그 외 인증 없이 접근X
                )
                // disable session (JWT 1회성 세션)
                // JWT의 상태정보를 저장하지 않는 stateless 한 특성이 있다.
                // 스프링 시큐리티는 기본적으로 인증이 필요한 요청에 대해 세션을 생성하고 관리하는 기능을 제공하는데,
                // JWT 인증 방식에서는 세셩을 생성할 필요가 없기 때문에 이 기능을 STATELESS로 관리한다
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
//                .logout(logoutConfig -> {logoutConfig
//                        .logoutRequestMatcher("/logout")
//                        .addLogoutHandler(logoutService)
//                        .logoutSuccessHandler(((request, response, authentication) -> {
//                            SecurityContextHolder.clearContext();
//                        }))
//                })
                // ExceptionTranslaterFilter는 FilterSecurityInterceptor에서 발생한 오류들을 가로채서 처리한다.
                // AuthenticationException과 AccessDeniedException 2개에 해당한다.
                // 그 이외의 오류는 ControllerAdvice를 통해서 해결한다.
                // ExceptionTranslaterFilter는 제공하는 기능이 많기 때문에 커스터마이징을 하지 않는 것을 권장한다.
                // AuthenticationException은 다시, 로그인을 해야 들어올 수 있어!라는 예외가 된다. (인증 실패)
                // AccessDeniedException은 네가 누구인지 아는데 여기에 접근할 수 없어!라는 예외가 된다. (권한(인가) 없음)
                .exceptionHandling(handler -> handler
                        // 인가(Authorization)가 실패했을 때 실행된다.
                        // 예를 들자면, ROLE_ADMIN 권한이 있는 사용자가 필요한 요청에 ROLE_USER 권한을 가진 사용자가 접근했을 때 실행된다.
                        .accessDeniedHandler(accessDeniedHandler)
                        // 인증(Authentication)이 실패했을 때 실행된다.
                        // 예를 들자면, 인증이 필요한 요청에 로그인하지 않은 사용자가 접근했을 때 실행된다.
                        // AuthenticationException || AccessDeniedException 발생 시 AuthenticationEntryPoint 에 자격 증명 요청을 위임하게 되는데,
                        // 스프링 시큐리티는 아래를 별도로 설정하지 않으면
                        // Http403ForbiddenEntryPoint를 사용한다. (그 안에서는 단순히 403 에러를 반환)
                        .authenticationEntryPoint(jwtEntryPoint)
                )
                // 로그인 화면에서 인증을 처리해 줄 Custom Filter를 등록해 준다.
                // 정확히는 AbstractAuthenticationProcessingFilter 로 먼저 들어오게 되고 doFilter 메서드에서
                // attemptAuthentication 메서드를 호출하게 되는데, 이 때 이를 상속하는 UsernamePasswordAuthenticationFilter가 사용된다.
                // 또는 UsernamePasswordAuthenticationFilter 를 상속받은 Custom Filter가 사용된다.
                // formLogin 방식을 disable 안 했으면 UsernamePasswordAuthenticationFilter가 자동으로 실행될텐데
                // SecurityConfig에서 formLogin을 disable 시켰으므로 본 Custom Filter를 만들어 그 자리에 등록해 주어야 한다.
                // 따라서 UsernamePasswordAuthenticationFilter 를 상속받은 LoginFilter 를 새로 만들어 그 자리에 등록해 준다.
                // formLogin 방식을 disable 했으므로 UsernamePasswordAuthenticationFilter 자리에 이를 상속받은 LoginFilter 를 등록한다.
                .addFilterAt(
                        new LoginFilter(authenticationConfiguration.getAuthenticationManager(), tokenProvider, loginSuccessHandler, loginFailureHandler),
                        UsernamePasswordAuthenticationFilter.class
                )

                // Request 마다 매번 new LoginFilter() 생성은 비효율적인가?? 그렇다면 -->
                // https://velog.io/@ksiisk99/SpringSecurity1

                // 로그인 인증 처리 Filter 앞에 JWT 토큰의 유효성 검사를 수행하는 JwtAuthenticationFilter 등록한다.
                // Spring Security filter 필터들 중 가장 먼저 거치는 UsernamePasswordAuthenticationFilter 보다도 앞에 둔다.
//                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), LoginFilter.class)
                ;
        // UsernamePasswordAuthenticationFilter 란? : Form based Authentication 방식으로 인증을 진행할 때 아이디, 패스워드 데이터를 파싱하여 인증 요청을 위임하는 필터
        return httpSecurity.build() ;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOriginList = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
        CorsConfiguration configuration = new CorsConfiguration();
        // setAllowCredentials() : true로 설정하면 Access-Control-Allow-Credentials 헤더가 설정된다.
        configuration.setAllowCredentials(true);    // 쿠키 허용
        // setAllowedOrigins() : 모든 Origin에서 접근이 가능하도록 해놨다. 이후 Origin 이 확정되면 변경해줘야 한다.
//        configuration.setAllowedOrigins(List.of("http://front.world.com")); // 접근 허용할 URL
        configuration.setAllowedOrigins(allowedOriginList); // 접근 허용할 URL https://my-pizza.io:90
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        // addExposedHeader() : 클라이언트에게 노출할 헤더 값을 설정
        configuration.addExposedHeader(HttpHeaders.AUTHORIZATION);
        configuration.addExposedHeader(TokenProvider.REFRESH_TOKEN_COOKIE);
        configuration.addExposedHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
        configuration.addExposedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        // addAllowedHeader() : 클라이언트가 전송할 수 있는 헤더 값을 설정
        configuration.addAllowedHeader("*");
//        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With", "Content-Type", "Authorization", "X-XSRF-token"));
        // setMaxAge() : 클라이언트가 다시 preflight 요청을 보내지 않아도 되는 시간을 설정
//        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // registerCorsConfiguration() : CORS 구성을 등록, “/**” 는 모든 경로에서 CORS가 적용되도록 설정한다.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // PasswordEncoder는 BCryptPasswordEncoder를 사용
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
