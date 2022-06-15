import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

public final class Connection {
    private static final String TOKEN = "<token to access InfluxDB>";
    public static final String BUCKET = "<Bucket>";
    public static final String ORG = "<user e-mail to access>";
    public static final InfluxDBClient CLIENT = InfluxDBClientFactory.create("<URL>", TOKEN.toCharArray());
}
