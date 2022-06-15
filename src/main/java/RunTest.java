import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

public class RunTest {
    public static void main(final String[] args) {
        final String measurement = "test";

        // Insert
        final Point point = Point
                .measurement(measurement)
                .addTag("tagkey1", "tagvalue1")
                .addTag("tagkey2", "tagvalue2")
                .addField("field1", 52)
                .addField("field2", 38.586)
                .time(Instant.now(), WritePrecision.NS);
        final Point point2 = Point
                .measurement(measurement)
                .addTag("tagkey1", "tagvalue1")
                .addTag("tagkey2", "tagvalue2")
                .addField("field1", 85)
                .addField("field2", 996)
                .time(Instant.now(), WritePrecision.NS);
        Connection.CLIENT.getWriteApiBlocking().writePoints(Connection.BUCKET, Connection.ORG, List.of(point, point2));

        // Delete
        Connection.CLIENT.getDeleteApi().delete(
                OffsetDateTime.now().minus(1, ChronoUnit.HOURS),
                OffsetDateTime.now(),
                String.format("_measurement=\"%s\"", measurement),
                Connection.BUCKET,
                Connection.ORG
        );

        // Consult
        final String query = String.format("from(bucket: \"%s\") |> range(start: -1h)", Connection.BUCKET);
        Connection.CLIENT.getQueryApi()
                .query(query, Connection.ORG)
                .stream()
                .map(FluxTable::getRecords)
                .flatMap(Collection::stream)
                .forEach(record -> {
                    System.out.println("---------------------");
                    System.out.println(record.getMeasurement());
                    System.out.println(record.getField());
                    System.out.println(record.getValue());
                    System.out.println(record.getTime());
                    System.out.println("---------------------\n");
                });

        Connection.CLIENT.close();
    }
}
