package com.ebookstore.controller;

import com.ebookstore.common.BusinessException;
import com.ebookstore.common.Result;
import com.ebookstore.dto.BookDetailDTO;
import com.ebookstore.dto.BookListDTO;
import com.ebookstore.entity.Category;
import com.ebookstore.mapper.CategoryMapper;
import com.ebookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryMapper categoryMapper;

    // 获取一级目录列表
    @GetMapping("/categories/level1")
    public Result<List<Category>> getLevelOneCategories() {
        return Result.ok(categoryMapper.findLevel1());
    }

    // 获取某一级目录下的二级目录列表
    @GetMapping("/categories/level2")
    public Result<List<Category>> getLevelTwoCategories(@RequestParam Integer parentId) {
        return Result.ok(categoryMapper.findByParentId(parentId));
    }

    // 获取某一级目录下所有图书
    @GetMapping("/list-by-parent")
    public Result<List<BookListDTO>> getBooksByParentCategory(@RequestParam Integer parentId) {
        return Result.ok(bookService.getBooksByParentCategory(parentId));
    }

    // 获取图书详情（三层信息）
    @GetMapping("/detail/{id}")
    public Result<BookDetailDTO> getBookDetail(@PathVariable Long id) {
        BookDetailDTO book = bookService.getBookDetail(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        return Result.ok(book);
    }

    // 获取图书列表（支持分类 + 关键词搜索）
    @GetMapping("/list")
    public Result<List<BookListDTO>> getBookList(
            @RequestParam(required = false, defaultValue = "0") Integer categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.ok(bookService.getBooksByCategoryAndKeyword(categoryId, keyword));
    }
}
