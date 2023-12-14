package com.adeo.lab.java21.fire;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Sinks;

import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.FormatProcessor.FMT;

/**
 * Hello everybody, let's start with Java 21 and pattern matching.
 * You are a new and powerful developer. For the moment, you retrieve an application developed by a Java dinosaur.
 * The code is oriented object, and now you want to change it, and you want to use the new Java 21 features with Pattern matching
 * and Pattern matching for instanceof. You also want to block the creation of new payment system.
 * If you prefer, only Cash, the credit card and PayPal are authorized.
 * You think that it is necessary to stop with POO and you oriented to Data oriented programming (DOP).
 * For that, use the record pattern !
 * You want to perform the displaying by using the preview in Java 21, the java.lang.StringTemplate like this println(STR.""" My value is \{ data.getValue() } """).
 * If you want to do that, you need to activate the preview features in Java 21.
 * You can also use the FormatProcessor, java.util.FormatProcessor, like this println(FMT.""" My value is %d\{ data.getValue() } """).
 * Now, it's time for me to tell you, good luck !
 * <a href="https://docs.oracle.com/en/java/javase/20/language/sealed-classes-and-interfaces.html#GUID-0C709461-CC33-419A-82BF-61461336E65F">Sealed classes</a>
 * <a href="https://docs.oracle.com/en/java/javase/20/language/record-patterns.html#GUID-7623D3AD-4141-4914-A384-60C65BD0C010">Record patterns</a>
 * <a href="https://docs.oracle.com/en/java/javase/20/language/switch-expressions.html#GUID-BA4F63E3-4823-43C6-A5F3-BAA4A2EF3ADC">Switch expression</a>
 */
public class DataOrientedProgrammingWorkshopSolution {

    public static void main(String[] args) throws Exception {

        // Observer: When a payment has been made, I enter it in the console.
        NetworkSimulation.paymentsSink.asFlux()
                .subscribe(p -> System.out.println(STR."""
                    >>> A payment has been done : \{p}
                """));

        // By Cash
        Future<PaymentResult> cashPaymentResult = payment(new Cash(38.3f));
        System.out.println(FMT."""
                    >> The payment by Cash is %s\{ cashPaymentResult.get().status().name() }
                """);

        // By Card
        Future<PaymentResult> cardPaymentResult = payment(new Card(88.31f));
        System.out.println(STR."""
                    >> The payment by Card is \{ cardPaymentResult.get().status().name() }
                """);

        // By PayPal
        Future<PaymentResult> PayPalPaymentResult = payment(new PayPal(99.89f));
        System.out.println(STR."""
                    >> The payment by Paypal is \{ PayPalPaymentResult.get().status().name() }
                """);

        // The sum for today
        DoubleSummaryStatistics stats = NetworkSimulation.payments
                        .stream()
                        .collect(Collectors.summarizingDouble(PaymentResult::amount));

        System.out.println(STR."""
                >> The total amount is \{stats.getSum()}
                >> The number of payment is \{stats.getCount()}
                >> The payment max is \{stats.getMax()}
                >> The payment min is \{stats.getMin()}
                """);

        NetworkSimulation.scheduled.close();
    }

    /**
     * @param paymentSystem : the payment system
     * @return {@link Future<PaymentResult>}
     */
    static Future<PaymentResult> payment(@NotNull PaymentSystem paymentSystem) {

        return switch(paymentSystem) {
            case Cash(float amount) -> NetworkSimulation.run(paymentSystem, amount);
            case Card(float amount) -> NetworkSimulation.run(paymentSystem, amount);
            case PayPal(float amount) -> NetworkSimulation.run(paymentSystem, amount);
        };
    }

    /**
     * Sealed main interface about payment system.
     */
    sealed interface PaymentSystem permits Cash, Card, PayPal {
    }

    /**
     * Payment system implementation : Payment by cash.
     */
    record Cash(float amount) implements PaymentSystem {}

    /**
     * Payment system implementation : Payment by credit card.
     */
    record Card(float amount) implements PaymentSystem {}

    /**
     * Payment system implementation : Payment By paypal
     */
    record PayPal(float amount) implements PaymentSystem {}

    /**
     * @param status
     * @param amount
     */
    record PaymentResult(PaymentStatus status, float amount) {

        PaymentResult {
            Objects.requireNonNull(status);
        }
    }

    /**
     * Don't touch !
     */
    enum PaymentStatus {
        OK,
        KO;
        PaymentStatus() {}
    }

    /*
    The network simulation component.
     */
    static class NetworkSimulation {

        final static ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        final static SplittableRandom random = new SplittableRandom();
        final static CopyOnWriteArrayList<PaymentResult> payments = new CopyOnWriteArrayList<>();
        final static Sinks.Many<PaymentResult> paymentsSink = Sinks.many().unicast().onBackpressureBuffer();

        /**
         * @param paymentSystem : the payment system
         * @return {@link Future<PaymentResult>}
         */
        public static Future<PaymentResult> run(PaymentSystem paymentSystem, float amount) {

            System.out.println(FMT.
                    """ 
                    > The payment is activated... the amount is %f\{amount} and the payment type is %s\{paymentSystem.getClass().getSimpleName()}, Please wait...
                    """
                    );

            Callable<PaymentResult> runPayment = () -> {
                var p = new PaymentResult(PaymentStatus.OK, amount);
                payments.add(p);
                paymentsSink.tryEmitNext(p);
                return p;
            };

            return switch (paymentSystem) {
                case Cash cash -> scheduled.schedule(runPayment, random.nextInt(1, 3), TimeUnit.SECONDS);
                case Card card -> scheduled.schedule(runPayment, random.nextInt(3, 10), TimeUnit.SECONDS);
                case PayPal payPal -> scheduled.schedule(runPayment, random.nextInt(4, 8), TimeUnit.SECONDS);
            };
        }

    }
}
