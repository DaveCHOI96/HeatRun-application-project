plugins {
	java
	id("org.springframework.boot") version "3.5.14"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.main"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.kafka:spring-kafka")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:kafka")
	testImplementation("org.testcontainers:postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// ── JWT ──────────────────────────────────────────────────
	// Spring Security와 함께 직접 JWT 발급/검증할 때 필요
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	// ── PostGIS / 지리공간 ────────────────────────────────────
	// hibernate-spatial: Spring Boot 3.5가 Hibernate 버전을 관리하므로 버전 생략
	implementation("org.hibernate.orm:hibernate-spatial")
	// JTS: 프라이버시 존 반경 폴리곤 계산 (hibernate-spatial이 내부적으로 쓰는 geometry 모델)
	implementation("org.locationtech.jts:jts-core:1.20.0")
	// ── QueryDSL ─────────────────────────────────────────────
	// 히트맵 타일 동적 쿼리, 복합 조건 검색
	val querydslVersion = "7.1"
	implementation("io.github.openfeign.querydsl:querydsl-jpa:$querydslVersion")
	annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:$querydslVersion:jpa")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	// ── Firebase (FCM 푸시 알림) ──────────────────────────────
	implementation("com.google.firebase:firebase-admin:9.8.0")
	// ── AWS SDK v2 (S3 공유카드 / 이미지 업로드) ──────────────
	// v1(com.amazonaws)은 Java 21 비호환 → v2(software.amazon.awssdk) 사용
	implementation(platform("software.amazon.awssdk:bom:2.42.33"))
	implementation("software.amazon.awssdk:s3")
	implementation("software.amazon.awssdk:sts") // presigned URL 발급용
	// ── Swagger / OpenAPI ────────────────────────────────────
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")

// commons-lang3 transitive CVE 강제 패치
	implementation("org.apache.commons:commons-lang3:3.18.0")
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
