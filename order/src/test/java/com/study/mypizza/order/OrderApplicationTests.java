package com.study.mypizza.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}
	@Test
	public void testOrder() throws Exception {
		String pizzaNm = "페퍼로니피자" ;
		String regionNm = "강남구" ;
		String jsonResponse = "{\"customerId\": \"1\", " +
				"\"pizzaNm\": \""+pizzaNm+"\", " +
				"\"qty\": \"2\", " +
				"\"regionNm\": \""+regionNm+"\" }";

		mockMvc.perform(post("/orders")
						.contentType(MediaType.APPLICATION_JSON) // Json 타입으로 지정
						.content(jsonResponse) // jjson으로 내용 등록
				)
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.pizzaNm").value(pizzaNm))
				.andExpect(jsonPath("$.regionNm").value(regionNm))
				.andDo(print()); // 응답값 print
	}
}
