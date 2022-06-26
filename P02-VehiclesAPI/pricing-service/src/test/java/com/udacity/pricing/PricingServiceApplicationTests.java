package com.udacity.pricing;

import com.udacity.pricing.domain.price.Price;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PricingServiceApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testGetVehicleById(){
		Long id = 1L;
		String currency = "USD";
		ResponseEntity<Price> response =
				testRestTemplate.getForEntity(
						"http://localhost:"+port+
								"/services/price?vehicleId="+id,
						Price.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertEquals(response.getBody().getVehicleId(), id);
		assertEquals(response.getBody().getCurrency(), currency);
	}
}
