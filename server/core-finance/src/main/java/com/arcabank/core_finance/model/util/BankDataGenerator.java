package com.arcabank.core_finance.model.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class BankDataGenerator {
    private static final String MFO = "322001"; // Inter-branch turnover ArcaBank
    private static final String COUNTRY_CODE = "UA";
    private static final String BIN_VISA = "414949"; // Bank Identification for Visa
    private static final Random RANDOM = new Random();

    // Generate IBAN
    public static String generateIban() {
        StringBuilder accountDigits = new StringBuilder();
        for (int i = 0; i < 19; i++) {
            accountDigits.append(RANDOM.nextInt(10));
        }

        String controlDigits = String.format("%2d", RANDOM.nextInt(90) + 10);

        return COUNTRY_CODE + controlDigits + MFO + accountDigits;
    }

    // Generate PAN
    public static String generatePan() {
        StringBuilder panBuilder = new StringBuilder(BIN_VISA);

        for (int i = 0; i < 9; i++) {
            panBuilder.append(RANDOM.nextInt(10));
        }

        String panWithoutCheckSum = panBuilder.toString();
        int checkSum = calculateLunaChecksum(panWithoutCheckSum);
        panBuilder.append(checkSum);
        return panBuilder.toString();
    }

    // Luna algorithm
    private static int calculateLunaChecksum(String panWithoutChecksum) {
        int sum = 0;
        boolean alternate = true;

        for (int i = panWithoutChecksum.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(panWithoutChecksum.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        int checksum = 10 - (sum % 10);
        return (checksum == 10) ? 0 : checksum;
    }

    // Card validity period
    public static String generateExpirationDate() {
        LocalDate expDate = LocalDate.now().plusYears(4);
        return expDate.format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    // CVV
    public static String generateCvv() {
        int cvv = RANDOM.nextInt(900) + 100;
        return String.valueOf(cvv);
    }

}
