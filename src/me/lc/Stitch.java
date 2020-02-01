package me.lc;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Stitch {
    public native boolean stitchPairImg(String imgF1, String imgF2, String resultF);

    static {
        final String rootPath = System.getProperty("user.dir");
        System.load(Paths.get(rootPath, "src", "me", "lc", "image_dll.dll").toString());
    }

    public static void main(String[] args) {
        Stitch stitch = new Stitch();
        Path resRootPath = Paths.get(System.getProperty("user.dir"), "res");
        stitch.stitchPairImg(Paths.get(resRootPath.toString(), "1.png").toString(),
                Paths.get(resRootPath.toString(), "2.png").toString(),
                Paths.get(resRootPath.toString(), "result.png").toString());
    }
}
