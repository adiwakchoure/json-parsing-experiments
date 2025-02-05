package com.benchmark.json;

import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ParquetDataReader {
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}");
    private static final String INPUT_FILE = "data/input.parquet";

    private static class LocalInputFile implements InputFile {
        private final File file;

        LocalInputFile(File file) {
            this.file = file;
        }

        @Override
        public long getLength() throws IOException {
            return file.length();
        }

        @Override
        public SeekableInputStream newStream() throws IOException {
            return new SeekableInputStream() {
                private final RandomAccessFile input = new RandomAccessFile(file, "r");

                @Override
                public int read() throws IOException {
                    return input.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return input.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return input.read(b, off, len);
                }

                @Override
                public long getPos() throws IOException {
                    return input.getFilePointer();
                }

                @Override
                public void seek(long pos) throws IOException {
                    input.seek(pos);
                }

                @Override
                public void readFully(byte[] bytes) throws IOException {
                    input.readFully(bytes);
                }

                @Override
                public void readFully(byte[] bytes, int start, int len) throws IOException {
                    input.readFully(bytes, start, len);
                }

                @Override
                public void readFully(ByteBuffer buf) throws IOException {
                    byte[] bytes = new byte[buf.remaining()];
                    readFully(bytes);
                    buf.put(bytes);
                }

                @Override
                public void close() throws IOException {
                    input.close();
                }
            };
        }
    }

    public List<String> readJsonStrings() throws Exception {
        List<String> jsonStrings = new ArrayList<>();
        
        File inputFile = new File(INPUT_FILE);
        if (!inputFile.exists()) {
            throw new IOException("Input file not found: " + INPUT_FILE);
        }

        ParquetFileReader reader = ParquetFileReader.open(new LocalInputFile(inputFile));
        MessageType schema = reader.getFooter().getFileMetaData().getSchema();
        MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
        
        PageReadStore pages;
        while ((pages = reader.readNextRowGroup()) != null) {
            long rows = pages.getRowCount();
            RecordReader<Group> recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));
            
            for (int i = 0; i < rows; i++) {
                Group group = recordReader.read();
                String jsonStr = group.getString("Body", 0);
                jsonStrings.add(jsonStr);
            }
        }
        reader.close();
        
        return jsonStrings;
    }

    public static void main(String[] args) throws Exception {
        ParquetDataReader reader = new ParquetDataReader();
        List<String> jsonStrings = reader.readJsonStrings();
        System.out.println("Read " + jsonStrings.size() + " JSON strings from input.parquet");
        if (!jsonStrings.isEmpty()) {
            System.out.println("First JSON string: " + jsonStrings.get(0));
        }
    }
}
