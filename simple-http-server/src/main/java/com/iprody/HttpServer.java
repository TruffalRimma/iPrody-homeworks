package com.iprody;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private static final String STATIC = "simple-http-server/static";
    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put("html", "text/html");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("txt", "text/plain");
    }

    public static void main(String[] args) throws IOException {
    /*
    ServerSocket:
    - Представляет серверный сетевой сокет.
    - Ожидает входящие подключения на указанном порту (8080) по протоколу TCP/IP.
     */
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server started at http://localhost:8080");

        while (true) {
            // Метод accept() блокирует выполнение до тех пор, пока не подключится клиент, и возвращает объект Socket.
            Socket clientSocket = serverSocket.accept();
      /*
      Socket:
      - Представляет соединение с одним конкретным клиентом по двоичному протоколу TCP/IP
      - Предоставляет два потока с побитовым чтением:
        getInputStream() — для чтения данных, приходящих от клиента (запрос).
        getOutputStream() — для отправки данных клиенту (ответ).
       */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            // Чтение запроса
            String line;
            String fileName = getUrlFileName(in.readLine());
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            Path staticDir = Paths.get(STATIC).toAbsolutePath().normalize();
            Path filePath = staticDir.resolve(fileName).normalize();

            // Проверка безопасности - проверяем, чтобы путь не позволял выйти за пределы директории static/.
            // + проверка существования файла
            if (!filePath.startsWith(staticDir) || !Files.exists(filePath)) {
                sendErrorResponse(out);
            } else {
                sendResponse(out, filePath, fileName);
            }

            out.flush();
            clientSocket.close();
        }
    }

    private static String getUrlFileName(String firstLine) {
        if (firstLine == null || firstLine.isEmpty()) {
            return firstLine;
        }

        // Выводим в консоль первую строчку, чтобы не нарушать старую логику
        System.out.println(firstLine);
        String[] segments = firstLine.split(" ");
        // Избавляемся от / в имени файла
        return segments[1].substring(1);
    }

    private static void sendResponse(OutputStream out, Path filePath, String fileName) throws IOException {
        String extension = fileName.split("\\.") [1];
        String contentType = CONTENT_TYPES.get(extension);

        String headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + Files.size(filePath) + "\r\n" +
                "\r\n";
        out.write(headers.getBytes());

        try (BufferedInputStream fileIn = new BufferedInputStream(Files.newInputStream(filePath))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void sendErrorResponse(OutputStream out) throws IOException {
        String response = "<h1>404 Not Found</h1>";
        byte[] body = response.getBytes();

        String headers = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "\r\n";

        out.write(headers.getBytes());
        out.write(body);
    }
}
