package com.svinarev.compiler.config;

import io.sentry.spring.EnableSentry;
import org.springframework.context.annotation.Configuration;

@EnableSentry(dsn = "${sentry.dsn}")
@Configuration
public class SentryConfig {

}
