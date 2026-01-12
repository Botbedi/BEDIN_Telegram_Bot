package Model;

import java.time.LocalDate;

public class Data {
    public static String oggi() {
        return LocalDate.now().toString();
    }
}
