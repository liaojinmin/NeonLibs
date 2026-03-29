import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

/**
 * NeonLibs
 * PACKAGE_NAME
 *
 * @author 老廖
 * @since 2026/3/18 14:19
 */
public class ImageToYaml {

    public static void main(String[] args) throws Exception {

        File folder = new File("E:\\测试客户端\\缝隙行者\\.minecraft\\resourcepacks\\textures\\mobscard"); // 图片目录
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));

        if (files == null) return;

        // OCR
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // 你的路径
        tesseract.setLanguage("chi_sim");
        tesseract.setVariable("load_system_dawg", "false");
        tesseract.setVariable("load_freq_dawg", "false");


        FileWriter writer = new FileWriter("卡牌.yml");
        FileWriter writer2 = new FileWriter("卡牌物品.yml");
        for (File file : files) {
            String text = "null";
            /*
            BufferedImage original = ImageIO.read(file);

            int width = original.getWidth();    // 600
            int height = original.getHeight();  // 600
            int cropHeight = 100;

            // 从下往上截取，所以 y = height - cropHeight
            int x = 0;
            int y = height - cropHeight;

            BufferedImage cropped = original.getSubimage(x, y, width, cropHeight);

            // ===== OCR =====
            try {
                text = tesseract.doOCR(cropped).trim();
            } catch (Exception e) {
                text = "识别失败";
            }
            text = text.replaceAll("\\s+", "");



             */

            // ===== ID =====
            String id = file.getName().replace(".png", "");
            String level = id.substring(id.length() - 1);
            if (level.equalsIgnoreCase("1")) {
                level = "一级";
            } else if (level.equalsIgnoreCase("2")) {
                level = "二级";
            } else level = "三级";
            // ===== YAML =====
            String yaml = id + ":\n" +
                    "  type: 'icon'\n" +
                    "  path: 'textures/mobscard/" + id + ".png'\n" +
                    "  scale: 1\n" +
                    "  transformSetting:\n" +
                    "    ground:\n" +
                    "      scaleX: 0.5\n" +
                    "      scaleY: 0.5\n" +
                    "      scaleZ: 0.5\n" +
                    "    thirdPersonRightHand:\n" +
                    "      scaleX: 0.5\n" +
                    "      scaleY: 0.5\n" +
                    "      scaleZ: 0.5\n" +
                    "      offsetX: 0.0\n" +
                    "      offsetY: -5.0\n" +
                    "      offsetZ: 0.0\n" +
                    "  matchCondition:\n" +
                    "    type: '406'\n" +
                    "    match: \"" + id + "\"\n\n";

            String yaml2 = id + ":\n" +
                    "  uniqueId: " + id + "\n" +
                    "  data:\n" +
                    "    power: 50\n" +
                    "  display:\n" +
                    "    material: 'QUARTZ'\n" +
                    "    name: §B "+ level +"\n" +
                    "    lore:\n" +
                    "      - ''\n" +
                    "      - '§8-  §C不可交易'\n" +
                    "      - '§8-  §7[§power§7$data{power}]'\n" +
                    "\n";

            writer.write(yaml);

            writer2.write(yaml2);

            System.out.println("处理: " + file.getName() + " -> " + text);
        }
        writer.close();
        writer2.close();
        System.out.println("完成！");
    }


}
