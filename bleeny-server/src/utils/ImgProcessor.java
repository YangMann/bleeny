package utils;

import exceptions.FaceNotFoundException;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

/**
 * Created by JeffreyZhang on 2014/6/24.
 */
public class ImgProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final String CASCADE_PATH = "E:\\GitHub\\drone-slam\\drone-slam\\src\\drone_slam\\apps\\humanchase\\";
    private String face_cascade_name = "haarcascade_frontalface_alt.xml";
    private CascadeClassifier face_cascade = new CascadeClassifier(CASCADE_PATH + face_cascade_name);

    public Mat rgb2gray(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat frame = new Mat(h, w, CvType.CV_8UC3);
        frame.put(0, 0, pixels);
        Mat frame_gray = new Mat();
        Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(frame_gray, frame_gray);

        return frame_gray;
    }

    public Point[][] detectFace(Mat frame_gray) throws FaceNotFoundException {
        MatOfRect faces = new MatOfRect();
        face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0, new Size(30, 30), new Size());

        Rect[] facesArray = faces.toArray();
        if (facesArray.length <= 0) {
            throw new FaceNotFoundException("No face found");
        }
        Point[][] rects = new Point[facesArray.length][2];
        for (int i = 0; i < facesArray.length; i++) {
            rects[i][0] = facesArray[i].tl();
            rects[i][1] = facesArray[i].br();
        }
        return rects;
    }

    public BufferedImage drawRect(BufferedImage img, Point[][] detection) {
        Graphics2D graph = img.createGraphics();
        for (Point[] face : detection) {
            graph.setColor(Color.GREEN);
            graph.drawRect((int) face[0].x, (int) face[0].y, (int) (face[1].x - face[0].x), (int) (face[1].y - face[0].y));
        }
        ImageIcon ii = new ImageIcon(img);
        JOptionPane.showMessageDialog(null, ii);
        return img;
    }

    // TEST
    public static void main(String[] args) {
        ImgProcessor imgProcessor = new ImgProcessor();
        try {
            File imgFile = new File("C:\\Users\\JeffreyZhang\\Desktop\\dc.jpg");
            BufferedImage imgBuffer = ImageIO.read(imgFile);
            Mat frame_gray = imgProcessor.rgb2gray(imgBuffer);
            imgProcessor.drawRect(imgBuffer, imgProcessor.detectFace(frame_gray));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
