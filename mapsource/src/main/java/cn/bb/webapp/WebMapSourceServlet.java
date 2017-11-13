package cn.bb.webapp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(urlPatterns = "/v4/mapbox.mapbox-terrain-v2,mapbox.mapbox-streets-v7/*", name = "WebMapSourceServlet")
public class WebMapSourceServlet extends HttpServlet {
    @Value("${source.storagepath}")
    private String storagePath;
    @Value("${source.url}")
    private String url;
    private static Logger logger = LogManager.getLogger(WebMapSourceServlet.class);
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        response.setContentType("application/octet-stream");
        String pathInfo = request.getPathInfo();
        Path path = Paths.get(storagePath, pathInfo);
        String content = null;
        ServletOutputStream outputStream = response.getOutputStream();
        if(Files.exists(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                is.transferTo(outputStream);
                logger.trace("get a file:" + pathInfo);
            }
        }
        else {
            Files.createDirectories(path.getParent());
            cn.bb.webapp.HttpRequestor httpRequestor = new cn.bb.webapp.HttpRequestor();
            try {
                content = httpRequestor.doGet(String.format(url, pathInfo));
                writeTxtFile(content, path.toString());
                logger.trace("download a file:" + pathInfo);
            } catch (Exception e) {
                logger.error("No such file.");
            }
            Files.newInputStream(path).transferTo(outputStream);
            logger.trace("get a file:" + pathInfo);
        }
    }

    public synchronized static void writeTxtFile(String content,String fileName)throws IOException {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                logger.trace("It's existed:" + fileName);
            } else {
                FileWriter fileWritter = new FileWriter(fileName, true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                file.createNewFile();
                bufferWritter.write(content);
                bufferWritter.close();
                logger.trace("write a file:" + fileName);
            }
        } catch (Exception e) {
            logger.error("No such file.");
        }
    }

}
