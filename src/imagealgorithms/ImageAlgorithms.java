/*
 *From GitHub
 */
package imagealgorithms;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    static BufferedImage originalImage;
    static int imageHeight;
    static int imageWidth;
    static final int DEFAULT_BLUR_MAGNITUDE = 2;

    @Override
    public void start(Stage primaryStage) {
        ImageView imgView = new ImageView();

        // File Select Button
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

        // Reset Image to Uneditted Button
        Button reset = new Button();
        reset.setText("Revert to Original");
        reset.setOnAction((e) -> {
            resetImage(imgView);
        });

        // 3x3 Box Blur Button
        Button filter1 = new Button();
        filter1.setText("Box Blur");
        filter1.setOnAction((e) -> {
            boxBlur(imgView);
        });

        // Text Entry Field that takes integers and Gaussian Blur Button that runs the algorithm using the given parameter
        TextField blurFactor = new TextField();
        blurFactor.setMaxWidth(70);
        blurFactor.setPromptText("blur factor");
        blurFactor.setText(Integer.toString(DEFAULT_BLUR_MAGNITUDE));
        Button filter2 = new Button();
        filter2.setText("Gaussian Blur");
        filter2.setOnAction((e) -> {
            gaussianBlur(imgView, blurFactor);
        });

        // Edge Detection Button
        Button filter3 = new Button();
        filter3.setText("Edge Finding");
        filter3.setOnAction((e) -> {
            findEdges(imgView);
        });

        VBox menu = new VBox();
        menu.getChildren().add(fileSelect);
        menu.getChildren().add(reset);
        menu.getChildren().add(filter1);
        menu.getChildren().add(blurFactor);
        menu.getChildren().add(filter2);
        menu.getChildren().add(filter3);

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

    public static void chooseImage(ActionEvent e, ImageView iv) throws FileNotFoundException, IOException {
        FileChooser fc = new FileChooser();
        File tmp = fc.showOpenDialog(((Node) e.getTarget()).getScene().getWindow());
        if (tmp != null) {
            System.out.println("Image loaded");
            sourceImage = ImageIO.read(tmp);
            destinationImage = deepCopy(sourceImage); // Not redundant I swear
            originalImage = deepCopy(sourceImage); // Set a copy of the untouched image to the side
            imageHeight = sourceImage.getHeight() - 1;
            imageWidth = sourceImage.getWidth() - 1;
            iv.setImage(updateDisplay());
            System.out.println(sourceImage.getHeight() + " height of image | " + sourceImage.getWidth() + " width of image");
        }
    }

    public static void resetImage(ImageView iv) {
        if (originalImage == null) {
            return;
        }
        destinationImage = deepCopy(originalImage); // Refer to destinationImage, then duplicate it into the destinationImage. updateImage() will handle the rest of the image syncronization
        iv.setImage(updateDisplay());
        System.out.println("Revert Successful!");
    }

    public static Image updateDisplay() {
        sourceImage = deepCopy(destinationImage); // destinationImage is the new base for manipulation: the source!
        return SwingFXUtils.toFXImage(sourceImage, null); // Takes buffered image and converts it back to an ImageView displayable image
    }

    // Creates a new identical instance of a BufferedImage (useful for reversion)
    // Code from: https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static void boxBlur(ImageView iv) {
        if (iv.getImage() == null) {
            return;
        }
        int[][] kernel;
        for (int height = 0; height <= imageHeight; height++) {
            for (int width = 0; width <= imageWidth; width++) {
                kernel = new int[][]{ // Contruct RGB array to manipulate. Edges are accounted for in construction
                    {(height == 0 || width == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width - 1, height - 1), (height == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width, height - 1), (height == 0 || width >= imageWidth) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width + 1, height - 1)},
                    {(width == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width - 1, height), sourceImage.getRGB(width, height), (width == imageWidth) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width + 1, height)},
                    {(height >= imageHeight || width == 0) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width - 1, height + 1), (height >= imageHeight) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width, height + 1), (height >= imageHeight || width >= imageWidth) ? sourceImage.getRGB(width, height) : sourceImage.getRGB(width + 1, height + 1)}
                };
                int redAvg = 0, blueAvg = 0, greenAvg = 0;
                for (int i[] : kernel) {
                    for (int j : i) {
                        redAvg += getRed(j); // Add all of the R G or B values up
                        greenAvg += getGreen(j);
                        blueAvg += getBlue(j);
                    }
                }
                redAvg /= 9; // 9 is size of kernel
                greenAvg /= 9; // Really just simple averaging
                blueAvg /= 9;
                destinationImage.setRGB(width, height, 65536 * redAvg + 256 * greenAvg + blueAvg); // setRGB() takes the integer value of an rgb color
            }
        }
        // Why does ImageView not display after blur of image bigger than 1000 x 1000 ish? I couldn't say. . . not online, and no answers to my question on StackOverflow
        iv.setImage(updateDisplay());
        System.out.println("Simple Blur complete!");
    }

    public void gaussianBlur(ImageView iv, TextField tf) {
        int blurIntensity = DEFAULT_BLUR_MAGNITUDE;
        try {
            blurIntensity = Integer.parseInt(tf.getText());
        } catch (NumberFormatException exception) {
            System.out.println("\"" + tf.getText() + "\" input was not an integer");
            tf.setText(Integer.toString(DEFAULT_BLUR_MAGNITUDE));
        }
        if (iv.getImage() == null) {
            return;
        }
        if (blurIntensity <= 0) {
            blurIntensity = DEFAULT_BLUR_MAGNITUDE;
            tf.setText(Integer.toString(DEFAULT_BLUR_MAGNITUDE));
            System.out.println("Blur will not work with values of 0 or less");
        }
        // Do a pass only along the vertical axis first. The full Gaussian Blur can be achieved through two passes
        //System.out.println(getRed(destinationImage.getRGB(100, 100)) + ", " + getGreen(destinationImage.getRGB(100, 100)) + ", " + getBlue(destinationImage.getRGB(100, 100)));
        for (int y = 0; y <= imageHeight; y++) {
            for (int x = 0; x <= imageWidth; x++) {
                destinationImage.setRGB(x, y, gaussianFunctionHorizontal(blurIntensity, x, y));
            }
        }
        //System.out.println(getRed(destinationImage.getRGB(100, 100)) + ", " + getGreen(destinationImage.getRGB(100, 100)) + ", " + getBlue(destinationImage.getRGB(100, 100)));
        // Vertical blur pass
        sourceImage = destinationImage;
        for (int y = 0; y <= imageHeight; y++) {
            for (int x = 0; x <= imageWidth; x++) {
                destinationImage.setRGB(x, y, gaussianFunctionVertical(blurIntensity, x, y));
            }
        }
        iv.setImage(updateDisplay());
        //System.out.println(getRed(destinationImage.getRGB(100, 100)) + ", " + getGreen(destinationImage.getRGB(100, 100)) + ", " + getBlue(destinationImage.getRGB(100, 100)));
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

    // Gaussian Wikipedia: https://en.wikipedia.org/wiki/Gaussian_blur
    // Performs blurs over all pixels horizontally and then all pixels vertically in O(2n)
    public int gaussianFunctionHorizontal(int blurIntensity, int x, int y) {
        double redAvg = 0, blueAvg = 0, greenAvg = 0;
        int loops = 0;
        while (loops <= blurIntensity * 3) { // The algorithm takes the rgb values of pixels loop tiles away on the x axis and applies the Gaussian function to determine the ratio to which the rgb value to the blurred pixel
            // Math checks out when function corroborated on Desmos
            // GAUSSIAN ALGORITHM
            double blurContributionRatio = (double) ((1 / Math.sqrt(Math.PI * 2 * Math.pow(blurIntensity, 2))) * Math.pow(Math.E, (-1 * Math.pow(loops, 2) / (2 * Math.pow(blurIntensity, 2)))));
            redAvg += getRed(sourceImage.getRGB((x + loops > imageWidth) ? x : x + loops, y)) * blurContributionRatio;
            greenAvg += getGreen(sourceImage.getRGB((x + loops > imageWidth) ? x : x + loops, y)) * blurContributionRatio;
            blueAvg += getBlue(sourceImage.getRGB((x + loops > imageWidth) ? x : x + loops, y)) * blurContributionRatio;
            loops++;
        }
        loops = -1; // Pixels behind the current target have to be checked as well 
        while (loops >= -1 * blurIntensity * 3) { // As a general rule, blurIntensity * 3 is the point at which the rgb ratio additions become negligable
            double blurContributionRatio = (double) ((1 / Math.sqrt(Math.PI * 2 * Math.pow(blurIntensity, 2))) * Math.pow(Math.E, (-1 * Math.pow(loops, 2) / (2 * Math.pow(blurIntensity, 2)))));
            redAvg += getRed(sourceImage.getRGB((x + loops < 0) ? x : x + loops, y)) * blurContributionRatio;
            greenAvg += getGreen(sourceImage.getRGB((x + loops < 0) ? x : x + loops, y)) * blurContributionRatio;
            blueAvg += getBlue(sourceImage.getRGB((x + loops < 0) ? x : x + loops, y)) * blurContributionRatio;
            loops--;
        }
        // Return the ratio at which to contribute the pixel
        return 65536 * (int) (redAvg) + 256 * (int) (greenAvg) + (int) (blueAvg);
    }

    // Everything from above applies to below, but with the targets of the blur being along the y axis
    public int gaussianFunctionVertical(int blurIntensity, int x, int y) {
        double redAvg = 0, blueAvg = 0, greenAvg = 0;
        int loops = 0;
        while (loops <= blurIntensity * 3) {
            double blurContributionRatio = (double) ((1 / Math.sqrt(Math.PI * 2 * Math.pow(blurIntensity, 2))) * Math.pow(Math.E, (-1 * Math.pow(loops, 2) / (2 * Math.pow(blurIntensity, 2)))));
            redAvg += getRed(sourceImage.getRGB(x, (y + loops > imageHeight) ? y : y + loops)) * blurContributionRatio;
            greenAvg += getGreen(sourceImage.getRGB(x, (y + loops > imageHeight) ? y : y + loops)) * blurContributionRatio;
            blueAvg += getBlue(sourceImage.getRGB(x, (y + loops > imageHeight) ? y : y + loops)) * blurContributionRatio;
            loops++;
        }
        loops = -1;
        while (loops >= -1 * blurIntensity * 3) {
            double blurContributionRatio = (double) ((1 / Math.sqrt(Math.PI * 2 * Math.pow(blurIntensity, 2))) * Math.pow(Math.E, (-1 * Math.pow(loops, 2) / (2 * Math.pow(blurIntensity, 2)))));
            redAvg += getRed(sourceImage.getRGB(x, (y + loops < 0) ? y : y + loops)) * blurContributionRatio;
            greenAvg += getGreen(sourceImage.getRGB(x, (y + loops < 0) ? y : y + loops)) * blurContributionRatio;
            blueAvg += getBlue(sourceImage.getRGB(x, (y + loops < 0) ? y : y + loops)) * blurContributionRatio;
            loops--;
        }
        // Return the ratio at which to contribute the pixel
        return 65536 * (int) (redAvg) + 256 * (int) (greenAvg) + (int) (blueAvg);
    }

    // Sobel Operation Wikipedia: https://en.wikipedia.org/wiki/Sobel_operator
    // StackOverflow that guided my code strucutre: https://stackoverflow.com/questions/41468661/sobel-edge-detecting-program-in-java
    public void findEdges(ImageView iv) {
        if (iv.getImage() == null) {
            return;
        }
        final int SOBEL_OP_LOW = 1, SOBEL_OP_HIGH = 2;
        int[][] storedEdgeGrays = new int[imageWidth + 1][imageHeight + 1];
        int[][] kernel;
        int maxContrast = 0; 
        for (int y = 0; y <= imageHeight; y++) {
            for (int x = 0; x <= imageWidth; x++) {
                // Sobel Operator
                // Gradients used: [-1,0,1]   [-1,-2,-1]
                //                 [-2,0,2] & [0, 0, 0]
                //                 [-1,0,1]   [1, 2, 1]
                // Other possiblities: {1,2,1} & {3,10,3} & {47,162,47}
                kernel = new int[][]{ // Contruct RGB array to manipulate. Edges are accounted for in construction
                    {(x == 0 || y == 0) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x - 1, y - 1), (y == 0) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x, y - 1), (y == 0 || x >= imageWidth) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x + 1, y - 1)},
                    {(x == 0) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x - 1, y), 0, (x == imageWidth) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x + 1, y)},
                    {(y >= imageHeight || x == 0) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x - 1, y + 1), (y >= imageHeight) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x, y + 1), (y >= imageHeight || x >= imageWidth) ? sourceImage.getRGB(x, y) : sourceImage.getRGB(x + 1, y + 1)}
                };
                int gradient1 = (getGrayscaleInt(kernel[0][0]) * -1 * SOBEL_OP_LOW) + (getGrayscaleInt(kernel[2][0]) * SOBEL_OP_LOW)
                        + (getGrayscaleInt(kernel[0][1]) * -1 * SOBEL_OP_HIGH) + (getGrayscaleInt(kernel[2][1]) * SOBEL_OP_HIGH)
                        + (getGrayscaleInt(kernel[0][2]) * -1 * SOBEL_OP_LOW) + (getGrayscaleInt(kernel[2][2]) * SOBEL_OP_LOW);
                int gradient2 = (getGrayscaleInt(kernel[0][0]) * -1 * SOBEL_OP_LOW) + (getGrayscaleInt(kernel[1][0]) * -1 * SOBEL_OP_HIGH) + (getGrayscaleInt(kernel[2][0]) * -1 * SOBEL_OP_LOW)
                        + (getGrayscaleInt(kernel[0][2]) * SOBEL_OP_LOW) + (getGrayscaleInt(kernel[1][2]) * SOBEL_OP_HIGH) + (getGrayscaleInt(kernel[2][2]) * SOBEL_OP_LOW);
                int edgeContrastValue = (int) Math.sqrt(Math.pow(gradient1, 2) + Math.pow(gradient2, 2)); // The algorithm that defines this operation: root(GradientKernelA^2 + GradientKernelB^2)
                if (edgeContrastValue > maxContrast) { // Keeping track of the highest contrast is necessary to find the range of gradients in the photo
                    maxContrast = edgeContrastValue;
                }
                storedEdgeGrays[x][y] = edgeContrastValue; // Save whatever grayscale int we get before setting to destination because we need knowledge of the gradient range (see above)
            }
        }
        
        double contrastScaling = 255.0 / maxContrast; // Scaling of grayscale to match distribution of values across the image
        
        for (int y = 0; y <= imageHeight; y++) {
            for (int x = 0; x <= imageWidth; x++) {
                int edge = storedEdgeGrays[x][y];
//                int red = getRed(edge), green = getGreen(edge), blue = getBlue(edge);
//                red = (int)(red * contrastScaling);
//                green = (int)(green * contrastScaling);
//                blue = (int)(blue * contrastScaling);
                edge = (int)(edge * contrastScaling); // Apply gradient ratio
                edge = 0xff000000 | (edge << 16) | (edge << 8) | edge; // Gonna be honest, I don't know how this words even after research. But with the rgb manipulation above, the entire image was on a blue gradient
                destinationImage.setRGB(x, y, edge);
            }
        }
        iv.setImage(updateDisplay());
        System.out.println("Edge Detection Complete!");
    }

    public int getGrayscaleInt(int rgbInt) {
        int red = getRed(rgbInt), green = getGreen(rgbInt), blue = getBlue(rgbInt);
        // Grayscale RGB coefficients are 0.2126 for red, 0.7152 for green, and 0.0722 for blue
        return (int) (red * 0.2126 + green * 0.7152 + blue * 0.0722);
    }
}
