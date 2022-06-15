import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Run {
    public static void main(final String[] args) {
        final String measurement = "<Measurement>";
        final String fileData = "<Your csv file>";

        // Insert
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileData))) {
            bufferedReader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .map(line -> Point
                            .measurement(measurement)
                            .addTag("football_player", "messi")
                            .addTag("sport", "soccer")
                            .addField("matchday", StringUtils.isNumeric(line[2]) ? Integer.parseInt(line[2]) : null)
                            .addField("local", line[4].contains("H") ? 1 : 2)
                            .addField("minute", Integer.parseInt(StringUtils.leftPad(line[9], 2, "0").substring(0, 2)))
                            .addField("left_or_right_footed", line[11].toUpperCase().contains("LEFT") ? Integer.valueOf(1) : line[11].toUpperCase().contains("RIGHT") ? 2 : null)
                            .time(
                                    LocalDate.parse(
                                                    Stream.of(line[3].split("/"))
                                                            .map(item -> StringUtils.leftPad(item, 2, "0"))
                                                            .collect(Collectors.joining("/")),
                                                    DateTimeFormatter.ofPattern("MM/dd/yy")
                                            )
                                            .atStartOfDay()
                                            .toInstant(ZoneOffset.UTC),
                                    WritePrecision.NS
                            )
                    )
                    .forEach(point -> {
                        Connection.CLIENT.getWriteApiBlocking().writePoint(Connection.BUCKET, Connection.ORG, point);
                        System.out.println("Inserted point: " + point.toLineProtocol());
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

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
