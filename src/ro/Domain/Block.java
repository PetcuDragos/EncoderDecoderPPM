package ro.Domain;

import java.util.List;
import java.util.stream.Collectors;

public class Block {

    private List<Double> values;
    private char typeOfBlock;
    private int positionX1, positionY1, positionX2, positionY2;

    public Block(List<Double> values, char typeOfBlock, int positionX1, int positionY1, int positionX2, int positionY2) {
        this.values = values;
        this.typeOfBlock = typeOfBlock;
        this.positionX1 = positionX1;
        this.positionX2 = positionX2;
        this.positionY1 = positionY1;
        this.positionY2 = positionY2;
    }


    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public char getTypeOfBlock() {
        return typeOfBlock;
    }

    public void setTypeOfBlock(char typeOfBlock) {
        this.typeOfBlock = typeOfBlock;
    }

    public int getPositionX1() {
        return positionX1;
    }

    public void setPositionX1(int positionX1) {
        this.positionX1 = positionX1;
    }

    public int getPositionY1() {
        return positionY1;
    }

    public void setPositionY1(int positionY1) {
        this.positionY1 = positionY1;
    }

    public int getPositionX2() {
        return positionX2;
    }

    public void setPositionX2(int positionX2) {
        this.positionX2 = positionX2;
    }

    public int getPositionY2() {
        return positionY2;
    }

    public void setPositionY2(int positionY2) {
        this.positionY2 = positionY2;
    }

    @Override
    public String toString() {
        return typeOfBlock + " " + positionX1 + " " + positionY1 + " " + positionX2 + " " + positionY2 + " " + values.stream().map(v -> (v.floatValue()) + " ").collect(Collectors.joining());
    }
}
