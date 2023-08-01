package org.acme.getting.started;

import io.github.snowdrop.jester.api.Container;
import io.github.snowdrop.jester.api.Jester;
import io.github.snowdrop.jester.api.RestService;
import io.github.snowdrop.jester.api.RunOnKubernetes;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@Jester
@RunOnKubernetes(ephemeralNamespaceEnabled = true, deleteNamespaceAfterAll = true)
public class GreetingResourceIT {

    @Container(image = "ttl.sh/c04768cb-2012-4280-990d-a9adf51c7462:2h", ports = 8080, expectedLog = "Installed features")
    static RestService app = new RestService();

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("hello"));
    }
}