package org.com.ivangeorgiev.lockbox.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {

    public static Boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }
}
