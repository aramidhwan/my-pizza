package com.study.mypizza.store;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StoreApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}
	@Test
	public void testStore() throws Exception {
		String regionNm = "중구" ;
		String jsonResponse = "{\"regionNm\": \""+regionNm+"\", " +
				"\"openYN\": \"true\" }";

		mockMvc.perform(post("/stores")
//						.header("Authorization", "Bearer " + accessToken))

						.contentType(MediaType.APPLICATION_JSON) // Json 타입으로 지정
						.content(jsonResponse) // jjson으로 내용 등록
				)
				.andExpect(status().is2xxSuccessful())
//				.andExpect(status().isOk())
//				.andExpect(content().string("expect json값"))
//				.andExpect(view().string("뷰이름"));
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.regionNm").value(regionNm))
//				.andExpect(jsonPath("$.hobby[2].name").value(regionNm))
				.andDo(print()); // 응답값 print

//		mockMvc.perform(get("/test")
//				.param("query", "부대찌개")
//				.cookie(new Cookie("key", "value"))
//				.header("헤더 값")
//				.contentType(MediaType.APPLICATION.JSON)
//				.content("json으로") );
	}
}
