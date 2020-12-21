package com.example.leak;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LeakApplicationTests {
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void leak() {
		for (int i = 0; i < 250; i++) {
			System.out.println("Request: " + (i + 1));

			String uri = "https://www.google.com/tia/tia.png"; // 200
			restTemplate.getForEntity("/leak?uri=" + uri, String.class);
		}
	}
}
