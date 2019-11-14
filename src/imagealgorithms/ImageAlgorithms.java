/*
 *From GitHub
 */
package imagealgorithms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 *
 * @author mgoetzmann
 */
public class ImageAlgorithms extends Application {
    // Inconsistent with when it works. If you see the "Browse JavaFX Application Classes" as an empty list with no options
    // just click around in the project file navigator for a bit and it should work on the next run

    static BufferedImage sourceImage;
    static BufferedImage destinationImage;
    static int imageHeight;
    static int imageWidth;

    @Override
    public void start(Stage primaryStage) {
        ImageView imgView = new ImageView();

        Button fileSelect = new Button();
        fileSelect.setText("Choose File");
        fileSelect.setOnAction((e) -> {
            try {
                chooseImage(e, imgView);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ImageAlgorithms.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ImageAlgorithms.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Button filter1 = new Button();
        filter1.setText("Box Blur");
        filter1.setOnAction((e) -> {
            boxBlur(imgView);
        });

        TextField blurFactor = new TextField();
        blurFactor.setMaxWidth(70);
        blurFactor.setPromptText("blur factor");
        blurFactor.setText("3");
        Button filter2 = new Button();
        filter2.setText("Gaussian Blur");
        filter2.setOnAction((e) -> {
            gaussianBlur(imgView, blurFactor);
        });

        VBox menu = new VBox();
        menu.getChildren().add(fileSelect);
        menu.getChildren().add(filter1);
        menu.getChildren().add(blurFactor);
        menu.getChildren().add(filter2);

        StackPane root = new StackPane();
        root.getChildren().add(imgView);
        root.getChildren().add(menu);

        Scene scene = new Scene(root, 700, 500);

        primaryStage.setTitle("Image Manipulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void chooseImage(ActionEvent e, ImageView iv) throws FileNotFoundException, IOException {
        FileChooser fc = new FileChooser();
        File tmp = fc.showOpenDialog(((Node) e.getTarget()).getScene().getWindow());
        if (tmp != null) {
            System.out.println("Image loaded");
            sourceImage = ImageIO.read(tmp);
            destinationImage = sourceImage; // Not redundant I swear
            imageHeight = sourceImage.getHeight() - 1;
            imageWidth = sourceImage.getWidth() - 1;
            iv.setImage(updateDisplay());
            System.out.println(sourceImage.getHeight() + " height of image | " + sourceImage.getWidth() + " width of image");
        }
    }

    public static Image updateDisplay() {
        sourceImage = destinationImage;
        return SwingFXUtils.toFXImage(sourceImage, null); // Takes buffered image and converts it back to an ImageView displayable image
    }

    public static void boxBlur(ImageView iv) {
        if (iv.getImage() == null) {
            return;
        }
        int[][] kernel;
        // TODO: x and y variable names instead of width and height
        for (int height = 0; height <= imageHeight; height++) {
            for (int width = 0; width <= imageWidth; width++) {
                // TODO: Optimize 99.9 percent of calculations by isolating edge cases in a seperate function
                kernel = new int[][]{ // Contruct RGB array to manipulate. Edges are accounted for in construction
                    {(height == 0 || width == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width - 1, height - 1), (height == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width, height - 1), (height == 0 || width >= imageWidth) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width + 1, height - 1)},
                    {(width == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width - 1, height), sourceImage.getRGB(width, height), (width == imageWidth) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width + 1, height)},
                    {(height >= imageHeight || width == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width - 1, height + 1), (height >= imageHeight) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width, height + 1), (height >= imageHeight || width >= imageWidth) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width + 1, height + 1)}
                };
                int redAvg = 0, blueAvg = 0, greenAvg = 0;
                for (int i[] : kernel) {
                    for (int j : i) {
                        redAvg += getRed(j);
                        greenAvg += getGreen(j);
                        blueAvg += getBlue(j);
                    }
                }
                redAvg /= 9; // 9 is size of kernel
                greenAvg /= 9;
                blueAvg /= 9;
                destinationImage.setRGB(width, height, 65536 * redAvg + 256 * greenAvg + blueAvg); // setRGB() takes the integer value of an rgb color
            }
        }
        // TODO: Why does ImageView not display after blur of image bigger than 1000 x 1000 ish 
        iv.setImage(updateDisplay());
        System.out.println("Simple Blur complete!");
    }

    public void gaussianBlur(ImageView iv, TextField tf) {
        final int DEFAULT_BLUR_MAGNITUDE = 3;
        int magnitude = DEFAULT_BLUR_MAGNITUDE;
        try {
            magnitude = Integer.parseInt(tf.getText());
        } catch (NumberFormatException exception) {
            System.out.println("\"" + tf.getText() + "\" input was not an integer");
            tf.setText("3");
        }
        if (iv.getImage() == null) {
            return;
        }
        // Do a pass only along the vertical axis first. The full Gaussian Blur can be achieved through two passes
        for (int height = 0; height <= imageHeight; height++) {
            for (int width = 0; width <= imageWidth; width++) {
                // TODO

                //
            }
        }
        iv.setImage(updateDisplay());
        System.out.println("Gaussian Blur complete!");
    }

    // Bitwise operations that java.util.Color does with getRed() methods from an integer representation of rgb. Impletementing these directly eliminates "Color" object creation
    // https://stackoverflow.com/questions/2615522/java-bufferedimage-getting-red-green-and-blue-individually
    public static int getRed(int n) {
        return (n >> 16) & 0xFF;
    }

    public static int getGreen(int n) {
        return (n >> 8) & 0xFF;
    }

    public static int getBlue(int n) {
        return n & 0xFF;
    }

    public long gaussianFunction(int x) {
        //TODO
        float blurFactor = x / 10;
        
        
        //
        // Return the ratio at which to contribute the pixel
        return 0;
    }
}
