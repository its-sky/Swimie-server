package com.depromeet.mock;

import com.depromeet.memory.domain.Memory;
import com.depromeet.memory.domain.vo.Timeline;
import com.depromeet.memory.port.out.persistence.MemoryPersistencePort;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FakeMemoryRepository implements MemoryPersistencePort {
    private Long autoGeneratedId = 0L;
    private final List<Memory> data = new ArrayList<>();

    @Override
    public Memory save(Memory memory) {
        if (memory.getId() == null || memory.getId() == 0) {
            Memory newMemory =
                    Memory.builder()
                            .id(++autoGeneratedId)
                            .member(memory.getMember())
                            .pool(memory.getPool())
                            .memoryDetail(memory.getMemoryDetail())
                            .recordAt(memory.getRecordAt())
                            .startTime(memory.getStartTime())
                            .endTime(memory.getEndTime())
                            .lane(memory.getLane())
                            .diary(memory.getDiary())
                            .build();
            data.add(newMemory);
            return newMemory;
        } else {
            data.removeIf(item -> item.getId().equals(memory.getId()));
            data.add(memory);
            return memory;
        }
    }

    @Override
    public Optional<Memory> findById(Long memoryId) {
        return data.stream().filter(item -> item.getId().equals(memoryId)).findAny();
    }

    @Override
    public Optional<Memory> findByIdWithMember(Long memoryId) {
        return Optional.empty();
    }

    @Override
    public Optional<Memory> findByRecordAtAndMemberId(LocalDate recordAt, Long memberId) {
        return Optional.empty();
    }

    @Override
    public Optional<Memory> update(Long memoryId, Memory memoryUpdate) {
        Optional<Memory> md = data.stream().filter(item -> item.getId().equals(memoryId)).findAny();
        if (md.isEmpty()) {
            return Optional.empty();
        } else {
            Memory origin = md.get();
            return Optional.of(
                    Memory.builder()
                            .id(memoryId)
                            .member(origin.getMember())
                            .pool(
                                    memoryUpdate.getPool() != null
                                            ? memoryUpdate.getPool()
                                            : origin.getPool())
                            .memoryDetail(
                                    memoryUpdate.getMemoryDetail() != null
                                            ? memoryUpdate.getMemoryDetail()
                                            : origin.getMemoryDetail())
                            .strokes(
                                    memoryUpdate.getStrokes() != null
                                            ? memoryUpdate.getStrokes()
                                            : origin.getStrokes())
                            .images(
                                    memoryUpdate.getImages() != null
                                            ? memoryUpdate.getImages()
                                            : origin.getImages())
                            .recordAt(
                                    memoryUpdate.getRecordAt() != null
                                            ? memoryUpdate.getRecordAt()
                                            : origin.getRecordAt())
                            .startTime(
                                    memoryUpdate.getStartTime() != null
                                            ? memoryUpdate.getStartTime()
                                            : origin.getStartTime())
                            .endTime(
                                    memoryUpdate.getEndTime() != null
                                            ? memoryUpdate.getEndTime()
                                            : origin.getEndTime())
                            .lane(
                                    memoryUpdate.getLane() != null
                                            ? memoryUpdate.getLane()
                                            : origin.getLane())
                            .diary(
                                    memoryUpdate.getDiary() != null
                                            ? memoryUpdate.getDiary()
                                            : origin.getDiary())
                            .build());
        }
    }

    @Override
    public int findOrderInMonth(Long memberId, Long memoryId, int month) {
        return 0;
    }

    @Override
    public Timeline findPrevMemoryByMemberId(
            Long memberId, LocalDate cursorRecordAt, LocalDate recordAt) {
        List<Memory> memories;
        if (cursorRecordAt == null) {
            memories =
                    data.stream()
                            .filter(memory -> memory.getMember().getId().equals(memberId))
                            .toList();
        } else {
            LocalDate finalCursorRecordAt = cursorRecordAt;
            memories =
                    data.stream()
                            .filter(memory -> memory.getMember().getId().equals(memberId))
                            .filter(memory -> memory.getRecordAt().isBefore(finalCursorRecordAt))
                            .toList();
        }
        memories = new ArrayList<>(memories);
        memories.sort((memory1, memory2) -> memory2.getRecordAt().compareTo(memory1.getRecordAt()));
        if (memories.size() > 10) {
            memories = memories.subList(0, 10);
        }

        boolean hasNext = false;
        cursorRecordAt = null;
        if (!memories.isEmpty() && memories.size() >= 10) {
            Memory lastMemory = memories.get(memories.size() - 1);
            cursorRecordAt = lastMemory.getRecordAt();
            hasNext = true;
        }

        return Timeline.builder()
                .timelineContents(memories)
                .pageSize(10)
                .cursorRecordAt(cursorRecordAt)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public Timeline findNextMemoryByMemberId(
            Long memberId, LocalDate cursorRecordAt, LocalDate recordAt) {
        List<Memory> memories;
        if (cursorRecordAt == null) {
            memories =
                    data.stream()
                            .filter(memory -> memory.getMember().getId().equals(memberId))
                            .toList();
            memories.sort(
                    (memory1, memory2) -> memory2.getRecordAt().compareTo(memory1.getRecordAt()));
            if (memories.size() > 10) {
                memories = memories.subList(0, 10);
            }
        } else {
            LocalDate finalCursorRecordAt = cursorRecordAt;
            memories =
                    data.stream()
                            .filter(memory -> memory.getMember().getId().equals(memberId))
                            .filter(memory -> memory.getRecordAt().isAfter(finalCursorRecordAt))
                            .toList();
            if (memories.size() > 10) {
                memories = memories.subList(0, 10);
            }
            memories.sort(
                    (memory1, memory2) -> memory2.getRecordAt().compareTo(memory1.getRecordAt()));
        }

        boolean hasNext = false;
        cursorRecordAt = null;
        if (!memories.isEmpty()) {
            Memory lastMemory = memories.get(0);
            cursorRecordAt = lastMemory.getRecordAt();
            hasNext = true;
        }

        return Timeline.builder()
                .timelineContents(memories)
                .pageSize(10)
                .cursorRecordAt(cursorRecordAt)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public List<Memory> getCalendarByYearAndMonth(Long memberId, Integer year, Short month) {
        return null;
    }

    @Override
    public Optional<Memory> findPrevMemoryByRecordAtAndMemberId(LocalDate recordAt, Long memberId) {
        return data.stream()
                .filter(
                        item ->
                                item.getRecordAt().isBefore(recordAt)
                                        && item.getMember().getId().equals(memberId))
                .max(Comparator.comparing(Memory::getRecordAt));
    }

    @Override
    public Optional<Memory> findNextMemoryByRecordAtAndMemberId(LocalDate recordAt, Long memberId) {
        return data.stream()
                .filter(
                        item ->
                                item.getRecordAt().isBefore(recordAt)
                                        && item.getMember().getId().equals(memberId))
                .min(Comparator.comparing(Memory::getRecordAt));
    }
}
