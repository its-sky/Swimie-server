package com.depromeet.notification.repository;

import static com.depromeet.member.entity.QMemberEntity.*;
import static com.depromeet.memory.entity.QMemoryEntity.*;
import static com.depromeet.notification.entity.QReactionLogEntity.*;
import static com.depromeet.reaction.entity.QReactionEntity.*;

import com.depromeet.member.entity.QMemberEntity;
import com.depromeet.notification.domain.ReactionLog;
import com.depromeet.notification.entity.ReactionLogEntity;
import com.depromeet.notification.port.out.ReactionLogPersistencePort;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionLogRepository implements ReactionLogPersistencePort {
    private final ReactionLogJpaRepository reactionLogJpaRepository;
    private final JPAQueryFactory queryFactory;

    QMemberEntity member = new QMemberEntity("member");
    QMemberEntity memoryMember = new QMemberEntity("memoryMember");

    @Override
    public ReactionLog save(ReactionLog reactionLog) {
        return reactionLogJpaRepository.save(ReactionLogEntity.from(reactionLog)).toPureModel();
    }

    @Override
    public List<ReactionLog> findByMemberIdAndCursorCreatedAt(
            Long memberId, LocalDateTime cursorCreatedAt) {
        return queryFactory
                .selectFrom(reactionLogEntity)
                .join(reactionLogEntity.reaction, reactionEntity)
                .fetchJoin()
                .join(reactionEntity.member, member)
                .fetchJoin()
                .join(reactionEntity.memory, memoryEntity)
                .fetchJoin()
                .join(memoryEntity.member, memoryMember)
                .fetchJoin()
                .where(memberEq(memberId), createdAtLoe(cursorCreatedAt))
                .limit(11)
                .orderBy(reactionLogEntity.createdAt.desc())
                .fetch()
                .stream()
                .map(ReactionLogEntity::toModel)
                .collect(Collectors.toList());
    }

    private BooleanExpression createdAtLoe(LocalDateTime cursorCreatedAt) {
        if (cursorCreatedAt == null) {
            return null;
        }
        return reactionLogEntity.createdAt.loe(cursorCreatedAt);
    }

    private BooleanExpression memberEq(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return memoryMember.id.eq(memberId);
    }
}
