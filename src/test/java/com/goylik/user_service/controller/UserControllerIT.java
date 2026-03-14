package com.goylik.user_service.controller;

import com.goylik.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    private final String BASE_URL = "/api/users";
    private final String VALID_USER_JSON = """
            {
                "name": "John",
                "surname": "Doe",
                "birthDate": "2000-11-05",
                "email": "john@test.com"
            }
            """;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturn201_WhenValidRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.birthDate").value("2000-11-05"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createUser_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_USER_JSON));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details.message").value(containsString("already exists")));
    }

    @Test
    void createUser_ShouldReturn400_WhenNameIsBlank() throws Exception {
        String invalidJson = """
                {
                    "name": "",
                    "surname": "Doe",
                    "birthDate": "2000-11-05",
                    "email": "john@test.com"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        String invalidJson = """
                {
                    "name": "John",
                    "surname": "Doe",
                    "birthDate": "2000-11-05",
                    "email": "not-an-email"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ShouldReturn400_WhenBirthDateIsInFuture() throws Exception {
        String invalidJson = """
                {
                    "name": "John",
                    "surname": "Doe",
                    "birthDate": "3000-01-01",
                    "email": "john@test.com"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andReturn();

        JsonNode createdUser = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long userId = createdUser.get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void getUserById_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details.message").value(containsString("not found")));
    }

    @Test
    void getUserById_ShouldReturn400_WhenIdIsNegative() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_ShouldReturnPage_WithDefaultPagination() throws Exception {
        createUser("John", "Doe", "john1@test.com");
        createUser("Jane", "Smith", "jane@test.com");
        createUser("Bob", "Johnson", "bob@test.com");

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));
    }

    @Test
    void getAllUsers_ShouldReturnFilteredResults_WhenNameProvided() throws Exception {
        createUser("John", "Doe", "john@test.com");
        createUser("Johnny", "Depp", "johnny@test.com");
        createUser("Jane", "Smith", "jane@test.com");

        mockMvc.perform(get(BASE_URL)
                        .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].name", everyItem(containsStringIgnoringCase("John"))));
    }

    @Test
    void getAllUsers_ShouldReturnFilteredResults_WhenSurnameProvided() throws Exception {
        createUser("John", "Doe", "john@test.com");
        createUser("Jane", "Doe", "jane@test.com");
        createUser("Bob", "Smith", "bob@test.com");

        mockMvc.perform(get(BASE_URL)
                        .param("surname", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].surname", everyItem(equalTo("Doe"))));
    }

    @Test
    void getAllUsers_ShouldReturnFilteredResults_WhenBothNameAndSurnameProvided() throws Exception {
        createUser("John", "Doe", "john@test.com");
        createUser("John", "Smith", "john.smith@test.com");
        createUser("Jane", "Doe", "jane@test.com");

        mockMvc.perform(get(BASE_URL)
                        .param("name", "John")
                        .param("surname", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.content[0].surname").value("Doe"));
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoMatchFound() throws Exception {
        createUser("John", "Doe", "john@test.com");

        mockMvc.perform(get(BASE_URL)
                        .param("name", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllUsers_ShouldRespectPagination() throws Exception {
        for (int i = 0; i < 15; i++) {
            createUser("User" + i, "Last" + i, "user" + i + "@test.com");
        }

        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(jsonPath("$.pageable.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void updateUser_ShouldReturn200_WhenValidRequest() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        String updateJson = """
                {
                    "name": "Jonathan",
                    "surname": "DoeUpdated",
                    "birthDate": "2000-11-05",
                    "email": "jonathan@test.com"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Jonathan"))
                .andExpect(jsonPath("$.surname").value("DoeUpdated"))
                .andExpect(jsonPath("$.email").value("jonathan@test.com"));
    }

    @Test
    void updateUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        String updateJson = """
                {
                    "name": "Jonathan",
                    "surname": "Doe",
                    "birthDate": "2000-11-05",
                    "email": "jonathan@test.com"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details.message").value(containsString("not found")));
    }

    @Test
    void updateUser_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        createUser("John", "Doe", "john@test.com");

        Long secondUserId = createUserAndGetId("Jane", "Smith", "jane@test.com");

        String updateJson = """
                {
                    "name": "Jane",
                    "surname": "Smith",
                    "birthDate": "2000-11-05",
                    "email": "john@test.com"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/" + secondUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details.message").value(containsString("already exists")));
    }

    @Test
    void updateUser_ShouldAllowUpdatingWithSameEmail() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        String updateJson = """
                {
                    "name": "Jonathan",
                    "surname": "Doe",
                    "birthDate": "2000-11-05",
                    "email": "john@test.com"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonathan"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void updateUser_ShouldReturn400_WhenInvalidData() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        String invalidJson = """
                {
                    "name": "",
                    "surname": "Doe",
                    "birthDate": "3000-01-01",
                    "email": "not-email"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_ShouldReturn204_WhenUserExists() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        mockMvc.perform(delete(BASE_URL + "/" + userId))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void deleteUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details.message").value(containsString("not found")));
    }

    @Test
    void deleteUser_ShouldReturn404_WhenUserAlreadyDeleted() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        mockMvc.perform(delete(BASE_URL + "/" + userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(BASE_URL + "/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateUser_ShouldReturn204_WhenUserExists() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/activate"))
                .andExpect(status().isNoContent());

        var user = userRepository.findById(userId);
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getActive()).isTrue();
    }

    @Test
    void deactivateUser_ShouldReturn204_WhenUserExists() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        var user = userRepository.findById(userId);
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getActive()).isFalse();
    }

    @Test
    void activateUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/99999/activate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details.message").value(containsString("not found")));
    }

    @Test
    void deactivateUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/99999/deactivate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details.message").value(containsString("not found")));
    }

    @Test
    void activateUser_ShouldWork_WhenUserAlreadyActive() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/activate"))
                .andExpect(status().isNoContent());

        var user = userRepository.findById(userId);
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getActive()).isTrue();
    }

    @Test
    void deactivateUser_ShouldWork_WhenUserAlreadyDeactivated() throws Exception {
        Long userId = createUserAndGetId("John", "Doe", "john@test.com");

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        var user = userRepository.findById(userId);
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getActive()).isFalse();
    }

    @Test
    void completeUserLifecycle_ShouldWork() throws Exception {
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_USER_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdUser = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long userId = createdUser.get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));

        String updateJson = """
                {
                    "name": "Jonathan",
                    "surname": "Doe",
                    "birthDate": "2000-11-05",
                    "email": "john.updated@test.com"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonathan"))
                .andExpect(jsonPath("$.email").value("john.updated@test.com"));

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch(BASE_URL + "/" + userId + "/activate"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(delete(BASE_URL + "/" + userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + userId))
                .andExpect(status().isNotFound());
    }

    private void createUser(String name, String surname, String email) throws Exception {
        String userJson = String.format("""
                {
                    "name": "%s",
                    "surname": "%s",
                    "birthDate": "2000-11-05",
                    "email": "%s"
                }
                """, name, surname, email);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());
    }

    private Long createUserAndGetId(String name, String surname, String email) throws Exception {
        String userJson = String.format("""
                {
                    "name": "%s",
                    "surname": "%s",
                    "birthDate": "2000-11-05",
                    "email": "%s"
                }
                """, name, surname, email);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdUser = objectMapper.readTree(result.getResponse().getContentAsString());
        return createdUser.get("id").asLong();
    }
}