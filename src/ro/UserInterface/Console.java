package ro.UserInterface;

import ro.Service.ImageService;

import java.util.Scanner;

public class Console {

    private ImageService imageService;

    public Console(ImageService imageService) {
        this.imageService = imageService;
    }

    public void start() {
        int choice = -1;
        Scanner s = new Scanner(System.in);
        while (choice != 0) {
            System.out.println("0 - Exit");
            System.out.println("1 - Encode image");
            System.out.println("2 - Decode image");
            choice = Integer.parseInt(s.nextLine());
            if (choice == 1) encodeImage();
            else if (choice == 2) decodeImage();
        }
    }

    private void encodeImage() {
        Scanner s = new Scanner(System.in);
        System.out.println("\nPlease enter the name of the file you want to encode.\n");
        String fileName = s.nextLine();
        this.imageService.encodeImage(fileName);
        System.out.println("Image encoded");
    }

    private void decodeImage() {
        Scanner s = new Scanner(System.in);
        System.out.println("\nPlease enter the name of the file you want to decode.\n");
        String fileName = s.nextLine();
        this.imageService.decodeImage(fileName);
        System.out.println("Image decoded");
    }
}
