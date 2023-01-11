package com.xmppjingle.bjomeliga.service.ratelimit

import java.lang.annotation.Documented

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Documented
annotation class RateLimit(val value: String)
