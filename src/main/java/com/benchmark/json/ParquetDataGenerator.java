package com.benchmark.json;

import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.util.HadoopOutputFile;

import java.io.File;

public class ParquetDataGenerator {
    private static final String SCHEMA = "message Log {\n" +
            "  required binary Body (UTF8);\n" +
            "}";

    public static void main(String[] args) throws Exception {
        // Create schema
        MessageType schema = MessageTypeParser.parseMessageType(SCHEMA);
        Configuration conf = new Configuration();
        GroupWriteSupport.setSchema(schema, conf);

        // Create Parquet writer
        File outputFile = new File("input.parquet");
        if (outputFile.exists()) {
            outputFile.delete();
        }

        ParquetWriter<Group> writer = ExampleParquetWriter.builder(HadoopOutputFile.fromPath(
                new org.apache.hadoop.fs.Path(outputFile.getAbsolutePath()), conf))
                .withType(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();

        // Create a factory for creating groups
        SimpleGroupFactory groupFactory = new SimpleGroupFactory(schema);

        // Generate sample JSON data
        String sampleJson = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";

        // Write sample data
        for (int i = 0; i < 10000; i++) {
            Group group = groupFactory.newGroup();
            group.add("Body", sampleJson);
            writer.write(group);
        }

        writer.close();
        System.out.println("Generated Parquet file with sample data");
    }
}
