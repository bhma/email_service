package com.henbran.email_service.component;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.henbran.email_service.record.Email;
import com.henbran.email_service.service.EmailService;
import com.henbran.email_service.utils.Constants;

import jakarta.annotation.PostConstruct;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class EmailComponent {
    private final EmailService emailService;

    public EmailComponent(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostConstruct
    public void doSubscribe() throws JedisException {
        executeSubscribe();
    }

    @Async
    public void executeSubscribe() throws JedisException {
        Jedis jedis = new Jedis("localhost", 6379);
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Mensagem recebida da outra API!!");
                System.out.println("Mensagem: " + message);
                try {
                    Email email = new Email(Constants.TO_EMAIL, Constants.EMAIL_SUBJECT, message);
                    emailService.sendEmail(email);

                } catch (Exception e) {
                    // TODO: handle exception
                    System.out.println("Ocorreu um problema ao tentar enviar um email. Verifique o EmailService.");
                    System.out.println(e.getMessage());
                }
            }
        };

        jedis.subscribe(pubSub, Constants.EMAIL_CHANNEL_STRING);
        jedis.close();
    }
}
