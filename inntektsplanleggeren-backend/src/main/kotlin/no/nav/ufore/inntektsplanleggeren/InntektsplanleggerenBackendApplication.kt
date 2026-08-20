package no.nav.ufore.inntektsplanleggeren

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

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

	val secretsFile = File("/tmp/inntektsplanleggeren.env")

	if (isLocal && !secretsFile.exists()) {
		ProcessBuilder("./inntektsplanleggeren-backend/fetch-secrets.sh")
			.inheritIO()
			.start()
			.waitFor()
	}
}