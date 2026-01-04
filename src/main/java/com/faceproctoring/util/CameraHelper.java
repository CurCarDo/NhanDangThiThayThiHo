package com.faceproctoring.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

public class CameraHelper {

    private OpenCVFrameGrabber grabber;
    private volatile boolean running = false;

    private BufferedImage lastFrame;
    private final Object frameLock = new Object();

    private static void log(String msg) {
        String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.println(ts + " [Camera] " + msg);
    }

    /** Capture latest frame */
    public BufferedImage captureFrame() {
        synchronized (frameLock) {
            return lastFrame;
        }
    }

    /** Start camera stream (async) */
    public void startStream(Consumer<String> onFrame,
            ImageView imageView,
            Consumer<Exception> onError) {

        new Thread(() -> {
            Java2DFrameConverter converter = null;
            try {
                long t0 = System.currentTimeMillis();
                log("Opening camera (DSHOW)...");

                grabber = new OpenCVFrameGrabber(0);

                // ✅ FORCE DirectShow backend (compatible way)

                grabber.setImageWidth(320);
                grabber.setImageHeight(240);
                grabber.setFrameRate(15);

                grabber.start();

                log("Camera ready in " + (System.currentTimeMillis() - t0) + " ms");

                running = true;
                converter = new Java2DFrameConverter();

                while (running) {
                    Frame frame = grabber.grab();
                    if (frame == null)
                        continue;

                    BufferedImage img = converter.convert(frame);
                    if (img == null)
                        continue;

                    // Flip image horizontally (mirror)
                    BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                    Graphics2D g = flipped.createGraphics();
                    g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), 0, 0, img.getHeight(),
                            null);
                    g.dispose();

                    synchronized (frameLock) {
                        lastFrame = flipped;
                    }

                    Platform.runLater(() -> imageView.setImage(
                            SwingFXUtils.toFXImage(flipped, null)));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(flipped, "jpg", baos);
                    onFrame.accept(
                            Base64.getEncoder().encodeToString(baos.toByteArray()));
                }

            } catch (Exception e) {
                log("Camera error: " + e.getMessage());
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e));
                }
            } finally {
                try {
                    if (converter != null)
                        converter.close();
                } catch (Exception ignored) {
                }
            }
        }, "Camera-Thread").start();
    }

    /** Stop and release camera */
    public void stopCamera() {
        running = false;
        try {
            if (grabber != null) {
                log("Stopping camera...");
                grabber.stop();
                grabber.release();
                grabber = null;
                log("Camera released");
            }
        } catch (Exception e) {
            log("Error stopping camera: " + e.getMessage());
        }
    }
}
