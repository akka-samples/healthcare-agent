package io.akka.health.agent.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;
import io.akka.health.agent.application.ConversationHistoryView;

/**
 * This Http endpoint return the static UI page located under src/main/resources/static-resources/
 */
@akka.javasdk.annotations.http.HttpEndpoint
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class UiEndpoint {

  private final ComponentClient componentClient;

  public UiEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Get("/")
  public HttpResponse index() {
    return HttpResponses.staticResource("index.html");
  }

  @Get("/users/{userId}/sessions/")
  public ConversationHistoryView.ConversationHistory getSession(String userId) {
    return componentClient.forView()
            .method(ConversationHistoryView::getSessionsByUser)
            .invoke(userId);
  }
}
