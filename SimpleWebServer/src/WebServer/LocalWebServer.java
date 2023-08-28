package WebServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalWebServer 
{
    public static void main(String[] args)
    {
        int port = 1234; // Port number to run the server on
        String rootFolder = "C:\\Hari\\Webtech\\SalesOrder"; // Local Folder Path

        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            while (true) 
            {
                Socket clientSocket = serverSocket.accept();

                // Handle the client request in a separate thread
                Thread thread = new Thread(() -> handleClientRequest(clientSocket, rootFolder));
                thread.start();
            }
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket, String rootFolder)
    {
        try 
        {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            // Read the request
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            StringBuilder requestBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null && !line.isEmpty()) 
            {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString();
            System.out.println("Received request:\n" + request);

            // Extract the requested file path from the request
            String[] requestLines = request.split("\r\n");
            String[] requestTokens = requestLines[0].split(" ");
            String filePath = requestTokens[1];
            if (filePath.equals("/"))
            {
                filePath = "/index.html"; // Default file if no specific file is requested
            }

            // Construct the absolute path to the file
            Path absolutePath = Path.of(rootFolder + filePath);

            // Check if the file exists and is readable
            if (Files.exists(absolutePath) && Files.isRegularFile(absolutePath)) 
            {
                // Read the file content and construct the HTTP response
                byte[] fileContent = Files.readAllBytes(absolutePath);
                String contentType = Files.probeContentType(absolutePath);
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + fileContent.length + "\r\n" +
                        "\r\n";

                // Write the response header and file content to the output stream
                output.write(response.getBytes("UTF-8"));
                output.write(fileContent);
            }
            else 
            {
                // File not found, return a 404 response
                String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                output.write(notFoundResponse.getBytes("UTF-8"));
            }

            // Close the connection
            output.close();
            input.close();
            clientSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
