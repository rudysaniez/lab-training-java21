package com.adeo.lab.java21.fire;

import java.util.Objects;
import java.util.SplittableRandom;
import java.util.concurrent.*;

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
 */
public class DataOrientedProgrammingWorkshopSolution {

    public static void main(String[] args) throws Exception {

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

        NetworkSimulation.scheduled.close();
    }

    /**
     * @param paymentSystem : the payment system
     * @return {@link Future<PaymentResult>}
     */
    static Future<PaymentResult> payment(PaymentSystem paymentSystem) {

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

    record PaymentResult(PaymentStatus status) {

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

    static class NetworkSimulation {

        final static ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        final static SplittableRandom random = new SplittableRandom();

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

            Callable<PaymentResult> runPayment = () -> new PaymentResult(PaymentStatus.OK);

            return switch (paymentSystem) {
                case Cash cash -> scheduled.schedule(runPayment, random.nextInt(1, 3), TimeUnit.SECONDS);
                case Card card -> scheduled.schedule(runPayment, random.nextInt(3, 10), TimeUnit.SECONDS);
                case PayPal payPal -> scheduled.schedule(runPayment, random.nextInt(4, 8), TimeUnit.SECONDS);
            };
        }

    }
}
