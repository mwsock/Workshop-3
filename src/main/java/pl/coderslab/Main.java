package pl.coderslab;

import com.github.slugify.Slugify;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    static final Slugify SLG = Slugify.builder().build();
    static final String BASE_URL = "https://www.infoworld.com";
    static final String FILE_EXTENSION = ".txt";
    static Document doc = null;
    public static void main(String[] args) {
        try {
            doc = Jsoup.connect(BASE_URL + "/category/java/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(() -> getSubPageUrl(doc));
        executorService.shutdown();

    }

    private static void getSubPageUrl(Document doc) {


        List<Element> elementList = doc.select("div.article h3 a");
        elementList.forEach(element -> {

            String subPageUrl = BASE_URL +  element.attr("href");
            String fileName = getFileName(element);

            System.out.println(element.text() + " " + fileName);

            try {

                getArticle(subPageUrl).forEach(article -> {
                    saveArticle(fileName, article);
                });
                Thread.sleep(1000);

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        });

    }

    private static void saveArticle(String fileName, Element article) {
        article.text().lines().forEach(line -> {
            try {
                String textLine = line + "\n";
                checkFile(fileName);
                Files.write(Path.of(fileName + FILE_EXTENSION), textLine.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void checkFile(String fileName) throws IOException {
        Path path = Path.of(fileName + FILE_EXTENSION);
        if (!Files.exists(path)) Files.createFile(path);
    }

    private static String getFileName(Element element) {
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + "-" + SLG.slugify(element.text());
        return fileName;
    }

    private static List<Element> getArticle(String subPageUrl) throws IOException {
        doc = Jsoup.connect(subPageUrl).get();
        List<Element> elementList = doc.select("div[itemprop=articleBody] p");

        return elementList;

    }
}