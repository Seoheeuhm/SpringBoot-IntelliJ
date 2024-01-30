package com.example.firstproject.service;

import com.example.firstproject.dto.ArticleForm;
import com.example.firstproject.entity.Article;
import com.example.firstproject.repository.ArticleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service  //서비스 객체 생성
public class ArticleService {
    @Autowired
    private ArticleRepository articleRepository;  //게시글 리파지터리 객체 주입

    public List<Article> index() {
        return articleRepository.findAll();
    }

    public Article show(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Article create(ArticleForm dto) {
        Article article = dto.toEntity();
        if (article.getId() != null) {
            return null;
        }
        return articleRepository.save(article);
    }

    public Article update(Long id, ArticleForm dto) {
        //1. DTO -> 엔티티 변환
        Article article = dto.toEntity();
        log.info("id: {}, article: {}", id, article.toString());
        //2. 타깃 조회
        Article target = articleRepository.findById(id).orElse(null);
        //3. 잘못된 요청 처리
        if (target == null || id != article.getId()) {
            //400, 잘못된 요청 응답
            log.info("잘못된 요청! id: {}, article: {}", id, article.toString());
            return null;
        }
        //4. 업데이트 및 정상 응답(200)
        target.patch(article);
        Article updated = articleRepository.save(target);
        return updated;
    }

    public Article delete(Long id) {
        //1. 대상 찾기
        Article target = articleRepository.findById(id).orElse(null);
        //2. 잘못된 요청 처리
        if (target == null) {
            return null;
        }
        //3. 대상 삭제
        articleRepository.delete(target);
        return target;
    }

    @Transactional
    public List<Article> createArticles(List<ArticleForm> dtos) {
        //1.dto 묶음을 엔티티 묶음으로 반환
        List<Article> articleList = dtos.stream() //dtos를 스트림화, 최종 결과를 articleList에 저장
                .map(dto -> dto.toEntity())  //map()으로 dto가 하나하나 올 때마다 dto.toEntity()를 수행해 매핑
                .collect(Collectors.toList());  //이렇게 매칭한 것을 리스트로 묶기
        //2. 엔티티 묶음을 DB에 저장
        articleList.stream()  //articleList를 스트림화
                .forEach(article -> articleRepository.save(article));  //article이 하나씩 올 때마다 articleRepository를 통해 DB에 저장
        //3. 강제 예외 발생
        articleRepository.findById(-1L)  //id가 -1인 데이터 찾기
                .orElseThrow(() -> new IllegalArgumentException("결제 실패!")); //찾는 데이터 없으면 예외 발생
        //4. 결과 값 반환
        return articleList;  //articleList를 형식상 반환
    }
}
