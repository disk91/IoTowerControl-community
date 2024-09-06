package com.disk91.iot.users;

import com.disk91.common.tools.exceptions.ITParseException;
import com.disk91.common.tools.exceptions.ITTooManyException;
import com.disk91.users.config.UsersConfig;
import com.disk91.users.mdb.entities.Role;
import com.disk91.users.mdb.repositories.RolesRepository;
import com.disk91.users.services.UsersRolesCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Import(UsersConfig.class)
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ExtendWith(MockitoExtension.class)
public class RoleTests {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Mock
    private RolesRepository rolesRepository;

    @InjectMocks
    private UsersRolesCache usersRolesCache;

    //@Autowired
    @InjectMocks
    private UsersConfig usersConfig;

    /**
     * When the userRoleCache Service is getting started, the roles are automatically loaded in the cache
     */
    @Test
    public void testInitialization() {
        log.info("[users][test] Running testInitialization");

        // setup the mock to return null when the findAll method is called
        given(rolesRepository.findAll()).willReturn(new ArrayList<Role>());

        // Because @PostConstruct is not called by the test, we need to call it manually
        usersRolesCache.initRolesCache();

        // verify we have loaded the platform roles
        assertEquals(usersRolesCache.__countPfRoles(),usersRolesCache.__countRolesInCache());

        // query role ROLE_GOD_ADMIN, verify it is existing and corresponding to what is expected
        assertDoesNotThrow(() -> {
            Role godAdmin = usersRolesCache.getRole("ROLE_GOD_ADMIN");
            assertEquals("system",godAdmin.getCreationBy());
        });

    }

    @Test
    protected void testRoleCreation() {
        log.info("[users][test] Running testRoleCreation");

        // Make sur the configuration is good for test
        assertEquals("dba", usersConfig.getUsersIntracomMedium());

        // Make sure role creation fails when the description is not respecting the
        // lower-case format.
        assertThrows(ITParseException.class, () -> {
            usersRolesCache.addRole("ROLE_TEST", "Test Role", "Test Role Description", "test");
        });

        // Make sure role creation fails when the role already exists
        assertThrows(ITTooManyException.class, () -> {
            usersRolesCache.addRole("ROLE_GOD_ADMIN", "role-duplication-test", "Role duplication test", "test");
        });


        // add one role into the cache, this will create a new entry in the database and also in the cache


        //Role role = new Role();
        //Mockito.when(rolesRepository.save(any(Role.class))).thenReturn(role);

    }

}
