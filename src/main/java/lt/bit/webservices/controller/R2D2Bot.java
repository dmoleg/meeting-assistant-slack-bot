package lt.bit.webservices.controller;

import lt.bit.webservices.GoogleAuthService;
import me.ramswaroop.jbot.core.common.Controller;
import me.ramswaroop.jbot.core.common.EventType;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.Valid;
<<<<<<< Updated upstream
=======
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
>>>>>>> Stashed changes

@Component
public class R2D2Bot extends Bot {


    private GoogleAuthService googleAuthService;
    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) throws IOException {

        this.googleAuthService = new GoogleAuthService(event.getUserId());

        if (googleAuthService.isAuthorized(event.getUserId())) {
            reply(session, event, new Message("Hi, I am " + slackService.getCurrentUser().getName()));
        } else {
            String authorizationUrl = this.googleAuthService.getAuthorizationUrl();
            reply(session, event, new Message("Hi, " + event.getUserId() + " please follow " + authorizationUrl));

            reply(session, event, new Message("Waiting for access :)"));
            this.googleAuthService.waitForCode(event.getUserId());
            reply(session, event, new Message("Thank you for trust!"));
        }
    }
<<<<<<< Updated upstream
=======

    @Controller(events = {EventType.DIRECT_MESSAGE, EventType.MESSAGE}, pattern = "(setup meeting)", next = "confirmTiming")
    public void setupMeeting(WebSocketSession session, Event event) {
        startConversation(event, "confirmTiming");
        reply(session, event, "Cool! At what time (ex. 15:30) do you want me to set up the meeting?");
    }

    @Controller(events = {EventType.DIRECT_MESSAGE}, pattern = "(show my events)")
    public void showMyEvents(WebSocketSession session, Event event) throws IOException {
        List<com.google.api.services.calendar.model.Event> userEvents = this.googleAuthService.getUserEvents(event.getUserId());

        String events = userEvents.stream().map(e -> e.getSummary()).collect(Collectors.joining("\n"));
        reply(session, event, new Message(" Event: " + events));


    }

    @Controller(next = "askTimeForMeeting")
    public void confirmTiming(WebSocketSession session, Event event) {
        reply(session, event, "Your meeting is set at " + event.getText() +
                ". Would you like to repeat it tomorrow?");
        nextConversation(event);
    }

    @Controller(next = "askWhetherToRepeat")
    public void askTimeForMeeting(WebSocketSession session, Event event) {
        if (event.getText().contains("yes")) {
            reply(session, event, "Okay. Would you like me to set a reminder for you?");
            nextConversation(event);
        } else {
            reply(session, event, "No problem. You can always schedule one with 'setup meeting' command.");
            stopConversation(event);
        }
    }

    @Controller
    public void askWhetherToRepeat(WebSocketSession session, Event event) {
        if (event.getText().contains("yes")) {
            reply(session, event, "Great! I will remind you tomorrow before the meeting.");
        } else {
            reply(session, event, "Okay, don't forget to attend the meeting tomorrow :)");
        }
        stopConversation(event);
    }
>>>>>>> Stashed changes
}
