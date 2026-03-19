import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestValue {
    // Escaping the $ so it's not resolved by bash but compiled correctly
    @Value("${DB_HOST_TEST:#{null}}")
    private String dbHost;

    @Value("${DB_PORT_TEST:5432}")
    private String dbPort;

    @Value("${DB_NAME_TEST:#{null}}")
    private String dbName;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestValue.class);
        TestValue tv = context.getBean(TestValue.class);
        System.out.println("dbHost=" + tv.dbHost);
        System.out.println("dbPort=" + tv.dbPort);
        System.out.println("dbName=" + tv.dbName);
        
        System.out.println("Result URL: jdbc:postgresql://" + tv.dbHost + ":" + tv.dbPort + "/" + tv.dbName);
    }
}
