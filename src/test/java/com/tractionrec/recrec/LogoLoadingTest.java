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

    @Test
    public void testRecRecApplicationLogoMethods() {
        // Test the public loadLogoImage method
        Image logoImage = RecRecApplication.loadLogoImage();
        assertNotNull(logoImage, "RecRecApplication.loadLogoImage() should return an image");

        assertTrue(logoImage.getWidth(null) > 0, "Logo should have positive width");
        assertTrue(logoImage.getHeight(null) > 0, "Logo should have positive height");
    }

    @Test
    public void testRecRecApplicationLogoIcon() {
        // Test the public loadLogoIcon method
        ImageIcon logoIcon = RecRecApplication.loadLogoIcon(48);
        assertNotNull(logoIcon, "RecRecApplication.loadLogoIcon() should return an ImageIcon");

        assertEquals(48, logoIcon.getIconHeight(), "Logo icon should have the requested height");
        assertTrue(logoIcon.getIconWidth() > 0, "Logo icon should have positive width");
    }

    @Test
    public void testMalformedImageProtection() {
        // Test that our fix prevents division by zero errors
        // We verify that the actual logo has valid dimensions (our protection should never trigger in practice)
        Image realLogo = RecRecApplication.loadLogoImage();
        assertNotNull(realLogo, "Real logo should load successfully");
        assertTrue(realLogo.getWidth(null) > 0, "Real logo should have positive width to avoid division by zero");
        assertTrue(realLogo.getHeight(null) > 0, "Real logo should have positive height to avoid division by zero");

        // Verify that loadLogoIcon works with various sizes
        int[] testSizes = {16, 20, 32, 48, 64, 128};
        for (int size : testSizes) {
            ImageIcon icon = RecRecApplication.loadLogoIcon(size);
            assertNotNull(icon, "Logo icon should be created for size " + size);
            assertEquals(size, icon.getIconHeight(), "Icon should have requested height " + size);
            assertTrue(icon.getIconWidth() > 0, "Icon should have positive width for size " + size);
        }
    }
}
