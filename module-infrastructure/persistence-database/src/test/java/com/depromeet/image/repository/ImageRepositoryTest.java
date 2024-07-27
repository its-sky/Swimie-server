package com.depromeet.image.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.depromeet.TestQueryDslConfig;
import com.depromeet.fixture.image.ImageFixture;
import com.depromeet.fixture.member.MemberFixture;
import com.depromeet.fixture.memory.MemoryDetailFixture;
import com.depromeet.fixture.memory.MemoryFixture;
import com.depromeet.image.domain.Image;
import com.depromeet.member.Member;
import com.depromeet.member.repository.MemberJpaRepository;
import com.depromeet.member.repository.MemberRepository;
import com.depromeet.member.repository.MemberRepositoryImpl;
import com.depromeet.memory.Memory;
import com.depromeet.memory.MemoryDetail;
import com.depromeet.memory.repository.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ExtendWith(SpringExtension.class)
public class ImageRepositoryTest {
    @Autowired private JPAQueryFactory queryFactory;
    @Autowired private ImageJpaRepository imageJpaRepository;
    @Autowired private MemoryJpaRepository memoryJpaRepository;
    private MemoryRepository memoryRepository;
    @Autowired private MemberJpaRepository memberJpaRepository;
    private MemberRepository memberRepository;
    @Autowired private MemoryDetailJpaRepository memoryDetailJpaRepository;
    private MemoryDetailRepository memoryDetailRepository;
    private ImageRepository imageRepository;

    private Memory memory;
    private Member member;

    @BeforeEach
    void setUp() {
        imageRepository = new ImageRepository(queryFactory, imageJpaRepository);
        memberRepository = new MemberRepositoryImpl(memberJpaRepository);
        memoryRepository = new MemoryRepositoryImpl(queryFactory, memoryJpaRepository);
        memoryDetailRepository = new MemoryDetailRepositoryImpl(memoryDetailJpaRepository);
        member = memberRepository.save(MemberFixture.memberFixture());
    }

    @AfterEach
    void clear() {
        imageJpaRepository.deleteAllInBatch();
        memoryJpaRepository.deleteAllInBatch();
        memoryDetailJpaRepository.deleteAllInBatch();
    }

    @Test
    void findByMemoryId_쿼리_테스트() {
        // given
        List<Image> expectedImages = saveMemoryAndImages();
        List<Long> expectedImageIds = expectedImages.stream().map(Image::getId).toList();

        // when
        List<Image> images = imageRepository.findAllByMemoryId(memory.getId());

        List<Long> imageIds = images.stream().map(Image::getId).toList();
        assertThat(imageIds).isEqualTo(expectedImageIds);
    }

    @Test
    void findImagesByIds_쿼리_테스트() {
        // given
        List<Long> imageIds = saveMemoryAndImages().stream().map(Image::getId).toList();
        // when
        List<Image> resultImages = imageRepository.findImageByIds(imageIds);

        // then
        List<Long> resultImageIds = resultImages.stream().map(Image::getId).toList();
        assertThat(imageIds).isEqualTo(resultImageIds);
    }

    @Test
    void deleteAllByIds_쿼리_테스트() {
        // given
        List<Long> imageIds = saveMemoryAndImages().stream().map(Image::getId).toList();

        // when
        imageRepository.deleteAllByIds(imageIds);

        // then
        assertThat(imageRepository.findImageByIds(imageIds)).isEmpty();
    }

    @Test
    void deleteAllByMemoryId_쿼리_테스트() {
        // given
        saveMemoryAndImages();

        // when
        imageRepository.deleteAllByMemoryId(memory.getId());

        // then
        assertThat(imageRepository.findAllByMemoryId(memory.getId())).isEmpty();
    }

    List<Image> saveMemoryAndImages() {
        saveMemory();
        return saveImages(memory);
    }

    void saveMemory() {
        MemoryDetail memoryDetail = MemoryDetailFixture.mockMemoryDetail();
        memoryDetail = memoryDetailRepository.save(memoryDetail);
        memory =
                memoryRepository.save(
                        MemoryFixture.mockMemory(
                                member, memoryDetail, null, LocalDate.of(2024, 7, 1)));
    }

    List<Image> saveImages(Memory memory) {
        return imageRepository.saveAll(ImageFixture.imageListFixture(memory));
    }
}
