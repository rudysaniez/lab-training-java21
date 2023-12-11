package com.adeo.lab.java21.fire;

import java.util.SplittableRandom;
import java.util.concurrent.*;

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
public class DataOrientedProgrammingWorkshop {

    public static void main(String[] args) throws Exception {

        /*
        Hey man, you've got the latest version of Reactor, and you've got the Sink function.
        So you can tell me when a payment has been made, right?
         */

        // By Cash
        Future<PaymentResult> cashPaymentResult = new Cash().payment(38.3f);
        var paymentResult = cashPaymentResult.get();
        System.out.printf("""
                    >> The payment by Cash is %s
                """,
                paymentResult.getStatus().name());

        // By Card
        Future<PaymentResult> cardPaymentResult = new Card().payment(78.9f);
        paymentResult = cardPaymentResult.get();
        System.out.printf("""
                    >> The payment by Card is %s
                """,
                paymentResult.getStatus().name());

        // By PayPal
        Future<PaymentResult> paypalPaymentResult = new PayPal().payment(99.99f);
        paymentResult = paypalPaymentResult.get();
        System.out.printf("""
                    >> The payment by Paypal is %s
                """,
                paymentResult.getStatus().name());

        /*
        You have to add the total, when can you add this function, please!
        You're working with Java 21, you've got the stream and the collect operator ...
         */

        System.exit(0); //FIXME: Need to understand why i must to specify an exist...
    }

    /**
     * Main interface
     */
    interface PaymentSystem {
        Future<PaymentResult> payment(float amount);
    }

    /**
     * Payment system implementation : Payment by cash.
     */
    static class Cash implements PaymentSystem {
        @Override
        public Future<PaymentResult> payment(float amount) {
            return NetworkSimulation.run(this, amount);
        }
    }

    /**
     * Payment system implementation : Payment by credit card.
     */
    static class Card implements PaymentSystem {
        @Override
        public Future<PaymentResult> payment(float amount) {
            return NetworkSimulation.run(this, amount);
        }
    }

    /**
     * Payment system implementation : Payment By paypal
     */
    static class PayPal implements PaymentSystem {
        @Override
        public Future<PaymentResult> payment(float amount) {
            return NetworkSimulation.run(this, amount);
        }
    }

    static class PaymentResult {
        PaymentStatus status;

        PaymentResult(PaymentStatus status) {this.status = status;}
        PaymentStatus getStatus() {return status;}
    }

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

        /**
         * @param paymentSystem : the payment system
         * @return {@link Future<PaymentResult>}
         */
        public static Future<PaymentResult> run(
                PaymentSystem paymentSystem,
                float amount) {

            System.out.printf(
                    """ 
                    > The payment is activated... the amount is %f and the payment type is %s, Please wait...
                    """,
                    amount, paymentSystem.getClass().getSimpleName());

            Callable<PaymentResult> runPayment = () -> new PaymentResult(PaymentStatus.OK);

            final Future<PaymentResult> result;
            if (paymentSystem instanceof Cash) {
                result = scheduled.schedule(runPayment, random.nextInt(1, 3), TimeUnit.SECONDS);
            } else if (paymentSystem instanceof Card) {
                result = scheduled.schedule(runPayment, random.nextInt(4, 10), TimeUnit.SECONDS);
            }
            else result = scheduled.schedule(runPayment, random.nextInt(3, 8), TimeUnit.SECONDS);

            return result;
        }
    }
}
