/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Integration Test for {@link CrashController}.
 *
 * @author Alex Lutz
 */
// NOT Waiting https://github.com/spring-projects/spring-boot/issues/5574
@SpringBootTest(webEnvironment = RANDOM_PORT,
		properties = { "server.error.include-message=ALWAYS", "management.endpoints.enabled-by-default=false" })
class CrashControllerIntegrationTests {

	@LocalServerPort
	private int port;

	@Test
	void testTriggerExceptionJson() {
		RestClient restClient = RestClient.create();
		ResponseEntity<Map<String, Object>> resp = restClient.get()
			.uri("http://localhost:" + port + "/oups")
			.retrieve()
			.onStatus(status -> true, (request, response) -> {
			})
			.toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
			});
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resp.getBody()).containsKey("timestamp");
		assertThat(resp.getBody()).containsKey("status");
		assertThat(resp.getBody()).containsKey("error");
		assertThat(resp.getBody()).containsEntry("path", "/oups");
	}

	@Test
	void testTriggerExceptionHtml() {
		RestClient restClient = RestClient.create();
		ResponseEntity<String> resp = restClient.get()
			.uri("http://localhost:" + port + "/oups")
			.accept(MediaType.TEXT_HTML)
			.retrieve()
			.onStatus(status -> true, (request, response) -> {
			})
			.toEntity(String.class);
		assertThat(resp).isNotNull();
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resp.getBody()).isNotNull();
		// html:
		assertThat(resp.getBody()).contains("Something happened...");
		// Not the whitelabel error page:
		assertThat(resp.getBody()).doesNotContain("Whitelabel Error Page",
				"This application has no explicit mapping for");
	}

	@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,
			DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
	static class TestConfiguration {

	}

}
