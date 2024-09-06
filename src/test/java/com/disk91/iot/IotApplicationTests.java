package com.disk91.iot;

import com.disk91.users.mdb.entities.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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


package com.disk91.iot.users;

import com.disk91.users.config.UsersConfig;
import com.disk91.users.mdb.entities.Role;
import com.disk91.users.mdb.repositories.RolesRepository;
import com.disk91.users.services.UsersRolesCache;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

//@AutoConfigureMockMvc
@Import({UsersConfig.class, UsersRolesCache.class})
@DataMongoTest
//@SpringBootTest
public class RoleTests {

    @Autowired
    private UsersConfig usersConfig;

    //@Autowired
    //private MockMvc mockMvc;

   // @Mock
   // private RolesRepository rolesRepository;
   // @InjectMocks
   // private UsersRolesCache usersRolesCache;

    @Autowired
    private UsersRolesCache usersRolesCache;

    @Autowired
    private RolesRepository rolesRepository;


@Test
public void testInitialization() {
    System.out.println("HEY");
    // verify we have loaded the platform roles
    assertEquals(usersRolesCache.__countPfRoles(),usersRolesCache.__countRolesInCache());

    // query role ROLE_GOD_ADMIN, verify it is existing and corresponding to what is expected
    assertDoesNotThrow(() -> {
        Role godAdmin = usersRolesCache.getRole("ROLE_GOD_ADMIN");
        assertEquals(godAdmin.getCreationBy(),"super administrator");
    });

}

/*
    @Test
    public void testRoleCreation() {
        System.out.println("HEY2");

        // Make sur the configuration is good for test
        assertEquals(usersConfig.getUsersIntracomMedium(),"dba");

        // add one role into the cache, this will create a new entry in the database and also in the cache


        Role role = new Role();
        //Mockito.when(rolesRepository.save(any(Role.class))).thenReturn(role);

    }
*/

