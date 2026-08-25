package no.nav.ufore.inntektsplanleggeren

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InntektsplanleggerenBackendApplication

fun main(args: Array<String>) {
	fetchSecretsLokalt()
	runApplication<InntektsplanleggerenBackendApplication>(*args)
}

fun fetchSecretsLokalt() {
	val isLocal = System.getProperty("spring.profiles.active")
		?.split(",")
		?.contains("local") == true

	if (isLocal) {
		ProcessBuilder("./inntektsplanleggeren-backend/fetch-secrets.sh")
			.inheritIO()
			.start()
			.waitFor()
	}
}