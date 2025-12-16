import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

public class MyConfiguration {
    private static MyConfiguration instance;
    private Configurations configs=new Configurations();
    private Configuration config;
    private MyConfiguration() {
        try{
            config = configs.properties("config.properties");
        }catch(Exception e){
            System.err.println(e.getMessage() + " File non disponibile");
            System.exit(-1);
        }
    }
    public static MyConfiguration getInstance() {
        if (instance == null) {
            instance = new MyConfiguration();
        }
        return instance;
    }
    public String getProperty(String key) {
        return config.getString(key);
    }
}
