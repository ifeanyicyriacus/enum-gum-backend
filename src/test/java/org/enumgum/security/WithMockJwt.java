package org.enumgum.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory.class)
public @interface WithMockJwt {

    /** User ID placed in Authentication#getPrincipal() */
    String userId() default "00000000-0000-0000-0000-000000000000";

    /** Role (without "ROLE_" prefix) */
    String role() default "MEMBER";

    /** Org ID placed as a claim */
    String orgId() default "00000000-0000-0000-0000-000000000000";

    /** Email claim */
    String email() default "mock@enumgum.com";
}