package ro.Domain;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Encoder {

    private List<List<Double>> Y_matrix;
    private List<List<Double>> Cb_matrix;
    private List<List<Double>> Cr_matrix;
    private List<Pixel> pixelList;
    private List<Block> blockList;
    private List<Block> dctCoefficientBlockList;
    private List<Block> quantizedBlockList;
    private int resolutionWidth;
    private int resolutionHeight;

    public Encoder(int resolutionWidth, int resolutionHeight, List<Pixel> pixelList) {
        Y_matrix = new ArrayList<>();
        Cb_matrix = new ArrayList<>();
        Cr_matrix = new ArrayList<>();
        blockList = new ArrayList<>();
        dctCoefficientBlockList = new ArrayList<>();
        quantizedBlockList = new ArrayList<>();
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
            String s1 = this.quantizedBlockList.stream().map(s -> {
                return s.toString() + "\n";
            }).collect(Collectors.joining());
            myWriter.write(s0 + s1);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public void performForwardDCT() {

        for (Block block : this.blockList) {
            /// transforming y,u,v blocks to 8x8 matrices
            List<List<Double>> matrix;
            if (block.getTypeOfBlock() == 'Y') matrix = transform8x8BlockTo8x8Matrix(block);
            else matrix = transform4x4BlockTo8x8Matrix(block);

            // subtract 128 from each value of matrix
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    matrix.get(i).set(j, matrix.get(i).get(j) - 128);
                }
            }

            // form new dct coefficient blocks

            Block newBlock = new Block(new ArrayList<>(), block.getTypeOfBlock(), block.getPositionX1(), block.getPositionY1(), block.getPositionX2(), block.getPositionY2());

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {

                    double alphaU = 1, alphaV = 1;

                    if (i == 0) alphaU = 1 / Math.sqrt(2);
                    if (j == 0) alphaV = 1 / Math.sqrt(2);

                    double sum = 0;
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            sum += matrix.get(x).get(y) * Math.cos((double)(2 * x + 1) * (i * 3.14 *0.0625)) * Math.cos((double)(2 * y + 1) * j * 3.14 *0.0625);
                        }
                    }

                    double newValue = 0.25 * alphaU * alphaV * sum;
                    newBlock.getValues().add(newValue);
                }
            }

            dctCoefficientBlockList.add(newBlock);
        }
    }


    private List<List<Double>> transform8x8BlockTo8x8Matrix(Block block) {
        List<List<Double>> matrix = new ArrayList<>();
        for (int k = 0; k < 8; k++) {
            matrix.add(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        }
        int index = 0;
        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j ++) {
                matrix.get(i).set(j, block.getValues().get(index));
                index++;
            }
        }
        return matrix;
    }

    private List<List<Double>> transform4x4BlockTo8x8Matrix(Block block) {
        List<List<Double>> matrix = new ArrayList<>();
        for (int k = 0; k < 8; k++) {
            matrix.add(new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        }
        int index = 0;
        for (int i = 0; i < 8; i += 2) {
            for (int j = 0; j < 8; j += 2) {
                matrix.get(i).set(j, block.getValues().get(index));
                matrix.get(i).set(j + 1, block.getValues().get(index));
                matrix.get(i + 1).set(j, block.getValues().get(index));
                matrix.get(i + 1).set(j + 1, block.getValues().get(index));
                index++;
            }
        }
        return matrix;
    }


    public void performQuantization() {
        /*
                6   4   4   6   10  16  20  24
                5   5   6   8   10  23  24  22
                6   5   6   10  16  23  28  22
        Q =     6   7   9   12  20  35  32  25
                7   9   15  22  27  44  41  31
                10  14  22  26  32  42  45  37
                20  26  31  35  41  48  48  40
                29  37  38  39  45  40  41  40
         */

        List<List<Integer>> quantizationMatrix = new ArrayList<>();
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(6, 4, 4, 6, 10, 16, 20, 24)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(5, 5, 6, 8, 10, 23, 24, 22)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(6, 5, 6, 10, 16, 23, 28, 22)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(6, 7, 9, 12, 20, 35, 32, 25)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(7, 9, 15, 22, 27, 44, 41, 31)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(10, 14, 22, 26, 32, 42, 45, 37)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(20, 26, 31, 35, 41, 48, 48, 40)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(29, 37, 38, 39, 45, 40, 41, 40)));


        for (Block block : dctCoefficientBlockList) {

            //transform the block into 8x8 matrix
            List<List<Double>> matrix = transform8x8BlockTo8x8Matrix(block);

            //divide the matrices

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    matrix.get(i).set(j, (double) ((int) (matrix.get(i).get(j) / quantizationMatrix.get(i).get(j))));
                }
            }

            /// form new quantized blocks
            Block newBlock = new Block(new ArrayList<>(), block.getTypeOfBlock(), block.getPositionX1(), block.getPositionY1(), block.getPositionX2(), block.getPositionY2());
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    newBlock.getValues().add(matrix.get(i).get(j));
                }
            }
            quantizedBlockList.add(newBlock);
        }
    }
}
