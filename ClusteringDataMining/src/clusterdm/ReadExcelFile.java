package clusterdm;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

class ReadExcelFile {

    List<List<Integer>> allData = new ArrayList<List<Integer>>();

    public List<List<Integer>> readCSV() {

        try {
            FileReader filereader = new FileReader("Course Evaluation.csv");
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

            List<String[]> allDataStrings = new ArrayList<>();
            allDataStrings = csvReader.readAll();

            for (int i = 0; i < allDataStrings.size(); i++) {
                String currentLine = Arrays.toString(allDataStrings.get(i));
                String currentLineNew
                        = currentLine.substring(currentLine.indexOf('[') + 1,
                                currentLine.lastIndexOf(']'));
                String[] valuesOfEachLine = currentLineNew.split(";");
                List<Integer> buffer = new ArrayList<Integer>();
                for (int j = 0; j < valuesOfEachLine.length; j++) {
                    int tempInt = Integer.parseInt(valuesOfEachLine[j]);

                    buffer.add(tempInt);
                }
                allData.add(buffer);
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }

}
