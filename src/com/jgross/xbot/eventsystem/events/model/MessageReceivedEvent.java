package com.jgross.xbot.eventsystem.events.model;

import com.jgross.xbot.eventsystem.EventList;
import com.jgross.xbot.model.ChatRoom;
import org.jivesoftware.smack.packet.Message;

import com.jgross.xbot.eventsystem.events.RoomEvent;

public class MessageReceivedEvent extends RoomEvent {

    
    private static final EventList events = new EventList();
    private Message message;
    
    public MessageReceivedEvent(ChatRoom chatRoom, Message message) {
        super(chatRoom);
        this.message = message;
    }
    
    /**
     * Returns who the packet is being sent "from" or null if the value is not set.
     * The XMPP protocol often makes the "from" attribute optional, so it does not always need to be set.
     * 
     * @see Message#getFrom()
     * @return Message
     */
    public String from() {
        return message.getFrom();
    }
    
    /**
     * The body is the main message contents.
     * If the body of the message is null, then this method will return "".
     * @see Message#getBody()
     * @return the default body of the message.
     */
    public String body() {
        return message.getBody() == null ? "" : message.getBody();
    }
    
    /**
     * @return the message object that holds the XMPP users for this message.
     */
    public Message getMessage() {
        return message;
    }

    @Override
    public EventList getEvents() {
        return events;
    }
    
    public static EventList getEventList() {
        return events;
    }

}
