package ro.Service;

import ro.Domain.Decoder;
import ro.Domain.Encoder;
import ro.Repository.FileImageRepository;

public class ImageService {

    private FileImageRepository imageRepository;

    public ImageService(FileImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void encodeImage(String fileName) {
        this.imageRepository.getEncodedDataFromBinFile(fileName);
        Encoder encoder = new Encoder(this.imageRepository.getResolutionWidth(), this.imageRepository.getResolutionHeight(), this.imageRepository.getPixelList());
        encoder.formMatrices();
        encoder.rescaleImage();
        encoder.divideMatrixY();
        encoder.divideMatrixCb();
        encoder.divideMatrixCr();
        encoder.performForwardDCT();
        encoder.performQuantization();
        encoder.entropyEncoding();
        //encoder.writeToFile("outputEncoder.txt");
        encoder.writeOutput("outputEncoder.txt");
    }

    public void decodeImage(String fileName) {
        this.imageRepository.getDecodedDataFromFile2(fileName);
        //Decoder decoder = new Decoder(imageRepository.getResolutionWidth(), imageRepository.getResolutionHeight(), imageRepository.getBlockList());
        Decoder decoder = new Decoder(imageRepository.getResolutionWidth(), imageRepository.getResolutionHeight(), imageRepository.getOutput());
        decoder.entropyDecoding();
        decoder.performDequantization();
        decoder.performInverseDCT();
        decoder.formMatrices();
        decoder.transformBlocksToYMatrix();
        decoder.transformBlocksToCbMatrix();
        decoder.transformBlocksToCrMatrix();
        decoder.formPixels();
        decoder.writeToFile("outputDecoderP3.ppm", "P3");
        decoder.writeToFile("outputDecoderP6.ppm", "P6");
    }
}
