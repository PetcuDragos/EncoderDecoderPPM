package ro.Domain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Decoder {

    private List<List<Double>> Y_matrix;
    private List<List<Double>> Cb_matrix;
    private List<List<Double>> Cr_matrix;
    private List<Pixel> pixelList;
    private List<Block> blockList;
    private List<Block> dctCoefficientBlockList;
    private List<Block> quantizedBlockList;
    private int resolutionWidth;
    private int resolutionHeight;

    public Decoder(int resolutionWidth, int resolutionHeight, List<Block> blockList) {
        Y_matrix = new ArrayList<>();
        Cb_matrix = new ArrayList<>();
        Cr_matrix = new ArrayList<>();
        pixelList = new ArrayList<>();
        this.blockList = new ArrayList<>();
        dctCoefficientBlockList = new ArrayList<>();
        quantizedBlockList = blockList;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;

        // making them multiple of 8
        this.resolutionWidth = this.resolutionWidth + (8 * Math.min(1, this.resolutionWidth % 8) - this.resolutionWidth % 8);
        this.resolutionHeight = this.resolutionHeight + (8 * Math.min(1, this.resolutionHeight % 8) - this.resolutionHeight % 8);

    }

    public void formMatrices() {
        for (int i = 0; i < resolutionHeight; i++) {
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

    public void transformBlocksToYMatrix() {
        for (Block block : this.blockList) {
            if (block.getTypeOfBlock() == 'Y') {
                int index = 0;
                for (int i = block.getPositionX1(); i < block.getPositionX2(); i++) {
                    for (int j = block.getPositionY1(); j < block.getPositionY2(); j++) {
                        Y_matrix.get(i).set(j, block.getValues().get(index));
                        index++;
                    }
                }
            }
        }
    }

    public void transformBlocksToCbMatrix() {
        for (Block block : this.blockList) {
            if (block.getTypeOfBlock() == 'U') {
                int index = 0;
                for (int i = block.getPositionX1(); i < block.getPositionX2(); i += 2) {
                    for (int j = block.getPositionY1(); j < block.getPositionY2(); j += 2) {
                        Cb_matrix.get(i).set(j, block.getValues().get(index));
                        Cb_matrix.get(i).set(j + 1, block.getValues().get(index));
                        Cb_matrix.get(i + 1).set(j, block.getValues().get(index));
                        Cb_matrix.get(i + 1).set(j + 1, block.getValues().get(index));
                        index++;
                    }
                }
            }
        }
    }

    public void transformBlocksToCrMatrix() {
        for (Block block : this.blockList) {
            if (block.getTypeOfBlock() == 'V') {
                int index = 0;
                for (int i = block.getPositionX1(); i < block.getPositionX2(); i += 2) {
                    for (int j = block.getPositionY1(); j < block.getPositionY2(); j += 2) {
                        Cr_matrix.get(i).set(j, block.getValues().get(index));
                        Cr_matrix.get(i).set(j + 1, block.getValues().get(index));
                        Cr_matrix.get(i + 1).set(j, block.getValues().get(index));
                        Cr_matrix.get(i + 1).set(j + 1, block.getValues().get(index));
                        index++;
                    }
                }
            }
        }
    }

    public void formPixels() {
        for (int i = 0; i < resolutionHeight; i++) {
            for (int j = 0; j < resolutionWidth; j++) {
                this.pixelList.add(new Pixel(Y_matrix.get(i).get(j), Cb_matrix.get(i).get(j), Cr_matrix.get(i).get(j)));
            }
        }
    }

    public void writeToFile(String fileName, String format) {
        if (format.equals("P3")) {
            try (Writer myWriter = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
                String s0 = "P3\n" + this.resolutionWidth + " " + resolutionHeight + "\n255\n";
                String s1 = this.pixelList.stream().map(s -> {
                    return s.toString() + "\n";
                }).collect(Collectors.joining());
                myWriter.write(s0 + s1);
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        } else if (format.equals("P6")) {
            try (DataOutputStream myWriter1 = new DataOutputStream(new FileOutputStream(fileName))) {
                String s0 = "P6\n" + this.resolutionWidth + " " + resolutionHeight + "\n255\n";
                myWriter1.write(s0.getBytes());
                for (Pixel p : this.pixelList) {
                    myWriter1.writeByte(p.getRed());
                    myWriter1.writeByte(p.getGreen());
                    myWriter1.writeByte(p.getBlue());
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
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


    public void performDequantization() {
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


        for (Block block : quantizedBlockList) {

            //transform the block into 8x8 matrix
            List<List<Double>> matrix = transform8x8BlockTo8x8Matrix(block);

            //divide the matrices

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    matrix.get(i).set(j, matrix.get(i).get(j) * quantizationMatrix.get(i).get(j));
                }
            }

            /// form new quantized blocks
            Block newBlock = new Block(new ArrayList<>(), block.getTypeOfBlock(), block.getPositionX1(), block.getPositionY1(), block.getPositionX2(), block.getPositionY2());
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    newBlock.getValues().add(matrix.get(i).get(j));
                }
            }
            dctCoefficientBlockList.add(newBlock);
        }
    }

    public void performInverseDCT() {
        for (Block block : this.dctCoefficientBlockList) {
            /// transforming dct blocks to 8x8 matrices
            List<List<Double>> matrix;
            matrix = transform8x8BlockTo8x8Matrix(block);

            // form new dct coefficient blocks

            Block newBlock = new Block(new ArrayList<>(), block.getTypeOfBlock(), block.getPositionX1(), block.getPositionY1(), block.getPositionX2(), block.getPositionY2());

            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {

                    double sum = 0;
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            double alphaU = 1, alphaV = 1;

                            if (i == 0) alphaU = 1 / Math.sqrt(2);
                            if (j == 0) alphaV = 1 / Math.sqrt(2);
                            sum += alphaU * alphaV * matrix.get(i).get(j) * Math.cos((double)(2 * x + 1) * i * 3.14 * 0.0625) * Math.cos((double)(2 * y + 1) * j * 3.14 * 0.0625);
                        }
                    }
                    if((newBlock.getTypeOfBlock()=='U' || newBlock.getTypeOfBlock()=='V')) {
                        y++;
                        newBlock.getValues().add(0.25 * sum);
                    }
                    else newBlock.getValues().add(0.25 * sum);
                }
                if((newBlock.getTypeOfBlock()=='U' || newBlock.getTypeOfBlock()=='V')) x++;
            }

            // add 128 from each value of matrix
            for (int i = 0; i < newBlock.getValues().size(); i++) {
                newBlock.getValues().set(i, newBlock.getValues().get(i) + 128);
            }

            blockList.add(newBlock);
        }
    }
}
