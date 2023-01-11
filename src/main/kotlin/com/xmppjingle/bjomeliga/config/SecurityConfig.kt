package com.xmppjingle.bjomeliga.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Component
internal class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var apiKeyAuthFilter: ApiKeyAuthFilter

    @Throws(Exception::class)
    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(HttpMethod.OPTIONS).antMatchers(*AUTH_WHITELIST)
    }

    private val AUTH_WHITELIST = arrayOf(
        "/v2/**",
        "/configuration/**",
        "/swagger-resources/**",
        "/configuration/security",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/webjars/**",
        "/drawer/**",
        "/events/**",
        "/config/**",
        "/experiment/**"
    )

    @Throws(java.lang.Exception::class)
    override fun configure(http: HttpSecurity) {
        http.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeRequests()
            // Whitelisted endpoints that do not require authentication
            .antMatchers(*AUTH_WHITELIST).permitAll()
            // Endpoints that require authentication
            .antMatchers("/**/*").authenticated()
            .antMatchers("/**/*").denyAll()
    }

    @Bean
    fun api(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
    }
}