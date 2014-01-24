package com.novemberain.consumers;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HiLoPriorityConsumers {
    private static String HIGH_PRIORITY_Q = "examples.priority.hilo.high";
    private static String LOW_PRIORITY_Q  = "examples.priority.hilo.low";
    private static List<String> routingKeys = Arrays.asList(HIGH_PRIORITY_Q, LOW_PRIORITY_Q);

    public static void main(String[] args) throws IOException {
        ConnectionFactory cf = new ConnectionFactory();
        Connection conn = cf.newConnection();

        Channel ch1 = conn.createChannel();
        Channel ch2 = conn.createChannel();

        ch1.queueDeclare(HIGH_PRIORITY_Q, false, false, true, null);
        ch2.queueDeclare(LOW_PRIORITY_Q, false, false, true, null);

        Consumer hiCons = instantiateConsumer(ch1, "high");
        Consumer loCons = instantiateConsumer(ch2, "low");

        Channel ch3 = conn.createChannel();
        createBacklog(ch3, 30, LOW_PRIORITY_Q, "low");
        createBacklog(ch3, 3,  HIGH_PRIORITY_Q, "high");

        ch1.basicConsume(HIGH_PRIORITY_Q, hiCons);
        ch2.basicConsume(LOW_PRIORITY_Q, loCons);

        Random rnd = new Random();

        try {
            while(true) {
                // publish one low priority message
                ch3.basicPublish("", LOW_PRIORITY_Q, null, "message".getBytes());

                // publish one low or high priority one
                String key = routingKeys.get(rnd.nextInt(routingKeys.size()));
                ch3.basicPublish("", key, null, "message".getBytes());
                Thread.sleep(500);
            }
        } catch (InterruptedException ie) {
            System.out.println("Terminating...");
            System.exit(0);
        }
    }

    private static DefaultConsumer instantiateConsumer(final Channel ch1, final String priority) {
        return new DefaultConsumer(ch1) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("[" + priority + " priority] Consumed " + new String(body, "UTF-8"));
            }
        };
    }

    private static void createBacklog(Channel ch3, int count, String queue, String priority) throws IOException {
        for (int i = 0; i < count; i++) {
            String s = priority + " priority message " + Integer.toString(i);
            ch3.basicPublish("", queue, null, s.getBytes());
        }
    }
}
