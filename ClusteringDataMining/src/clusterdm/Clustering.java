package clusterdm;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class Clustering {

    int numberOfClusters = 0;

    public void setNumberOfClusters(int n) {
        this.numberOfClusters = n;
    }

    public int getNumberOfClusters() {
        return this.numberOfClusters;
    }

    List<List<Integer>> getScoresData() {
        ReadExcelFile readFile = new ReadExcelFile();
        return readFile.readCSV();
    }

    public List<List<Integer>> scores = new ArrayList<>(getScoresData());
    public List<List<Integer>> initialCentroidIntegers = new ArrayList<>();
    public List<List<Float>> initialCentroid = new ArrayList<>();
    public Set<Integer> indexOfOutliers = new HashSet<>();
    float maximumDistance;

    public void setMaxDistance(float md) {
        this.maximumDistance = md;
    }

    public boolean ListsContainsList(List<List<Integer>> Lists, List<Integer> list) {
        for (int row = 0; row < Lists.size(); row++) {
            int counter = 0;
            for (int col = 2; col < Lists.get(row).size(); col++) {
                if (Objects.equals(Lists.get(row).get(col), list.get(col))) {
                    counter++;
                }
            }
            if (counter == list.size() - 2) {
                return true;
            }
        }
        return false;
    }

    public void setInitialCentroidIntegers() {
        Random rand = new Random();
        int firstRandom = rand.nextInt(scores.size());
        initialCentroidIntegers.add(scores.get(firstRandom));
        int randomNumber;

        while (initialCentroidIntegers.size() < getNumberOfClusters()) {
            randomNumber = rand.nextInt(scores.size());
            if (!(ListsContainsList(initialCentroidIntegers, scores.get(randomNumber)))) {
                initialCentroidIntegers.add(scores.get(randomNumber));
            }
        }
    }

    public void setInitialCentroids() {
        setInitialCentroidIntegers();
        for (int row = 0; row < initialCentroidIntegers.size(); row++) {
            List<Float> rowFloatValuesBuffer = new ArrayList<>();
            Float temp;
            for (int col = 1; col < initialCentroidIntegers.get(row).size(); col++) {
                temp = (float) initialCentroidIntegers.get(row).get(col);
                rowFloatValuesBuffer.add(temp);
            }
            initialCentroid.add(rowFloatValuesBuffer);
        }
    }

    public Float calculateEculideanDistance(List<Integer> currentPoint, List<Float> centroid) {

        Float euclideanDistance = (float) 0;
        Float summation = (float) 0;

        for (int col = 0; col < centroid.size(); col++) {
            summation = (float) (summation + Math.pow(currentPoint.get(col) - centroid.get(col), 2));
            col++;
        }
        euclideanDistance = (float) Math.sqrt(summation);
        return euclideanDistance;
    }

    @SuppressWarnings("unlikely-arg-type")
    public List<List<Integer>> getClusteredScores(List<List<Float>> centroid_, boolean finalClustered) {
        List<List<Integer>> indexOfClusters = new ArrayList<List<Integer>>();

        for (int row = 0; row < scores.size(); row++) {
            Float[] distances = new Float[centroid_.size()];
            Float euclideanDistance;
            int closestCluster = 1;

            List<Integer> buffer = new ArrayList<Integer>();
            for (int cluster = 0; cluster < centroid_.size(); cluster++) {
                List<Integer> currentPoint = new ArrayList<Integer>();
                for (int column = 1; column < scores.get(row).size(); column++) {
                    currentPoint.add(scores.get(row).get(column));
                }

                euclideanDistance = calculateEculideanDistance(currentPoint, centroid_.get(cluster));
                distances[cluster] = euclideanDistance;
            }

            Float minimumDistance = (float) 999999;

            for (int i = 0; i < distances.length; i++) {
                if (Float.compare(distances[i], minimumDistance) < 0) {
                    minimumDistance = distances[i];
                    closestCluster = i + 1;
                }
                if (finalClustered) {

                    if (Float.compare(distances[i], maximumDistance) > 0) {
                        if (!(indexOfClusters.contains(row))) {
                            indexOfOutliers.add(row);
                        }
                    }
                }
            }
            buffer.add(closestCluster);
            buffer.add(row);
            indexOfClusters.add(buffer);
        }

        List<List<Integer>> clusteredScores = new ArrayList<List<Integer>>();
        int currentRow = 0;

        for (int cluster = 1; cluster <= centroid_.size(); cluster++) {
            for (int j = 0; j < indexOfClusters.size(); j++) {
                if (indexOfClusters.get(j).get(0) == cluster) {
                    currentRow = indexOfClusters.get(j).get(1);
                    List<Integer> newBuffer = new ArrayList<Integer>();
                    newBuffer.add(cluster);
                    newBuffer.addAll(1, scores.get(currentRow));
                    clusteredScores.add(newBuffer);
                }

            }
        }
        
        return clusteredScores;
    }

    public List<List<Float>> updatedCentroids(List<List<Float>> previousCentroid) {
        List<List<Integer>> clusteredScores = new ArrayList<List<Integer>>();
        clusteredScores = getClusteredScores(previousCentroid, false);
        int numberOfAllClustersRows = clusteredScores.size();
        int numberOfAllClustersColumns = clusteredScores.get(0).size();
        List<List<Float>> newCentroids = new ArrayList<List<Float>>();

        for (int NofCent = 1; NofCent <= previousCentroid.size(); NofCent++) {
            List<Float> bufferForEachCluster = new ArrayList<Float>();

            for (int col = 2; col < numberOfAllClustersColumns; col++) {
                Float summation = (float) 0;
                int counter = 0;
                for (int row = 0; row < numberOfAllClustersRows; row++) {
                    if (clusteredScores.get(row).get(0) == NofCent) {
                        summation += clusteredScores.get(row).get(col);
                        counter++;
                    }
                }
                Float mean = (Float) summation / counter;
                if (Float.isNaN(mean)) {
                    mean = (float) 0.0;
                }

                bufferForEachCluster.add(mean);
            }
            newCentroids.add(bufferForEachCluster);
        }
        return newCentroids;
    }

    public boolean checkChangeOfCentroids(List<List<Float>> cent1, List<List<Float>> cent2) {
        List<List<Integer>> clus2, clus1 = new ArrayList<List<Integer>>();
        clus1 = getClusteredScores(cent1, false);
        clus2 = getClusteredScores(cent2, false);
        int numberOfRows = clus1.size();
        for (int row = 0; row < numberOfRows; row++) {
            if (clus1.get(row).get(0) != clus2.get(row).get(0) || clus1.get(row).get(1) != clus2.get(row).get(1)) {
                return true;
            }
        }
        return false;
    }

    public List<List<Float>> getFinalCentroid() {
        List<List<Float>> finalCentroid = new ArrayList<List<Float>>();
        List<List<Float>> previousCentroid = new ArrayList<List<Float>>();
        List<List<Float>> currentCentroid = new ArrayList<List<Float>>();
        previousCentroid = updatedCentroids(initialCentroid);
        currentCentroid = updatedCentroids(previousCentroid);
        while (checkChangeOfCentroids(previousCentroid, currentCentroid)) {
            previousCentroid = currentCentroid;
            currentCentroid = updatedCentroids(previousCentroid);
        }
        finalCentroid = currentCentroid;

        return finalCentroid;
    }

    public void printOutlieres() {
        if (indexOfOutliers.size() == 0) {
            System.out.println("\n _______________________________");
            System.out.println("\n There is no Outlieres ....\n ");
        } else {
            System.out.println("\n \n Outlieres is : ");
            for (int row : indexOfOutliers) {
                System.out.println(scores.get(row));
            }

            System.out.println("\n _______________________________");
        }
    }
}
