package com.example.backend.repository;

import com.example.backend.model.ChapterPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChapterPageRepository extends JpaRepository<ChapterPage, Long> {
    @Query("""
            select count(page)
            from ChapterPage page
            where page.chapter.chapterId = :chapterId
              and page.pageNumber = :pageNumber
              and (page.pageStatus is null or upper(page.pageStatus) <> 'DELETED')
            """)
    long countActiveByChapterIdAndPageNumber(
            @Param("chapterId") Long chapterId,
            @Param("pageNumber") Integer pageNumber);

    List<ChapterPage> findByChapterChapterIdAndPageStatusIgnoreCaseOrderByPageNumberAsc(
            Long chapterId, String pageStatus);

    @Query("""
            select page
            from ChapterPage page
            where page.chapter.chapterId = :chapterId
              and (page.pageStatus is null or upper(page.pageStatus) <> 'DELETED')
            order by page.pageNumber asc
            """)
    List<ChapterPage> findByChapterChapterIdOrderByPageNumberAsc(
            @Param("chapterId") Long chapterId);

    @Query("""
            select page
            from ChapterPage page
            where page.chapter.series.seriesId = :seriesId
              and (page.pageStatus is null or upper(page.pageStatus) <> 'DELETED')
            order by page.pageNumber asc
            """)
    List<ChapterPage> findByChapterSeriesSeriesIdOrderByPageNumberAsc(
            @Param("seriesId") Long seriesId);
}
