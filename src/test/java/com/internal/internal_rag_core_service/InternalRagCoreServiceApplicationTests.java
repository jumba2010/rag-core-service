package com.internal.internal_rag_core_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "XAI_API_KEY=test-key")
@Import(TestcontainersConfiguration.class)
class InternalRagCoreServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
