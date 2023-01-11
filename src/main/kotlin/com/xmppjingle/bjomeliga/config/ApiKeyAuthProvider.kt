package com.xmppjingle.bjomeliga.config

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class ApiKeyAuthProvider : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val customerId = authentication.principal.toString()
        val apiKey = authentication.credentials.toString()

        return UsernamePasswordAuthenticationToken(customerId, apiKey)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}
