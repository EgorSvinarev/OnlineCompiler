package com.svinarev.compiler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.ArrayList;

import com.svinarev.compiler.utils.FileHandler;
import com.svinarev.compiler.filters.WhitelistProcessingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
	
	@Value("${whitelist.file}")
	private String whitelistFile;
	
	@Autowired
	private FileHandler fileHandler;
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		WhitelistProcessingFilter whitelistFilter = new WhitelistProcessingFilter(whitelistFile, fileHandler);
		whitelistFilter.setAuthenticationManager(authenticationManagerBean());
		
		PreAuthenticatedAuthenticationProvider preAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthProvider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<>(whitelistFilter));
        http.authenticationProvider(preAuthProvider);
	
        http
        	.antMatcher("/compile")
    		.addFilterAt(whitelistFilter, AbstractPreAuthenticatedProcessingFilter.class);
        
		http.csrf().disable()
	    	.sessionManagement().sessionCreationPolicy(STATELESS)
	    .and()
	    	.authorizeRequests()
	    		.antMatchers("/swagger-ui/**").permitAll()
	    		.antMatchers("/compile").authenticated();
	    
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*");
    }
}
