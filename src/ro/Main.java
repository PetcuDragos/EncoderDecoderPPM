package ro;

import ro.Repository.FileImageRepository;
import ro.Service.ImageService;
import ro.UserInterface.Console;

public class Main {

    public static void main(String[] args) {
        FileImageRepository repository = new FileImageRepository();
        ImageService service = new ImageService(repository);
        Console console = new Console(service);
        console.start();
    }
}
