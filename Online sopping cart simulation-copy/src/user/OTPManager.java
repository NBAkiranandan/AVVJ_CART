package user;

public class OTPManager {

    private static String otp;
    private static long expiryTime;

    public static void generateOTP() {
        otp = EmailUtility.generateOTP();
        expiryTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 min
    }

    public static String getOTP() {
        return otp;
    }

    public static boolean verifyOTP(String enteredOTP) {

        if (otp == null)
            return false;

        if (System.currentTimeMillis() > expiryTime) {
            otp = null;
            return false;
        }

        boolean valid = otp.equals(enteredOTP);
        if (valid)
            otp = null;

        return valid;
    }
}
