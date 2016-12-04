package com.jautumn.deposite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jautumn.downloader.DefaultDownloader;
import com.jautumn.downloader.DownloadResult;
import com.jautumn.downloader.Downloader;
import com.jautumn.parser.core.api.DownloadLinkParser;
import com.jautumn.parser.core.exceptions.BadDownloadServiceURLException;
import com.jautumn.parser.core.exceptions.ConnectionLimitException;
import com.jautumn.parser.impl.DepositFilesDownloadLinkParser;

public class DepositeApp {
    private static final String DEFAULT_SAVE_DIRECTORY_PATH = Paths.get(System.getProperty("user.home")).resolve("depositeDownloads").toString();

    public static void main(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        Options options = buildOptions();
        CommandLine cmd;
        try {
            cmd = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("DepositeApp", options);

            System.exit(1);
            return;
        }

        String url = cmd.getOptionValue("url");
        String path = cmd.getOptionValue("path");

        path = StringUtils.defaultIfBlank(path, DEFAULT_SAVE_DIRECTORY_PATH);
        try {
            DepositeApp app = new DepositeApp();
            DownloadResult downloadResult = app.download(url);
            app.save(downloadResult.getData(), path, downloadResult.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Options buildOptions() {
        Options options = new Options();

        Option url = new Option("u", "url", true, "deposite files download url");
        url.setRequired(true);
        options.addOption(url);

        Option savePath = new Option("p", "path", true, "path to save file");
        options.addOption(savePath);

        return options;
    }

    private DownloadResult download(String startUrl) throws InterruptedException, ConnectionLimitException, BadDownloadServiceURLException, IOException {
        DownloadLinkParser downloadLinkParser = new DepositFilesDownloadLinkParser(buildClient());
        String downloadURL = downloadLinkParser.getDownloadURL(startUrl);

        Downloader downloader = new DefaultDownloader();
        return downloader.download(downloadURL);
    }

    private void save(byte[] data, String saveDirPath, String fileName) throws IOException {
        Path savePath = Paths.get(saveDirPath);
        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        }
        savePath = savePath.resolve(fileName);
        StandardOpenOption option = Files.exists(savePath) ? StandardOpenOption.WRITE : StandardOpenOption.CREATE_NEW;
        Files.write(savePath, data, option);
    }

    private WebClient buildClient() {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        client.getCookieManager().setCookiesEnabled(false);
        return client;
    }
}
