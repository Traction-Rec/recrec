package com.tractionrec.recrec;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class LogoLoadingTest {

    @Test
    public void testLogoFileExists() {
        InputStream logoStream = getClass().getResourceAsStream("/logo.png");
        assertNotNull(logoStream, "Logo file should exist in resources");

        try {
            logoStream.close();
        } catch (IOException e) {
            // Ignore close errors in test
        }
    }

    @Test
    public void testLogoCanBeLoaded() throws IOException {
        InputStream logoStream = getClass().getResourceAsStream("/logo.png");
        assertNotNull(logoStream, "Logo file should exist in resources");

        BufferedImage logoImage = ImageIO.read(logoStream);
        assertNotNull(logoImage, "Logo should be readable as an image");

        assertTrue(logoImage.getWidth() > 0, "Logo should have positive width");
        assertTrue(logoImage.getHeight() > 0, "Logo should have positive height");

        logoStream.close();
    }
}
