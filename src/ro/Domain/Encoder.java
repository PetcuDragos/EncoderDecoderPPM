package ro.Domain;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Encoder {

    private List<List<Double>> Y_matrix;
    private List<List<Double>> Cb_matrix;
    private List<List<Double>> Cr_matrix;
    private List<Pixel> pixelList;
    private List<Block> blockList;
    private int resolutionWidth;
    private int resolutionHeight;

    public Encoder(int resolutionWidth, int resolutionHeight, List<Pixel> pixelList) {
        Y_matrix = new ArrayList<>();
        Cb_matrix = new ArrayList<>();
        Cr_matrix = new ArrayList<>();
        blockList = new ArrayList<>();
        this.pixelList = pixelList;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;

    }

    public void formMatrices() {

        for (int i = 0; i < resolutionHeight; i++) {
            Y_matrix.add(new ArrayList<>());
            Cb_matrix.add(new ArrayList<>());
            Cr_matrix.add(new ArrayList<>());
        }
        int index = 0;
        while (index < resolutionWidth * resolutionHeight) {
            int row = index / resolutionWidth;
            if (index < pixelList.size()) {
                Y_matrix.get(row).add(pixelList.get(index).getLuminance());
                Cb_matrix.get(row).add(pixelList.get(index).getChrominance_blue());
                Cr_matrix.get(row).add(pixelList.get(index).getChrominance_red());
            } else {
                Y_matrix.get(row).add(0.0);
                Cb_matrix.get(row).add(0.0);
                Cr_matrix.get(row).add(0.0);
            }
            index += 1;
        }
    }

    public void rescaleImage() {
        // making them multiple of 8
        if (this.resolutionWidth % 8 != 0) {
            int difference = (8 - resolutionWidth % 8);
            resolutionWidth += difference;
            for (int i = 0; i < resolutionHeight; i++) {
                for (int j = 0; j < difference; j++) {
                    Y_matrix.get(i).add(0.0);
                    Cb_matrix.get(i).add(0.0);
                    Cr_matrix.get(i).add(0.0);
                }
            }
        }
        if (this.resolutionHeight % 8 != 0) {
            int difference = (8 - resolutionHeight % 8);
            resolutionHeight += difference;
            for (int i = resolutionHeight; i < resolutionHeight + difference; i++) {
                Y_matrix.add(new ArrayList<>());
                Cb_matrix.add(new ArrayList<>());
                Cr_matrix.add(new ArrayList<>());
                for (int j = 0; j < resolutionWidth; j++) {
                    Y_matrix.get(i).add(0.0);
                    Cb_matrix.get(i).add(0.0);
                    Cr_matrix.get(i).add(0.0);
                }
            }
        }
    }

    public void divideMatrixY() {
        for (int row = 0; row < resolutionHeight; row += 8) {
            for (int column = 0; column < resolutionWidth; column += 8) {
                Block block = new Block(new ArrayList<>(), 'Y', row, column, row + 8, column + 8);
                for (int i = row; i < row + 8; i++) {
                    for (int j = column; j < column + 8; j++) {
                        block.getValues().add(Y_matrix.get(i).get(j));
                    }
                }
                blockList.add(block);
            }
        }
    }

    public void divideMatrixCb() {
        for (int row = 0; row < resolutionHeight; row += 8) {
            for (int column = 0; column < resolutionWidth; column += 8) {
                Block block = new Block(new ArrayList<>(), 'U', row, column, row + 8, column + 8);
                for (int i = row; i < row + 8; i += 2) {
                    for (int j = column; j < column + 8; j += 2) {
                        double average = (Cb_matrix.get(i).get(j) +
                                Cb_matrix.get(i).get(j + 1) +
                                Cb_matrix.get(i + 1).get(j) +
                                Cb_matrix.get(i + 1).get(j + 1)) / 4;
                        block.getValues().add(average);
                    }
                }
                blockList.add(block);
            }
        }
    }

    public void divideMatrixCr() {
        for (int row = 0; row < resolutionHeight; row += 8) {
            for (int column = 0; column < resolutionWidth; column += 8) {
                Block block = new Block(new ArrayList<>(), 'V', row, column, row + 8, column + 8);
                for (int i = row; i < row + 8; i += 2) {
                    for (int j = column; j < column + 8; j += 2) {
                        double average = (Cr_matrix.get(i).get(j) +
                                Cr_matrix.get(i).get(j + 1) +
                                Cr_matrix.get(i + 1).get(j) +
                                Cr_matrix.get(i + 1).get(j + 1)) / 4;
                        block.getValues().add(average);
                    }
                }
                blockList.add(block);
            }
        }
    }

    public void writeToFile(String fileName) {
        // creating a file for output
        try {
            FileWriter myWriter = new FileWriter(fileName);
            String s0 = this.resolutionWidth + " " + resolutionHeight + "\n";
            String s1 = this.blockList.stream().map(s -> {
                return s.toString() + "\n";
            }).collect(Collectors.joining());
            myWriter.write(s0 + s1);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


}
