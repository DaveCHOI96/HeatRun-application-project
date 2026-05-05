package com.main.heatrun.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
//Spring이 yml 값을 주입할 때 Setter를 통해 값을 넣음
//@Setter 없으면 값 주입 불가
@Setter
@Component
// 관련 설정을 클래스 하나로 묶어서 관리
@ConfigurationProperties(prefix = "jwt") // application.yml 의 jwt 아래 들을 자동으로 읽어서 필드에 주입
public class JwtProperties {

    // application.yml의 jwt.secret
    private String secret;

    private long accessTokenExpiration;

    private long refreshTokenExpiration;
}
