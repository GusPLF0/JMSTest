package br.gusplf.jms;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ApplicationScoped
public class PriceConsumer implements Runnable {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Inject
    ConnectionFactory connectionFactory;

    private volatile String lastPrice;

    public String getLastPrice() {
        return lastPrice;
    }

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("prices"));
            while (true) {
                Message message = consumer.receive(); // Pegando a mensagem
                if (message == null) return; // Quebra de loop se nada chegar
                lastPrice = message.getBody(String.class); // Pegando o conteudo da mensagem
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }
}
