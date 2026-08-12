package com.ebookstore.service;

import com.ebookstore.common.PageResult;
import com.ebookstore.dto.BookDetailDTO;
import com.ebookstore.dto.BookListDTO;
import java.util.List;

public interface BookService {
    List<BookListDTO> getBooksByCategory(Integer categoryId);
    List<BookListDTO> getBooksByParentCategory(Integer parentId);
    BookDetailDTO getBookDetail(Long bookId);
    List<BookListDTO> getBooksByCategoryAndKeyword(Integer categoryId, String keyword);
    PageResult<BookListDTO> getBooksPage(Integer categoryId, Integer parentId, String keyword, String sort, int page, int pageSize);
}