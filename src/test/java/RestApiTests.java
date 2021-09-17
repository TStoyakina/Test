import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pojos.MessagePojo;
import pojos.ReadMessageRequest;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RestApiTests {

   private static final RequestSpecification req_spec = new RequestSpecBuilder().
           setBaseUri("https://run.mocky.io/v3").
           setContentType(ContentType.JSON).build();

    @ParameterizedTest
    @DisplayName("Positive /sendMessage test. Request body is JSON, contains required id")
    @MethodSource("SendMessageSuccessful")
    public void SendMessageSuccessful(int id, String messageText, boolean importance)
    {
        MessagePojo request = new MessagePojo();
        request.setId(id);
        request.setText(messageText);
        request.setImportant(importance);

        given().
                baseUri("https://run.mocky.io/v3").
                basePath("/bb697acc-6196-4573-a3a2-eb9cd2cf0cd2").
                contentType(ContentType.JSON).
                body(request).
                when().
                post().
                then().
                statusCode(200).and().statusLine(StringContains.containsString("OK"));
    }

    static Stream<Arguments> SendMessageSuccessful() {
        return Stream.of(arguments(1, "Some text", false));
    }

    @ParameterizedTest
    @DisplayName("Negative /sendMessage test. Request body is JSON, but it doesn't contain required Id field")
    @MethodSource("SendMessageWOReqField")
    public void SendMessageWOReqField(String messageText, boolean importance)
    {
        MessagePojo request = new MessagePojo();
        request.setText(messageText);
        request.setImportant(importance);
        given().
                baseUri("https://run.mocky.io/v3").
                basePath("/991e0503-e3c6-47e5-8c0d-da9442c25126").
                contentType(ContentType.JSON).
                body(request).
                when().
                post().
                then().
                statusCode(400).and().statusLine(StringContains.containsString("Bad Request"));
    }
    static Stream<Arguments> SendMessageWOReqField() {
        return Stream.of(arguments("value1", false));
    }

    static IntStream MessageIDProvider() {
        Random r = new Random();
        return IntStream.of(r.nextInt(10));
    }

    @ParameterizedTest
    @DisplayName("Negative /cancelMessage test. Query-parameter contains non-existent Id value")
    @MethodSource("MessageIDProvider")
    public void CancelMessageInvalidID(int id)
    {
        given().
                baseUri("https://run.mocky.io/v3").
                basePath("/abf93805-65c1-43bd-a876-9a74aed6aea0").
                param(Integer.toString(id+1000)).
                when().
                get().
                then().
                statusCode(204).and().statusLine(StringContains.containsString("No Content"));
    }

    @ParameterizedTest
    @DisplayName("Positive /cancelMessage test. Query-parameter has message Id value which is being processing")
    @MethodSource("MessageIDProvider")
    public void CancelMessageSuccessful(int id)
    {
        given().
                baseUri("https://run.mocky.io/v3").
                basePath("/66c59f37-a99f-45c6-8cc4-4351b98aae23").
                param(Integer.toString(id)).
                when().
                get().
                then().
                statusCode(200).and().statusLine(StringContains.containsString("OK"));
    }

    @ParameterizedTest
    @DisplayName("Positive /readMessage test. Request body is a valid JSON. " +
            "Message with needed Id value has already been processes and added into DB")
    @MethodSource("MessageIDProvider")
    public void ReadMessageIncorrectResponse(int id)
    {
        MessagePojo message = new MessagePojo();
        message.setId(id);
        ReadMessageRequest request = new ReadMessageRequest();
        request.setFind(message);

        List<MessagePojo> messages =
                given().
                        baseUri("https://run.mocky.io/v3").
                        basePath("/8659e175-c9c9-4a76-ac2a-e6d18cb39a22").
                        contentType(ContentType.JSON).
                        body(request).
                        when().
                        post().
                        then().
                        statusCode(200).and().statusLine(StringContains.containsString("OK")).
                        and().
                        extract().jsonPath().getList("$", MessagePojo.class);

        //Check the response contains only required Id's
       assertThat(messages).extracting(MessagePojo::getId).isEqualTo(id);
    }



}
