package com.dmdev.junit.service;

import com.dmdev.junit.TestBase;
import com.dmdev.junit.dao.UserDao;
import com.dmdev.junit.dto.User;
import com.dmdev.junit.extension.ConditionalExtension;
import com.dmdev.junit.extension.PostProcessingExtension;
import com.dmdev.junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Tag("fast")
@Tag("user")
@TestInstance(Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class
//        GlobalExtension.class
})
//@RunWith()
class UserServiceTest extends TestBase {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

//    @Rule
//    ExpectedException

//    @Captor
//    private ArgumentCaptor<Integer> argumentCaptor;
    @InjectMocks
    private UserService userService;
    @Mock(lenient = true)
    private UserDao userdao;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

//    @BeforeEach
//    void prepare() {
//        System.out.println("Before each: " + this);
//
////        doReturn(true).when(userdao).delete(IVAN.getId());
////        this.userdao = Mockito.spy(new UserDao());
////        this.userService = new UserService(userdao);
//    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        doThrow(RuntimeException.class).when(userdao).delete(IVAN.getId());
        assertThrows(RuntimeException.class, () -> userdao.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
//        Mockito.doReturn(true).when(userdao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userdao).delete(2);

//        Mockito.when(userdao.delete(IVAN.getId())).thenReturn(true)
//                .thenReturn(false);

        var deleteResult = userService.delete(IVAN.getId());

//        verify(userdao, times(1)).delete(argumentCaptor.capture());

//        assertThat(argumentCaptor.getValue()).isEqualTo(1);
        assertThat(deleteResult).isTrue();

        System.out.println(userdao.delete(IVAN.getId()));
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        var users = userService.getAll();

        assertThat(users).hasSize(2);
//        assertEquals(2, users.size());
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: " );
    }

    @Nested
    @DisplayName("test user login functionality")
    @Tag("login")
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    class LoginTest {

        @Test
//        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);

            var maybeUser = userService.login(IVAN.getUsername(), "dummy");

            assertTrue(maybeUser.isEmpty());
        }

        //        @Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist(RepetitionInfo repetitionInfo) {
            userService.add(IVAN);

            var maybeUser = userService.login("dummy", IVAN.getPassword());

            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            System.out.println(Thread.currentThread().getName());
            var result = assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(100L);
                return userService.login("dummy", IVAN.getPassword());
            });
        }

        @Test
        void loginSuccessIfUserExists() {
            userService.add(IVAN);

            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
//    @org.junit.Test(expected = IllegalArgumentException.class)
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> {
                        var exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @ParameterizedTest(name = "{arguments} test")
//        @ArgumentsSource()
//        @NullSource
//        @EmptySource
//        @NullAndEmptySource
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        })
//        @EnumSource
        @MethodSource("com.dmdev.junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({
//                "Ivan,123",
//                "Petr,111"
//        })
        @DisplayName("login param test")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            doReturn(true).when(userdao).delete(IVAN.getId());
            userService.add(IVAN, PETR);

            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }


    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of( "dummy", "123", Optional.empty())
        );
    }
}