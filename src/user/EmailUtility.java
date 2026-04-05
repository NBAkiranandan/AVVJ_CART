package user;

import java.util.Properties;
import java.security.SecureRandom;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtility {

    private static final String SMTP_AUTH_USER = "avvjcart315@gmail.com";
    // ⚠️ PLEASE USE YOUR 16-LETTER GOOGLE APP PASSWORD BELOW, NOT YOUR REAL
    // PASSWORD!
    private static final String SMTP_AUTH_PWD = "ufar tdid ekqr xjiq";

    public static boolean sendOTP(String recipientEmail, String otp) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // This stops the PKIX path building SSL error that was crashing the emails:
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_AUTH_USER));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));

            message.setSubject("AVVJ Cart - OTP Verification");
            message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");

            Transport.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}