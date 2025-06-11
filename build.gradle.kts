plugins {
	java
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.study"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")		// 서버 모니터링 도구
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")		// jpa
	implementation("org.springframework.boot:spring-boot-starter-security")		// security (jwt)
	implementation("org.springframework.boot:spring-boot-starter-validation")	// 데이터 유효성 검증
	implementation("org.springframework.boot:spring-boot-starter-web")			// RestController, Service

	// database
	implementation("org.flywaydb:flyway-core")		// 데이터베이스 마이그레이션 툴 (SQL 파일 사용)
	runtimeOnly("com.h2database:h2")				// 자바 기반의 오픈소스 관계형 데이터베이스 관리 시스템 (In-Memory)
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.flywaydb:flyway-mysql")

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

	// jwt
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Querydsl
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")

	compileOnly("org.projectlombok:lombok")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val querydslGeneratedDir = layout.buildDirectory.dir("generated/querydsl").get().asFile

tasks.withType<JavaCompile>().configureEach {
	options.generatedSourceOutputDirectory.set(file(querydslGeneratedDir))
}

sourceSets {
	main {
		java.srcDir(querydslGeneratedDir)
	}
}

tasks.named("clean") {
	doLast {
		file(querydslGeneratedDir).deleteRecursively()
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
