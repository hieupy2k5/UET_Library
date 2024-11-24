package org.example.uet_library.apis;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.uet_library.models.Book;

public class BookAPI {

    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String ApiKey = "AIzaSyC8Wq4sinCA-uJQp-QP4hrLB06K--OwYP0";

    /**
     *
     * @param query
     * @param filter
     * @return new Task(return book from API) which we will call in BookAPI search
     */
    public static Task<ObservableList<Book>> searchBooks(String query, String filter) {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() {
                String searchQuery = buildSearchQuery(query, filter);
                String apiResponse = getHttpResponse(API_URL + searchQuery + "&maxResults=40&key=" + ApiKey);
                return getDocumentFromJson(apiResponse);
            }
        };
    }

    /**
     *
     * @param query
     * @param filter
     * @return the query with filter
     */
    private static String buildSearchQuery(String query, String filter) {
        String searchQuery = "";

        switch (filter.toLowerCase()) {
            case "isbn":
                searchQuery = "isbn:" + query;
                break;
            case "title":
                searchQuery = "intitle:" + query;
                break;
            case "author":
                searchQuery = "inauthor:" + query;
                break;
            default:
                searchQuery = query;
                break;
        }
        return encodeQuery(searchQuery);
    }

    /** Return json string of api call*/
    private static String getHttpResponse(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed with HTTP error code : " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * return the list book we search from API Google Book
     * @param json
     * @return book list we fetch from API Google Book (convert json to Book Object)
     */
    private static ObservableList<Book> getDocumentFromJson(String json) {
        ObservableList<Book> books = FXCollections.observableArrayList();
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            if (node.has("items")) {
                JsonNode items = node.get("items");

                for (JsonNode item : items) {
                    Book book = new Book();
                    JsonNode volumeInfo = item.get("volumeInfo");
                    if (volumeInfo != null) {
                        book.setTitle(volumeInfo.get("title").asText());

                        // Handle authors correctly (could be multiple authors)
                        if (volumeInfo.has("authors")) {
                            StringBuilder authors = new StringBuilder();
                            for (JsonNode authorNode : volumeInfo.get("authors")) {
                                if (authors.length() > 0) {
                                    authors.append(", ");
                                }
                                authors.append(authorNode.asText());
                            }
                            book.setAuthor(authors.toString());
                        } else {
                            book.setAuthor("");
                        }

                        // Handle ISBN (could be multiple identifiers)
                        if (volumeInfo.has("industryIdentifiers")) {
                            StringBuilder isbnList = new StringBuilder();
                            for (JsonNode identifierNode : volumeInfo.get("industryIdentifiers")) {
                                if (isbnList.length() > 0) {
                                    isbnList.append(", ");
                                }
                                isbnList.append(identifierNode.get("identifier").asText());
                            }
                            book.setIsbn(isbnList.toString());
                        } else {
                            book.setIsbn("");
                        }

                        // Handle description
                        if (volumeInfo.has("description")) {
                            book.setDescription(volumeInfo.get("description").asText());
                        } else {
                            book.setDescription("");
                        }

                        // Handle year
                        if (volumeInfo.has("publishedDate")) {
                            try {
                                book.setYear(Integer.parseInt(volumeInfo.get("publishedDate").asText().substring(0, 4)));
                            } catch (Exception e) {
                                book.setYear(2024);
                            }
                        } else {
                            book.setYear(2024);
                        }

                        // Handle image link (if available)
                        String imageLink = volumeInfo.has("imageLinks") && volumeInfo.get("imageLinks").has("thumbnail")
                                ? volumeInfo.get("imageLinks").get("thumbnail").asText() : "";
                        book.setImageLink(imageLink);
                        if (volumeInfo.has("infoLink")) {
                            book.setInfoBookLink(volumeInfo.get("infoLink").asText());
                        }
                        // Add the book to the list
                        if (volumeInfo.has("categories")) {
                            book.setType(volumeInfo.get("categories").get(0).asText());
                        } else {
                            book.setType("");
                        }
                        books.add(book);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }

    /** This code will fix error search with space and Vietnamese */
    private static String encodeQuery(String query) {
        try {
            return URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return query;
        }
    }

}
