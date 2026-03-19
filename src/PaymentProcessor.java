// PaymentProcessor.java
// Simulates payment processing with card validation and UPI checks

public class PaymentProcessor {

    public enum PaymentResult {
        SUCCESS, INSUFFICIENT_FUNDS, INVALID_CARD, DECLINED, PROCESSING_ERROR
    }

    public static class PaymentResponse {
        public PaymentResult result;
        public String        transactionId;
        public String        message;
        public double        amount;

        public PaymentResponse(PaymentResult result, String txnId,
                                String message, double amount) {
            this.result        = result;
            this.transactionId = txnId;
            this.message       = message;
            this.amount        = amount;
        }

        public boolean isSuccess() { return result == PaymentResult.SUCCESS; }
    }

    // Processes credit/debit card payments
    public PaymentResponse processCard(String cardNumber, String expiry,
                                        String cvv, String name, double amount) {
        String cleaned = cardNumber.replaceAll("\\s+", "");

        if (cleaned.length() < 16)
            return fail("Invalid card number. Must be 16 digits.", amount);
        if (!expiry.matches("\\d{2}/\\d{2}"))
            return fail("Invalid expiry format. Use MM/YY.", amount);
        if (cvv.length() < 3)
            return fail("Invalid CVV.", amount);
        if (name.trim().length() < 2)
            return fail("Please enter the cardholder name.", amount);

        simulateProcessing(1000);

        // Cards ending in 0000 are declined (for demo purposes)
        if (cleaned.endsWith("0000"))
            return new PaymentResponse(PaymentResult.DECLINED, null,
                "Payment declined by bank. Please try another card.", amount);

        return new PaymentResponse(PaymentResult.SUCCESS,
            generateTxnId("CARD"), "Payment successful!", amount);
    }

    // Processes UPI payments
    public PaymentResponse processUPI(String upiId, double amount) {
        if (upiId == null || !upiId.contains("@") || upiId.length() < 5)
            return fail("Invalid UPI ID. Format: name@bank", amount);

        simulateProcessing(800);
        return new PaymentResponse(PaymentResult.SUCCESS,
            generateTxnId("UPI"), "UPI Payment successful!", amount);
    }

    // Processes net banking payments
    public PaymentResponse processNetBanking(String bank, double amount) {
        if (bank == null || bank.trim().isEmpty())
            return fail("Please enter your bank name.", amount);

        simulateProcessing(1200);
        return new PaymentResponse(PaymentResult.SUCCESS,
            generateTxnId("NB"), "Net Banking payment successful!", amount);
    }

    // Processes a refund
    public PaymentResponse processRefund(String originalTxnId, double refundAmount) {
        simulateProcessing(600);
        return new PaymentResponse(PaymentResult.SUCCESS,
            generateTxnId("REF"),
            String.format("Refund of Rs.%.2f processed successfully!", refundAmount),
            refundAmount);
    }

    private PaymentResponse fail(String msg, double amount) {
        return new PaymentResponse(PaymentResult.INVALID_CARD, null, msg, amount);
    }

    private String generateTxnId(String prefix) {
        long ts  = System.currentTimeMillis() % 1000000;
        int  rnd = (int)(Math.random() * 9000) + 1000;
        return prefix + ts + rnd;
    }

    private void simulateProcessing(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}