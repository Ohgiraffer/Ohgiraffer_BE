package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.SettingChangeLog;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "setting_change_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettingChangeLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_log_id")
    private Long id;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_field", nullable = false, length = 100)
    private String changedField;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    private SettingChangeLogJpaEntity(Long changedBy, String changedField, String oldValue, String newValue) {
        this.changedBy = changedBy;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }

    public static SettingChangeLogJpaEntity fromDomain(SettingChangeLog log) {
        return new SettingChangeLogJpaEntity(
                log.getChangedBy(), log.getChangedField(), log.getOldValue(), log.getNewValue());
    }

    public SettingChangeLog toDomain() {
        return SettingChangeLog.reconstruct(id, changedBy, changedField, oldValue, newValue, changedAt);
    }
}