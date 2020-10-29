package ro.Domain;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Decoder {

    private List<List<Double>> Y_matrix;
    private List<List<Double>> Cb_matrix;
    private List<List<Double>> Cr_matrix;
    private List<Pixel> pixelList;
    private List<Block> blockList;
    private int resolutionWidth;
    private int resolutionHeight;

    public Decoder(int resolutionWidth, int resolutionHeight, List<Block> blockList) {
        Y_matrix = new ArrayList<>();
        Cb_matrix = new ArrayList<>();
        Cr_matrix = new ArrayList<>();
        pixelList = new ArrayList<>();
        this.blockList = blockList;
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
            try (Writer myWriter = new OutputStreamWriter(new FileOutputStream(fileName))) {
                String s0 = "P6\n" + this.resolutionWidth + " " + resolutionHeight + "\n255\n";
                myWriter.write(s0);
                for (Pixel p : this.pixelList) {
                    myWriter.write(p.getRed());
                    myWriter.write(p.getGreen());
                    myWriter.write(p.getBlue());
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

}
