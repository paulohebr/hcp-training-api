package br.gov.prodesp.hcpdemo;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class DemoHelper {
    public static final HashMap<RenderingHints.Key, Object> RenderingProperties = new HashMap<>();

    static {
        RenderingProperties.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        RenderingProperties.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        RenderingProperties.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public static BufferedImage textToImage(String Text, Font f, float Size) {
        //Derives font to new specified size, can be removed if not necessary.
        f = f.deriveFont(Size);

        FontRenderContext frc = new FontRenderContext(null, true, true);

        //Calculate size of buffered image.
        LineMetrics lm = f.getLineMetrics(Text, frc);

        Rectangle2D r2d = f.getStringBounds(Text, frc);

        BufferedImage img = new BufferedImage((int) Math.ceil(r2d.getWidth()), (int) Math.ceil(r2d.getHeight()), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHints(RenderingProperties);

        g2d.setBackground(Color.WHITE);
        g2d.setColor(Color.BLACK);

        g2d.clearRect(0, 0, img.getWidth(), img.getHeight());

        g2d.setFont(f);

        g2d.drawString(Text, 0, lm.getAscent());

        g2d.dispose();

        return img;
    }

    public static String[] NAMES = new String[]{
            "Adell",
            "Odelia",
            "Suanne",
            "Hang",
            "Rhett",
            "Ema",
            "Frieda",
            "Melvina",
            "Salvatore",
            "Monty",
            "Roxy",
            "Nam",
            "Suk",
            "Rosalee",
            "Eve",
            "Dominick",
            "Roselle",
            "Marline",
            "Bernie",
            "Modesto",
            "Delpha",
            "Divina",
            "Cris",
            "Rutha",
            "Karma",
            "Laurene",
            "Katelin",
            "Arthur",
            "Virgil",
            "Tomasa",
            "Scott",
            "Katelynn",
            "Pasty",
            "Cecilia",
            "Nu",
            "Takisha",
            "Alfredia",
            "Coral",
            "Garrett",
            "Thi",
            "Erica",
            "Risa",
            "Juanita",
            "Hubert",
            "Lidia",
            "Santos",
            "Robyn",
            "Daisey",
            "Phebe",
            "Janiece"
    };
}
