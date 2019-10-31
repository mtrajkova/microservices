package microservices.book.socialmultiplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import microservices.book.socialmultiplication.domain.Multiplication;
import microservices.book.socialmultiplication.domain.MultiplicationResultAttempt;
import microservices.book.socialmultiplication.domain.Result;
import microservices.book.socialmultiplication.domain.User;
import microservices.book.socialmultiplication.service.MultiplicationService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@WebMvcTest(MultiplicationResultAttemptController.class)
public class MultiplicationResultAttemptControllerTest {

    @MockBean
    private MultiplicationService multiplicationService;

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<MultiplicationResultAttempt> jsonResult;
    private JacksonTester<List<MultiplicationResultAttempt>> jsonResultAttempts;
    private JacksonTester<Result> jsonResponse;

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void postResultReturnCorrect() throws Exception {
        genericParamterizedTest(true);
    }

    @Test
    public void postResultReturnNotCorrect() throws Exception {
        genericParamterizedTest(false);
    }

    @Test
    public void getUserStats() throws Exception {
        User user = new User("john_doe");
        Multiplication multiplication = new Multiplication(50, 70);
        MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(user, multiplication, 3500, true);
        List<MultiplicationResultAttempt> recentAttempts = Lists.newArrayList(attempt);

        given(multiplicationService.getStatsForUser("john_doe")).willReturn(recentAttempts);

        MockHttpServletResponse response = mockMvc.perform(
                get("/results").param("alias", "john_doe"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                jsonResultAttempts.write(recentAttempts).getJson());
    }

    void genericParamterizedTest(final boolean correct) throws Exception {
        given(multiplicationService.checkAttempt(any(MultiplicationResultAttempt.class))).willReturn(correct);

        User user = new User("john");
        Multiplication multiplication = new Multiplication(50, 70);
        MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(user, multiplication, 3500, correct);

        MockHttpServletResponse response = mockMvc.perform(
                post("/results").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonResult.write(attempt)
                                .getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonResponse.write(new Result(correct)).getJson());
    }

}
