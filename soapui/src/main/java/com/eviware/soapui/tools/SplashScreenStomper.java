/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.tools;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SplashScreenStomper {
    public static void main(String arg[]) throws IOException {
        String version = arg[0];
        String inputFilePath = arg[1];
        String outputFilePath = arg[2];

        String fullText = String.format("%s\n\nCopyright SmartBear Software\n\nwww.soapui.org\nwww.smartbear.com", version);
        BufferedImage bufferedImage = ImageIO.read(new File(inputFilePath));
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setColor(new Color(0x66, 0x66, 0x66));
        Font font = new Font("Arial", Font.PLAIN, 12);
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int y = 181;
        for (String text : fullText.split("\n")) {
            graphics.drawString(text, 42, y);
            y += 15;
        }

        ImageIO.write(bufferedImage, "png", new File(outputFilePath));

    }
}
