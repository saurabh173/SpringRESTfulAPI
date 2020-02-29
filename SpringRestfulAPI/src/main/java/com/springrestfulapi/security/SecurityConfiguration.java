package com.springrestfulapi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
public class SecurityConfiguration  extends WebSecurityConfigurerAdapter {

   @Autowired
   private Environment env;

	@Autowired
	  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {		
		auth
		.ldapAuthentication()
        .userSearchFilter("(sAMAccountName={0})")
        .contextSource(contextSource());

	  }
	
    @Bean
    public BaseLdapPathContextSource contextSource() {
        LdapContextSource bean = new LdapContextSource();
        bean.setUrl(env.getProperty("ldap.urls"));
        bean.setBase(env.getProperty("ldap.base"));
 
        bean.setUserDn(env.getProperty("ldap.admuserdn"));
        bean.setPassword(env.getProperty("ldap.admpassword"));
        bean.setPooled(true);
        bean.setReferral("follow");
        bean.afterPropertiesSet();
        return bean;
    }

	  @Override
	  public void configure(WebSecurity web) throws Exception {
	    web
	      .ignoring()
	         .antMatchers("/resources/**"); // #3
	  }

	  @Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	      .csrf().disable()
	      .authorizeRequests()
	        .antMatchers("/signup","/about").permitAll() // #4
	        .antMatchers("/admin/**").hasRole("ADMIN") // #6
	        .anyRequest().authenticated() // 7
	        .and()
	    .formLogin()  // #8
	        .loginPage("/login.jsp") // #9
	        //.defaultSuccessUrl("/welcome")
	        .permitAll(); // #5
	  }
}