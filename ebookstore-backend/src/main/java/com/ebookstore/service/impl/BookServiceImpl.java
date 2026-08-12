package com.ebookstore.service.impl;

import com.ebookstore.common.PageResult;
import com.ebookstore.dto.BookDetailDTO;
import com.ebookstore.dto.BookListDTO;
import com.ebookstore.mapper.BookMapper;
import com.ebookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    @Override
    public List<BookListDTO> getBooksByCategory(Integer categoryId) {
        return bookMapper.findBooksByCategoryId(categoryId);
    }

    @Override
    public List<BookListDTO> getBooksByParentCategory(Integer parentId) {
        return bookMapper.findBooksByParentCategoryId(parentId);
    }

    @Override
    public BookDetailDTO getBookDetail(Long bookId) {
        return bookMapper.findBookDetailById(bookId);
    }

    @Override
    public List<BookListDTO> getBooksByCategoryAndKeyword(Integer categoryId, String keyword) {
        return bookMapper.findBooksByCategoryIdAndKeyword(categoryId, keyword);
    }

    @Override
    public PageResult<BookListDTO> getBooksPage(Integer categoryId, Integer parentId, String keyword,
                                                String sort, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<BookListDTO> list = bookMapper.findBooksPage(categoryId, parentId, keyword, sort, offset, pageSize);
        long total = bookMapper.countBooks(categoryId, parentId, keyword);
        return PageResult.of(list, total, page, pageSize);
    }
}
