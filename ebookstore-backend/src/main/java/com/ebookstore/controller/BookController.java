package com.ebookstore.controller;

import com.ebookstore.dto.BookDetailDTO;
import com.ebookstore.dto.BookListDTO;
import com.ebookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {

    @Autowired
    private BookService bookService;

    // 获取一级目录列表
    @GetMapping("/categories/level1")
    public ResponseEntity<?> getLevelOneCategories() {
        // 暂时硬编码，后面会改为数据库查询
        List<Map<String, Object>> categories = List.of(
                Map.of("id", 1, "name", "英语"),
                Map.of("id", 2, "name", "计算机")
        );
        return ResponseEntity.ok(categories);
    }

    // 获取某一级目录下的二级目录列表
    @GetMapping("/categories/level2")
    public ResponseEntity<?> getLevelTwoCategories(@RequestParam Integer parentId) {
        // 暂时硬编码，后面会改为数据库查询
        Map<Integer, List<Map<String, Object>>> subCategories = new HashMap<>();
        subCategories.put(1, List.of(
                Map.of("id", 11, "name", "英语词汇"),
                Map.of("id", 12, "name", "英语语法"),
                Map.of("id", 13, "name", "英语阅读")
        ));
        subCategories.put(2, List.of(
                Map.of("id", 21, "name", "编程语言"),
                Map.of("id", 22, "name", "数据库"),
                Map.of("id", 23, "name", "操作系统")
        ));
        return ResponseEntity.ok(subCategories.getOrDefault(parentId, List.of()));
    }

//    // 获取二级目录下的图书列表（二级目录展示）
//    @GetMapping("/list")
//    public ResponseEntity<List<BookListDTO>> getBookList(@RequestParam Integer categoryId) {
//        List<BookListDTO> books = bookService.getBooksByCategory(categoryId);
//        return ResponseEntity.ok(books);
//    }

    // 获取某一级目录下所有图书
    @GetMapping("/list-by-parent")
    public ResponseEntity<List<BookListDTO>> getBooksByParentCategory(@RequestParam Integer parentId) {
        List<BookListDTO> books = bookService.getBooksByParentCategory(parentId);
        return ResponseEntity.ok(books);
    }

    // 获取图书详情（三层信息）
    @GetMapping("/detail/{id}")
    public ResponseEntity<BookDetailDTO> getBookDetail(@PathVariable Long id) {
        BookDetailDTO book = bookService.getBookDetail(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    // 获取图书列表（支持分类 + 关键词搜索）
    @GetMapping("/list")
    public ResponseEntity<List<BookListDTO>> getBookList(
            @RequestParam Integer categoryId,
            @RequestParam(required = false) String keyword  // 👈 加这行
    ) {
        List<BookListDTO> books = bookService.getBooksByCategoryAndKeyword(categoryId, keyword);
        return ResponseEntity.ok(books);
    }
}