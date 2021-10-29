package com.svinarev.compiler.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import com.svinarev.compiler.utils.FileHandler;

public class WhitelistProcessingFilter extends AbstractPreAuthenticatedProcessingFilter implements UserDetailsService {
	
	private Logger logger = LoggerFactory.getLogger(WhitelistProcessingFilter.class);
	private String[] allowedIP;
	private String whitelistFile;
	private FileHandler fileHandler;
	
	public WhitelistProcessingFilter(String whitelistFilePath, FileHandler fileHandler) {
		this.whitelistFile = whitelistFilePath;
		this.fileHandler = fileHandler;
		
		this.allowedIP = getWhitelistAddresses();
	}
	
	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		
		for (String ip: allowedIP) {
			if (new IpAddressMatcher(ip).matches(request)) {
				logger.debug("Remote IP is in the allowed list: {}", request.getRemoteAddr(), allowedIP);
				return request.getRemoteAddr();
			}
		}
		
		logger.debug("Remote IP {} isn't in the allowed list: {}", request.getRemoteAddr(), allowedIP);
		
		return null;
	}
	
	@Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }
	
	@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User(username, "N/A", Collections.emptyList());
    }

	
	private String[] getWhitelistAddresses() {
		String list = fileHandler.read(whitelistFile);
		
		String[] addresses = list.split("\n");
		return addresses;
	}
	
}
