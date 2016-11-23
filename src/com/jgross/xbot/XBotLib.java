package com.jgross.xbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.jgross.xbot.model.Bot;
import com.jgross.xbot.eventsystem.EventSystem;
import com.jgross.xbot.model.ChatRoom;
import com.jgross.xbot.networking.Connection;
import com.jgross.xbot.utils.NotificationColor;
import com.jgross.xbot.utils.NotificationType;

public class XBotLib {

    public static final EventSystem events = new EventSystem();

    /**
     * Run the bot specified in the parameter. This will have the bot connect and login into
     * hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. </br>
     * <b>This method will block until the bot has been disconnected from the server</b>
     * @param bot
     *           The bot to run.
     */
    public void run(Bot bot) {
        Connection con = new Connection();
        bot.run(con);
        try {
            con.waitForEnd();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the bot specified in the parameter in a separate thread. This method will have the bot connect and
     * login into hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. The thread running
     * this bot will be returned.
     * @param bot
     *           The bot to run
     * @return
     *        The tread object running this bot
     */
    public Thread runBotDysync(final Bot bot) {
        Thread t = new Thread() {
            @Override
            public void run() {
                XBotLib.this.run(bot);
            }
        };
        return t;
    }

    /**
     * Run the bot specified in the parameter. This will have the bot connect and login into the
     * hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. </br>
     * <b>This method will block until the bot has been disconnected from the server</b>
     * @param bot
     *           The bot to run.
     */
    public static void runBot(Bot bot) {
        new XBotLib().run(bot);
    }

    /**
     * Run the bot specified in the parameter in a separate thread. This method will have the bot connect and
     * login into hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. The thread running
     * this bot will be returned.
     * @param bot
     *           The bot to run
     * @return
     *        The tread object running this bot
     */
    public static Thread runBotDesync(Bot bot) {
        return new XBotLib().runBotDysync(bot);
    }

    /**
     * Send a hipchat notification to the room specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param room_name
     *            The name of the room to send this notification to.
     * @param APIKey
     *              The Hipchat API key to use when sending this.
     * @param type
     *            The type of message to send. If {@link NotificationType#HTML} is chosen, then this message receives no special treatment.
     *            This must be valid HTML and entities must be escaped. @see  NotificationType#HTML for more info. </br>
     *            If {@link NotificationType#TEXT} is chosen, then this message will be treated just like a normal message from a user.
     *            @see NotificationType#TEXT for more info.
     * @param notifyusers
     *                   Whether users should be notified when this notification is sent (Change tab color, play a sound, ect).
     * @param color
     *             The background color for the message.
     * @throws IOException
     *  
     * @return
     *        The json response from the server
     */
    public static String sendNotification(String message, String from, String room_name, String APIKey, NotificationType type, boolean notifyusers, NotificationColor color) throws IOException {
        URL url = new URL("https://api.hipchat.com/v1/rooms/message?format=json&auth_token=" + APIKey);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        ChatRoom chatRoom = ChatRoom.createRoom(APIKey, room_name);
        from = from.replace(" ", "_");
        String tosend = "room_id=" + chatRoom.getHipchatRoomInfo(APIKey).getID() + "&from=" + from + "&message=" + message.replaceAll(" ", "+") + "&message_format=" + type.getType() + "&notify=" + notifyusers + "&color=" + color.getType();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Length", "" + tosend.length());
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(tosend);
        writer.close();
        BufferedReader read = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder builder = new StringBuilder(100);
        String line;
        while ((line = read.readLine()) != null)
            builder.append(line);
        read.close();
        return builder.toString();
    }
}