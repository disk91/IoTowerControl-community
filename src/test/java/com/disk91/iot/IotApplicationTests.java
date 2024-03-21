package com.disk91.iot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
class IotApplicationTests {

	/*
	@Test
	void contextLoads() {
	}
 */


}

/*

SpringBootTest
@AutoConfigureMockMvc
class StoreApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitizenRepository citizenRepository;


    @Test
    void contextLoads() {
    }

    @Test
    public void test() {

        Mockito.when(citizenRepository.getDataFromDB()).thenReturn("Something you'd like to Return");

    }

}
 */