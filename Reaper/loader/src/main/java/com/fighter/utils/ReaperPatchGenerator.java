package com.fighter.utils;

import com.fighter.patch.ReaperPatchCryptTool;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.security.Security;

/**
 * Created by haitengwang on 17/05/2017.
 */

public class ReaperPatchGenerator {

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("\tjava -jar ./bin/generator.jar reaper.dex reaper.rr");
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            printUsage();
            return;
        }

        System.out.println("generator reaper patch start ...");

        File patchFile = new File(args[1]);
        if (patchFile.exists()) {
            patchFile.delete();
        }

        File rawFile = new File(args[0]);
        if (!rawFile.exists()) {
            printUsage();
            return;
        }

        Security.addProvider(new BouncyCastleProvider());

        try {
            ReaperPatchCryptTool.encryptTo(rawFile, args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("success");
    }

}
